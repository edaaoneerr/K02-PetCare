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
import com.example.petcareproject.abstracts.OpenRouteServiceApi
import com.example.petcareproject.abstracts.RouteResponse
import com.example.petcareproject.model.Vet
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class VetListViewModel() : ViewModel() {

    val TAG = "VetListViewModel"
    var vetId: String? = null
    var clinicId: String = ""
    var vetClinicName:String = ""
    var vetNationalId: String= ""
    var vetName: String = ""
    var vetSpecialty: String= ""
    var vetPatientCount: Int = 0
    var vetExperienceYears: Double = 0.0
    var vetRating: Double = 0.0
    var vetDistance: Double = 0.0
    var vetReviewCount: Int = 0
    var vetImageUrl: String = ""
    var vetAbout: String = ""
    var vetWorkingHours: Map<String, String>  = emptyMap()
    private val _vets = MutableLiveData<List<Vet>>()
    val vets: LiveData<List<Vet>> = _vets
    private val _calculatedVetDistance = MutableLiveData<Double>()
    val calculatedVetDistance: LiveData<Double> = _calculatedVetDistance
    var userLat: Double = 0.0
    var userLng: Double = 0.0
    var latitude: Double? = 0.0
    var longitude: Double? = 0.0
    private var locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = 10000 // Update interval in milliseconds
        fastestInterval = 5000 // Fastest update interval in milliseconds
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY // Set the priority to high accuracy
    }
    private var lrsBuilder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
    //private var fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    fun updateVets(vetList: List<Vet>) {
        _vets.postValue(vetList)
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        private const val PICK_IMAGE_REQUEST = 1
    }


    fun fetchLocationAndSetupVetRecyclerView(activity: Activity, context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    userLat = location.latitude
                    userLng = location.longitude
                    println("User Location: Lat: ${userLat}, Lng: ${userLng}")

                } else  {
                    checkDeviceSettings(activity, context)
                    userLat = 0.0
                    userLng = 0.0
                    println("User Location: Lat: ${userLat}, Lng: ${userLng}")
                }
                setupVetRecyclerView()
            }.addOnFailureListener {
                println("Failed to get user location: ${it.message}")
            }
        }
    }
    fun checkDeviceSettings(activity: Activity, context: Context) {
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

    fun setupVetRecyclerView() {
        fetchVeterinariansAndUpdateDistance(userLat, userLng)
    }
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openrouteservice.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(OpenRouteServiceApi::class.java)

    /*    fun fetchVDistance(userLat: Double, userLng: Double, clinicLat: Double, clinicLng: Double) {
            viewModelScope.launch {
                try {
                    val start = "${URLEncoder.encode(userLng.toString(), "UTF-8")},${URLEncoder.encode(userLat.toString(), "UTF-8")}"
                    val end = "${URLEncoder.encode(clinicLng.toString(), "UTF-8")},${URLEncoder.encode(clinicLat.toString(), "UTF-8")}"
                    val response = service.getRoute("5b3ce3597851110001cf6248c218b52ee6f74e379ed1a27e7d44e37a", start, end)
                    val distance = response.features[0].properties.summary.distance / 1000 // distance in kilometers
                    _calculatedVetDistance.postValue(distance)
                } catch (e: Exception) {
                    println("Error fetching route: ${e.localizedMessage}")
                }
            }
        }*/

    suspend fun fetchVDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        return withContext(Dispatchers.IO) { // Switch to IO dispatcher for network calls
            val apiKey = "5b3ce3597851110001cf6248c218b52ee6f74e379ed1a27e7d44e37a" // Replace this with your actual API key
            val start = "$lon1,$lat1"
            val end = "$lon2,$lat2"
            val response = service.getRoute(apiKey, start, end)
            parseDistanceFromJson(response)
        }
    }

    fun parseDistanceFromJson(response: RouteResponse): Double {
        if (response.features.isNotEmpty()) {
            val feature = response.features[0]
            if (feature.properties.segments.isNotEmpty()) {
                return feature.properties.segments[0].distance / 1000
            }
        }
        return 0.0 // Return 0.0 if no distance is found
    }
    fun formatDistance(distance: Double): String {
        return if (distance < 1) {
            // Convert kilometers to meters if the distance is less than 1 kilometer
            val meters = (distance * 1000).toInt()
            "$meters meters"
        } else {
            // Format distance to two decimal places if it's more than 1 kilometer
            String.format("%.2f km", distance)
        }
    }

    private fun fetchVeterinariansAndUpdateDistance(userLat: Double, userLng: Double) {
        val db = FirebaseFirestore.getInstance()
        db.collection("veterinarians")
            .get()
            .addOnSuccessListener { result ->
                val vets = mutableListOf<Vet>()
                viewModelScope.launch { // Use lifecycleScope to launch coroutines in a Fragment
                    result.forEach { document ->
                        try {
                            val vetId = document.id
                            val clinicId = document.getString("clinicId") ?: ""
                            val vetClinicName = document.getString("vetClinicName") ?: ""
                            val vetName = document.getString("vetName") ?: ""
                            val vetSpecialty = document.getString("vetSpecialty") ?: ""
                            val vetPatientCount = document.getDouble("vetPatientCount")?.toInt() ?: 0
                            val vetExperienceYears = document.getDouble("vetExperienceYears") ?: 0.0
                            val vetRating = document.getString("vetRating") ?: "0.0"
                            val vetReviewCount = document.getDouble("vetReviewCount")?.toInt() ?: 0
                            val vetDistance = document.getString("vetDistance") ?: "0.0"
                            val vetImageUrl = document.getString("vetImageUrl") ?: ""
                            val vetAbout = document.getString("vetAbout") ?: ""
                            val vetWorkingHours = document.get("vetWorkingTime") as? Map<String, String> ?: emptyMap()

                            if (vetDistance == "0") {
                                fetchClinicDistance(clinicId, userLat, userLng) { distance ->
                                    val vet = Vet(vetId, clinicId, vetClinicName, vetName, vetSpecialty, vetPatientCount, vetExperienceYears, vetRating, vetReviewCount, formatDistance(distance), vetImageUrl, vetAbout, vetWorkingHours)
                                    vets.add(vet)
                                    if (vets.size == result.size()) {
                                        updateVets(vets)
                                    }
                                }
                            } else {
                                val vet = Vet(vetId, clinicId, vetClinicName, vetName, vetSpecialty, vetPatientCount, vetExperienceYears, vetRating, vetReviewCount, formatDistance(vetDistance.toDouble()), vetImageUrl, vetAbout, vetWorkingHours)
                                vets.add(vet)
                                if (vets.size == result.size()) {
                                    updateVets(vets)
                                }
                            }
                        } catch (e: Exception) {
                            println("Document null: $e")
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }

    private fun fetchClinicDistance(clinicId: String, userLat: Double, userLng: Double, callback: (Double) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("veterinary_clinics")
            .whereEqualTo("clinicId", clinicId)
            .get()
            .addOnSuccessListener { result ->
                viewModelScope.launch {
                    result.forEach { document ->
                        try {
                            latitude = document.getDouble("latitude") ?: 0.0
                            longitude = document.getDouble("longitude") ?: 0.0

                            if (latitude != 0.0 && longitude != 0.0 && userLat != 0.0 && userLng != 0.0) {
                                val distance = fetchVDistance(userLat, userLng, latitude!!, longitude!!)
                                callback(distance)
                            } else {
                                println("Could not fetch VET location. Please turn on from your settings.")
                            }
                        } catch (e: Exception) {
                            println("Could not fetch VET location. Please turn on from your settings.")
                            println("Document null: $e")
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }

}