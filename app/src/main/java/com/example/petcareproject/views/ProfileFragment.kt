package com.example.petcareproject.views

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.petcareproject.databinding.FragmentProfileBinding
import com.example.petcareproject.factory.AuthViewModelFactory
import com.example.petcareproject.model.Review
import com.example.petcareproject.repository.AuthRepository
import com.example.petcareproject.viewmodels.AuthViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Random as JavaRandom
import kotlin.random.Random as KotlinRandom

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var documentIds: List<String>? = null
    private var currentIndex = 0

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


       binding.btnUploadImages.setOnClickListener {
             pickImageAndUpload()
         }
        fetchDocumentIds()
       // generateAndRegisterUsers(viewModel, viewLifecycleOwner, 1)
        createAndUploadReviewsForVet()

        // fetchClinicsAndGenerateVets()
    }

    fun pickImageAndUpload() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            // Use Glide to convert URI to Bitmap
            Glide.with(this)
                .asBitmap()
                .load(imageUri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        uploadImageToFirebase(resource)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        }
    }

  /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
      super.onActivityResult(requestCode, resultCode, data)
      if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
          val svgUri = data.data
          if (svgUri != null) {
              uploadSvgToFirebase(svgUri)
          }
      }
  }*/
    private fun fetchDocumentIds() {
        val db = FirebaseFirestore.getInstance()
        db.collection("veterinarians").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                documentIds = task.result?.documents?.map { it.id }
            } else {
                task.exception?.let {
                    println("Error fetching document IDs: ${it.message}")
                }
            }
        }
    }
    fun displayImageFromFirebase(imageView: ImageView, filePath: String) {
        val storageRef = FirebaseStorage.getInstance().getReference(filePath)
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this).load(uri).into(imageView)
        }.addOnFailureListener {
            println("Error from choosing firebase")
        }
    }
    data class Clinic(val clinicId: String, val clinicName: String, val clinicDistance: Double)


   /* fun generateRandomVets(clinics: List<Clinic>) {
        val db = FirebaseFirestore.getInstance()
        val random = KotlinRandom.Default
        val specialties = listOf("Hematology", "Cardiology", "Orthopedics", "Neurology", "Oncology")
        val workingHours = mapOf(
            "Monday" to "08.00 AM-18.00 PM",
            "Tuesday" to "08.00 AM-18.00 PM",
            "Wednesday" to "08.00 AM-18.00 PM",
            "Thursday" to "08.00 AM-18.00 PM",
            "Friday" to "08.00 AM-18.00 PM",
            "Saturday" to "08.00 AM-18.00 PM",
            "Sunday" to "08.00 AM-18.00 PM"
        )
        val names = mutableListOf(
            "Alice Johnson", "Bob Smith", "Carol Williams", "David Jones", "Emma Brown",
            "Frank White", "Grace Taylor", "Hank Anderson", "Ivy Thomas", "Jack Martinez",
            "Kathy Robinson", "Liam Clark", "Mia Rodriguez", "Noah Lewis", "Olivia Lee",
            "Paul Walker", "Quinn Hall", "Rita Allen", "Sam Young", "Tina Hernandez",
            "Uma King", "Victor Scott", "Wendy Green", "Xander Adams", "Yara Baker",
            "Zane Cooper", "Amelia Turner", "Ben Parker", "Catherine Evans", "Daniel Hill",
            "Edward Johnson", "Fiona Brown", "George White", "Hannah Green", "Isaac King",
            "Julia Adams", "Kevin Lee", "Laura Clark", "Michael Davis", "Nancy Wilson",
            "Oscar Miller", "Penny Thompson", "Quincy Wright", "Rachel Harris", "Steven Lewis",
            "Theresa Scott", "Ulysses Martinez", "Vanessa Robinson", "Walter Evans", "Xena Hall",
            "Yvonne Cooper", "Zachary Phillips"
        )
        names.shuffle()

        for (i in 1..30) {
            val clinic = clinics[random.nextInt(clinics.size)]
            val vetName = names[i - 1] // Ensure unique names by using the shuffled list
            val vet = hashMapOf(
                "clinicId" to clinic.clinicId,
                "vetClinicName" to clinic.clinicName,
                "vetName" to vetName,
                "vetSpecialty" to specialties[random.nextInt(specialties.size)],
                "vetPatientCount" to random.nextInt(50, 300),
                "vetExperienceYears" to random.nextDouble(1.0, 10.0),
                "vetRating" to random.nextDouble(3.0, 5.0).toString(),
                "vetReviewCount" to random.nextInt(0, 100),
                "vetDistance" to clinic.clinicDistance,
                "vetAbout" to "This is a generated vet $vetName",
                "vetWorkingTime" to workingHours,
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now(),
                "isActive" to true,
                "lastSignInAt" to Timestamp.now()
            )

            db.collection("veterinarians")
                .add(vet)
                .addOnSuccessListener { documentReference ->
                    val vetId = documentReference.id
                    documentReference.update("vetId", vetId)
                        .addOnSuccessListener {
                            println("Veterinarian added with ID: $vetId")
                        }
                        .addOnFailureListener { e ->
                            println("Error updating vetId: $e")
                        }
                }
                .addOnFailureListener { e ->
                    println("Error adding veterinarian: $e")
                }
        }
    }*/

    fun generateAndRegisterUsers(viewModel: AuthViewModel, viewLifecycleOwner: LifecycleOwner, vetCount: Int) {
        val db = FirebaseFirestore.getInstance()
        db.collection("veterinary_clinics")
            .get()
            .addOnSuccessListener { result ->
                val clinics = mutableListOf<Clinic>()
                for (document in result) {
                    val clinicId = document.id
                    val clinicName = document.getString("name") ?: ""
                    val clinicDistance = document.getDouble("distance") ?: 0.0
                    clinics.add(Clinic(clinicId, clinicName, clinicDistance))
                }
                println("Generating $vetCount veterinarians...")
                generateRandomVets(clinics, viewModel, viewLifecycleOwner, vetCount)
            }
            .addOnFailureListener { exception ->
                println("Error getting clinic documents: $exception")
            }
    }

    fun generateRandomVets(clinics: List<Clinic>, viewModel: AuthViewModel, viewLifecycleOwner: LifecycleOwner, vetCount: Int) {
        val db = FirebaseFirestore.getInstance()
        val random = KotlinRandom.Default
        val specialties = listOf(
            "Cardiology", "Radiology", "Anaesthesia", "Intensive Care Medicine", "Surgery",
            "Neurology", "Orthopaedics", "Welfare", "Laboratory Animal Medicine", "Nutrition",
            "Oncology", "Preventive Medicine", "Pharmacology", "Internal Medicine", "Microbiology",
            "Poultry", "Theriogenology", "Behavior", "Behavioral Medicine", "Dermatology",
            "Ophthalmology", "Dentistry", "Pathology", "Vet Dermatologist"
        )
        val workingHours = mapOf(
            "Monday" to "08.00 AM-18.00 PM",
            "Tuesday" to "08.00 AM-18.00 PM",
            "Wednesday" to "08.00 AM-18.00 PM",
            "Thursday" to "08.00 AM-18.00 PM",
            "Friday" to "08.00 AM-18.00 PM",
            "Saturday" to "08.00 AM-18.00 PM",
            "Sunday" to "08.00 AM-18.00 PM"
        )
        val names = mutableListOf(
            "Alice Johnson", "Bob Smith", "Carol Williams", "David Jones", "Emma Brown",
            "Frank White", "Grace Taylor", "Hank Anderson", "Ivy Thomas", "Jack Martinez",
            "Kathy Robinson", "Liam Clark", "Mia Rodriguez", "Noah Lewis", "Olivia Lee",
            "Paul Walker", "Quinn Hall", "Rita Allen", "Sam Young", "Tina Hernandez",
            "Uma King", "Victor Scott", "Wendy Green", "Xander Adams", "Yara Baker",
            "Zane Cooper", "Amelia Turner", "Ben Parker", "Catherine Evans", "Daniel Hill",
            "Edward Johnson", "Fiona Brown", "George White", "Hannah Green", "Isaac King",
            "Julia Adams", "Kevin Lee", "Laura Clark", "Michael Davis", "Nancy Wilson",
            "Oscar Miller", "Penny Thompson", "Quincy Wright", "Rachel Harris", "Steven Lewis",
            "Theresa Scott", "Ulysses Martinez", "Vanessa Robinson", "Walter Evans", "Xena Hall",
            "Yvonne Cooper", "Zachary Phillips"
        ).shuffled()

        val emails = names.map { it.replace(" ", "").lowercase() + "@example.com" }.shuffled()
        val passwords = List(names.size) { "password123" }

        names.zip(emails).zip(passwords).take(vetCount).forEach { pair ->
            val (nameEmail, password) = pair
            val (name, email) = nameEmail
            val isVet = true
            val clinic = clinics[random.nextInt(clinics.size)] // Randomly select a clinic for each vet

            GlobalScope.launch(Dispatchers.IO) {
                val vetUri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/pethealer-e19c5.appspot.com/o/veterinarians%2Ffiles%2FvetRegistrationConfirmation%2FdUiV1Q7eTtQzqKTbzxiTAqZFltg1_registrationConfirmationFile?alt=media&token=4194b605-f6ad-41fb-aa83-bea4c79ba5d3")
                viewModel.register(email, password, name, isVet, clinic.clinicName, vetUri)
                withContext(Dispatchers.Main) {
                    viewModel.registrationStatus.observeOnce(viewLifecycleOwner) { task ->
                        if (task?.isSuccessful == true && isVet) {
                            val user = task.result?.user
                            user?.let { user ->
                                val vet = hashMapOf(
                                    "vetId" to user.uid,
                                    "clinicId" to clinic.clinicId,
                                    "vetClinicName" to clinic.clinicName,
                                    "vetName" to name,
                                    "vetSpecialty" to specialties.random(),
                                    "vetPatientCount" to random.nextInt(50, 300),
                                    "vetExperienceYears" to String.format("%.1f", random.nextDouble(1.0, 10.0)).toDouble(),
                                    "vetRating" to String.format("%.1f", random.nextDouble(3.0, 5.0)),
                                    "vetReviewCount" to random.nextInt(0, 100),
                                    "vetDistance" to "0",
                                    "vetAbout" to "This is a generated vet $name",
                                    "vetWorkingTime" to workingHours,
                                    "createdAt" to Timestamp.now(),
                                    "updatedAt" to Timestamp.now(),
                                    "isActive" to true,
                                    "lastSignInAt" to Timestamp.now()
                                )
                                db.collection("veterinarians")
                                    .document(user.uid)
                                    .set(vet)
                                    .addOnSuccessListener {
                                        println("Veterinarian added with ID: ${user.uid}")
                                    }
                                    .addOnFailureListener { e ->
                                        println("Error adding veterinarian: $e")
                                    }
                            } ?: run {
                                println("Error: User is null")
                            }
                        }
                    }
                }
            }
        }
    }

    // Extension function to observe LiveData once
    fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(value: T) {
                observer.onChanged(value)
                removeObserver(this)
            }
        })
    }









    fun uploadImageToFirebase(bitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val storageRef = FirebaseStorage.getInstance().reference.child("veterinarians/images/${System.currentTimeMillis()}.jpg")
        val uploadTask = storageRef.putBytes(data)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                println("Successfully Uploaded!! URL: $imageUrl")
                if (documentIds != null && currentIndex < documentIds!!.size) {
                    updateVetImageInFirestore(documentIds!![currentIndex], imageUrl)
                    currentIndex++
                }
            }
        }.addOnFailureListener {
            println("Error uploading image: ${it.message}")
        }
    }
    fun updateClinicImageInFirestore(clinicId: String, imageUrl: String) {
        val db = FirebaseFirestore.getInstance()
        val clinicRef = db.collection("service_categories").document(clinicId)

        clinicRef.update("serviceCategoryImage", imageUrl)
            .addOnSuccessListener {
                println("DocumentSnapshot successfully updated with image URL!")

            }
            .addOnFailureListener { e ->
                println("Error updating document: $e")
            }
    }
    fun updateVetImageInFirestore(vetId: String, imageUrl: String) {
        val db = FirebaseFirestore.getInstance()
        val clinicRef = db.collection("veterinarians").document(vetId)

        clinicRef.update("vetImageUrl", imageUrl)
            .addOnSuccessListener {
                println("DocumentSnapshot successfully updated with image URL!")

            }
            .addOnFailureListener { e ->
                println("Error updating document: $e")
            }
    }
    fun updateServiceCategoryImageInFirestore(serviceCategoryId: String, serviceCategoryImage: String) {
        val db = FirebaseFirestore.getInstance()
        val serviceCategoryRef = db.collection("service_categories").document(serviceCategoryId)

        serviceCategoryRef.update("serviceCategoryImage", serviceCategoryImage)
            .addOnSuccessListener {
                println("DocumentSnapshot successfully updated with image URL!")

            }
            .addOnFailureListener { e ->
                println("Error updating document: $e")
            }
    }
    fun fetchIds(collectionPath: String, onComplete: (List<String>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection(collectionPath).get()
            .addOnSuccessListener { result ->
                val ids = result.documents.mapNotNull { it.id }
                onComplete(ids)
            }
            .addOnFailureListener { exception ->
                println("Error getting $collectionPath IDs: " + exception)
            }
    }

    fun generateReviewsForClinic(userIds: List<String>, clinicIds: List<String>): List<Review> {
        val reviews = mutableListOf<Review>()
        val random = JavaRandom()
        // Each user leaves a review for each clinic
        userIds.forEach { userId ->
            clinicIds.forEach { clinicId ->
                val rating = random.nextInt(5) + 1 // Ratings between 1 and 5
                val comment = "This is a generated review by user $userId for clinic $clinicId"
                val review = Review(userId = userId,vetId =  clinicId, rating = rating.toString(), comment =  comment)
                reviews.add(review)
            }
        }
        return reviews
    }
    fun generateReviewsForVet(userIds: List<String>, vetIds: List<String>): List<Review> {
        val reviews = mutableListOf<Review>()
        val random = JavaRandom()
        // Each user leaves a review for each clinic
        userIds.forEach { userId ->
            vetIds.forEach { vetId ->
                val rating = random.nextInt(5) + 1 // Ratings between 1 and 5
                val comment = "This is a generated review by user $userId for veterinarian $vetId"
                val review = Review(userId = userId, vetId =  vetId, rating = rating.toString(), comment =  comment)
                reviews.add(review)
            }
        }
        return reviews
    }

    fun writeReviewsToFirestore(reviews: List<Review>) {
        val db = FirebaseFirestore.getInstance()
        reviews.forEach { review ->
            db.collection("reviews").add(review)
                .addOnSuccessListener { documentReference ->
                    val reviewId = documentReference.id
                    println("Review successfully added with ID: $reviewId")
                    // Update the review with the document ID
                    review.reviewId = reviewId
                    // Update the document with the review ID
                    db.collection("reviews").document(reviewId)
                        .set(review)
                        .addOnSuccessListener {
                            println("Review ID successfully updated in Firestore!")
                        }
                        .addOnFailureListener { e ->
                            println("Error updating review ID in Firestore: $e")
                        }
                }
                .addOnFailureListener { e ->
                    println("Error adding review: $e")
                }
        }
    }

    fun createAndUploadReviewsForClinic() {
        val db = FirebaseFirestore.getInstance()
        // Fetch users and clinics
        fetchIds("users") { userIds ->
            fetchIds("veterinary_clinics") { vetIds ->
                // Generate reviews
                val reviews = generateReviewsForVet(userIds, vetIds)
                // Write reviews to Firestore
                writeReviewsToFirestore(reviews)
            }
        }
    }

    fun createAndUploadReviewsForVet() {
        val db = FirebaseFirestore.getInstance()
        // Fetch users and clinics
        fetchIds("users") { userIds ->
            fetchIds("veterinarians") { clinicIds ->
                // Generate reviews
                val reviews = generateReviewsForVet(userIds, clinicIds)
                // Write reviews to Firestore
                writeReviewsToFirestore(reviews)
            }
        }
    }
    fun uploadSvgToFirebase(svgUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference.child("serviceCategories/images/${System.currentTimeMillis()}.svg")

        // Upload SVG file to Firebase Storage
        storageRef.putFile(svgUri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    val svgUrl = uri.toString()
                    println("Successfully Uploaded!! URL: $svgUrl")
                    if (documentIds != null && currentIndex < documentIds!!.size) {
                        updateServiceCategoryImageInFirestore(documentIds!![currentIndex], svgUrl)
                        currentIndex++
                    }
                }
            }
            .addOnFailureListener { exception ->
                println("Error uploading SVG: ${exception.message}")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}