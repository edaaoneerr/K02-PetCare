package com.example.petcareproject.views

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.petcareproject.adapters.VeterinaryClinicAdapter
import com.example.petcareproject.databinding.FragmentVetClinicListBinding
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
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class VetClinicListFragment : Fragment() {

    private var _binding: FragmentVetClinicListBinding? = null
    private val binding get() = _binding!!

    private var locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = 10000 // Update interval in milliseconds
        fastestInterval = 5000 // Fastest update interval in milliseconds
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY // Set the priority to high accuracy
    }
    private var lrsBuilder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
    //private var fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var vetClinicsAdapter: VeterinaryClinicAdapter? = null

    var userLat: Double = 0.0
    var userLng: Double = 0.0
    var latitude: Double? = 0.0
    var longitude: Double? = 0.0
    var name: String = ""
    var specialty: String = ""
    var address: String = ""
    var imageUrl: String = ""
    var rating: String = ""
    var vetDistance: Double? = 0.0

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVetClinicListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            // Use Glide to convert URI to Bitmap
            Glide.with(this)
                .asBitmap()
                .load(imageUri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val baos = ByteArrayOutputStream()
                        resource.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val data = baos.toByteArray()
                        println(data)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Removing tint from icons and background from the BottomNavigationView

        /*  val viewPager = binding.campaignCarousel
          val items = listOf(R.drawable.campaign_1, R.drawable.campaign_2, R.drawable.campaign_3, R.drawable.campaign_4) // Replace with your image resources
          val adapter = CampaignCarouselPagerAdapter(items, requireContext())
          viewPager.adapter = adapter
  */
    }
    override fun onStart() {
        super.onStart()
        fetchLocationAndSetupClinicRecyclerView()

    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            println("Permission granted")
            fetchLocationAndSetupClinicRecyclerView()

        }
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
    private fun fetchLocationAndSetupClinicRecyclerView() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    userLat = location.latitude
                    userLng = location.longitude
                    println("User Location: Lat: ${userLat}, Lng: ${userLng}")

                } else  {
                    checkDeviceSettings()
                    userLat = 0.0
                    userLng = 0.0
                    println("User Location: Lat: ${userLat}, Lng: ${userLng}")
                }
                setupClinicRecyclerView()
            }.addOnFailureListener {
                println("Failed to get user location: ${it.message}")
            }
        }
    }
    fun checkDeviceSettings() {
        val result = LocationServices.getSettingsClient(requireContext()).checkLocationSettings(lrsBuilder!!.build())
        result.addOnSuccessListener {
            Toast.makeText(context, "Success GPS Enabled", Toast.LENGTH_LONG).show()
        }
        result.addOnFailureListener { resolvableApiException ->
            Toast.makeText(context, "Failure GPS Disabled", Toast.LENGTH_LONG).show()
            //The Line of code below enables the location on the AVD so long as the user selects OK from the dialog.
            //Sometimes after running this app you may need to disable and re-enable the the location of the AVD for it to work.
            (resolvableApiException as ResolvableApiException).startResolutionForResult(requireActivity(), 6)
        }
    }
    private fun setupClinicRecyclerView() {
        fetchClinicsAndUpdateDistance(userLat, userLng)
    }
    private fun fetchClinicsAndUpdateDistance(userLat: Double, userLng: Double) {
        val db = FirebaseFirestore.getInstance()
        db.collection("veterinary_clinics")
            .orderBy("rating", Query.Direction.DESCENDING)
            .orderBy("distance")
            .get()
            .addOnSuccessListener { result ->
                val clinics = mutableListOf<VeterinaryClinic>()
                lifecycleScope.launch { // Use lifecycleScope to launch coroutines in a Fragment
                    result.forEach { document ->
                        // Extract details from the document and call getDistance
                        // Update clinics list inside the coroutine
                        try {
                            name = document.getString("name") ?: ""
                            specialty = document.getString("specialty") ?: ""
                            rating = document.getString("rating") ?: ""
                            address = document.getString("address") ?: ""
                            latitude = document.getDouble("latitude") ?: 0.0
                            longitude = document.getDouble("longitude") ?: 0.0
                            imageUrl = document.getString("clinic_image") ?: "" // Fetch the image URL
                            if (latitude != null && longitude != null) {
                                val clinicLat = latitude
                                val clinicLng = longitude
                                vetDistance = getDistance(userLat, userLng, clinicLat!!, clinicLng!!) / 1000.0
                                clinics.add(VeterinaryClinic(name, specialty, rating, latitude!!, longitude!!, address, String.format("%.2f", vetDistance).toDouble(), imageUrl))

                                // Calculate distance
                                val clinicDistance = String.format("%.2f", vetDistance).toDouble()

                                // Update the clinic document with the new distance
                                val clinicRef = document.reference
                                println("Clinic ref" + clinicRef)
                                clinicRef.update("distance", clinicDistance)

                                println("$name, $specialty, $rating, $longitude, $latitude, $vetDistance, $imageUrl}")
                            } else {
                                println("$name, $specialty, $rating, $longitude, $latitude}")
                            }
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Could not fetch location. Please turn on from your settings.",
                                Toast.LENGTH_LONG
                            ).show()
                            clinics.add(VeterinaryClinic(name, specialty, rating, latitude!!, longitude!!, address, 0.0, imageUrl))
                            println("Document null: $e") // or handle the exception as needed
                        }
                    }
                    // After the loop, update the adapter
                    updateAdapter(clinics)
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }
    private fun updateAdapter(clinics: List<VeterinaryClinic>) {
       vetClinicsAdapter = VeterinaryClinicAdapter(clinics)
        binding.vetClinicsRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.vetClinicsRecyclerView.adapter = vetClinicsAdapter
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}