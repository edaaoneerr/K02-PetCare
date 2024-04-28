package com.example.petcareproject.viewmodels

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcareproject.model.VeterinaryClinic
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class VetClinicListViewModel() : ViewModel() {

    private val _userLat = MutableLiveData<Double>()
    val userLat: LiveData<Double> = _userLat

    private val _userLng = MutableLiveData<Double>()
    val userLng: LiveData<Double> = _userLng

    private val _latitude = MutableLiveData<Double>()
    val latitude: LiveData<Double> = _latitude

    private val _longitude = MutableLiveData<Double>()
    val longitude: LiveData<Double> = _longitude

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> = _name

    private val _specialty = MutableLiveData<String>()
    val specialty: LiveData<String> = _specialty

    private val _address = MutableLiveData<String>()
    val address: LiveData<String> = _address

    private val _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String> = _imageUrl

    private val _rating = MutableLiveData<String>()
    val rating: LiveData<String> = _rating

    private val _vetDistance = MutableLiveData<String>()
    val vetDistance: LiveData<String> = _vetDistance

    private val _clinics = MutableLiveData<List<VeterinaryClinic>>()
    val clinics: LiveData<List<VeterinaryClinic>> = _clinics

    val errorLiveData = MutableLiveData<String>()

    private var locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = 10000 // Update interval in milliseconds
        fastestInterval = 5000 // Fastest update interval in milliseconds
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY // Set the priority to high accuracy
    }
    private var lrsBuilder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
    //private var fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        private const val PICK_IMAGE_REQUEST = 1
    }


    fun fetchClinicsAndUpdateDistance(query: Query): List<VeterinaryClinic> {
        /*val query = db.collection("veterinary_clinics")
            .orderBy("rating", Query.Direction.DESCENDING)
            .orderBy("distance")*/
            query.get()
            .addOnSuccessListener { result ->
                val clinics = mutableListOf<VeterinaryClinic>()
                viewModelScope.launch(Dispatchers.IO) { // Use lifecycleScope to launch coroutines in a Fragment
                    result.forEach { document ->
                        // Extract details from the document and call getDistance
                        // Update clinics list inside the coroutine
                        try {
                            _name.postValue(document.getString("name") ?: "")
                            _specialty.postValue(document.getString("specialty") ?: "")
                            _rating.postValue(document.getString("rating") ?: "")
                            _address.postValue(document.getString("address") ?: "")
                            _latitude.postValue(document.getDouble("latitude") ?: 0.0)
                            _longitude.postValue(document.getDouble("longitude") ?: 0.0)
                            _imageUrl.postValue(document.getString("clinic_image") ?: "") // Fetch the image URL

                            if (_latitude.value != null && _longitude.value != null) {
                                val clinicLat = latitude.value
                                val clinicLng = longitude.value

                                val distance = getDistance(_userLat.value!!, userLng.value!!, clinicLat!!, clinicLng!!) / 1000.0
                                val clinicDistance = String.format("%.2f", distance.toString()).toDouble()
                                _vetDistance.postValue(clinicDistance.toString())
                                clinics.add(VeterinaryClinic(_name.value!!, specialty.value!!, rating.value!!, latitude.value!!, longitude.value!!, address.value!!, clinicDistance, imageUrl.value!!))

                                // Update the clinic document with the new distance
                                val clinicRef = document.reference
                                println("Clinic ref" + clinicRef)
                                clinicRef.update("distance", clinicDistance)
                                _clinics.postValue(clinics)
                                println("$name, $specialty, $rating, $longitude, $latitude, $vetDistance, $imageUrl}")
                            } else {
                                println("$name, $specialty, $rating, $longitude, $latitude}")
                            }
                        } catch (e: Exception) {
                            errorLiveData.postValue("Could not fetch location. Please turn on from your settings.")
                            _clinics.postValue(clinics)
                            clinics.add(VeterinaryClinic(_name.value!!, specialty.value!!, rating.value!!, latitude.value!!, longitude.value!!, address.value!!, 0.0, imageUrl.value!!))
                            println("Document null: $e") // or handle the exception as needed
                        }
                    }
                }

            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
        return _clinics.value!!
    }

    suspend fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        return withContext(Dispatchers.IO) { // Switch to IO dispatcher for network calls
            val apiKey = "55fb6421-e6d5-4dc2-adb4-537fafa8a328" // Replace this with your actual API key
            val urlString = "https://graphhopper.com/api/1/route?point=$lat1,$lon1&point=$lat2,$lon2&vehicle=car&key=$apiKey"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = reader.readText()
            reader.close()
            // Parse the JSON response
            parseDistanceFromJson(response)
        }
    }

    fun parseDistanceFromJson(jsonResponse: String): Double {
        val jsonObject = JsonParser.parseString(jsonResponse).asJsonObject
        val paths = jsonObject.getAsJsonArray("paths")
        if (paths != null && paths.size() > 0) {
            val path = paths.get(0).asJsonObject
            val distance = path.get("distance").asDouble
            return distance
        }
        return 0.0 // Return 0.0 if no distance is found
    }


    fun fetchLocationAndSetupClinicRecyclerView(query: Query, activity: Activity, context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    _userLat.postValue(location.latitude)
                    _userLng.postValue(location.longitude)
                    println("User Location: Lat: ${userLat}, Lng: ${userLng}")
                    viewModelScope.launch {
                        fetchClinicsAndUpdateDistance(query)
                    }

                } else  {
                    checkDeviceSettings(context, activity)
                    _userLat.postValue(0.0)
                    _userLng.postValue(0.0)
                    println("User Location: Lat: ${userLat}, Lng: ${userLng}")
                    viewModelScope.launch {
                        fetchClinicsAndUpdateDistance(query)
                    }
                }

            }.addOnFailureListener {
                println("Failed to get user location: ${it.message}")
            }
        }
    }


    fun checkDeviceSettings(context: Context, activity: Activity) {
        val result = LocationServices.getSettingsClient(context).checkLocationSettings(lrsBuilder!!.build())
        result.addOnSuccessListener {
            Toast.makeText(context, "Success GPS Enabled", Toast.LENGTH_LONG).show()
        }
        result.addOnFailureListener { resolvableApiException ->
            Toast.makeText(context, "Failure GPS Disabled", Toast.LENGTH_LONG).show()
            //The Line of code below enables the location on the AVD so long as the user selects OK from the dialog.
            //Sometimes after running this app you may need to disable and re-enable the the location of the AVD for it to work.
            (resolvableApiException as ResolvableApiException).startResolutionForResult(activity, 6)
        }
    }

    private val db = FirebaseFirestore.getInstance()

    /*suspend fun fetchClinicsAndUpdateDistance(query: Query) {
        val result = query.get().await()
        val clinics = mutableListOf<VeterinaryClinic>()
        result.forEach { document ->
            val clinic = document.toObject(VeterinaryClinic::class.java)
            // Calculate distance within a coroutine context
            val clinicDistance = withContext(Dispatchers.IO) {
                getDistance(_userLat.value!!, _userLng.value!!, clinic.latitude, clinic.longitude) / 1000.0
            }
            // Update the clinic document with the new distance
            updateDistance(document.id, clinicDistance)
            // Update clinic object with distance
            clinic.distance = String.format("%.2f", clinicDistance) + " km"
            clinics.add(clinic)
        }
        _clinics.postValue(clinics)
    }
*/

    private fun updateDistanceAndFetchClinics(location: Location, query: Query, callback: (List<VeterinaryClinic>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val clinics = mutableListOf<VeterinaryClinic>()
            // Perform Firestore query and distance calculations here using the location
            // Update LiveData or invoke callback with the list
            _clinics.postValue(clinics)
            callback(clinics)
        }
    }



    private fun updateDistance(clinicId: String, distance: Double) {
        val clinicRef = db.collection("veterinary_clinics").document(clinicId)
        clinicRef.update("distance", distance)
            .addOnSuccessListener {
                // Distance updated successfully
            }
            .addOnFailureListener { exception ->
                println("Error updating distance: $exception")
            }
    }
}