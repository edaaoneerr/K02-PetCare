package com.example.petcareproject.views

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.petcareproject.databinding.FragmentProfileBinding
import com.example.petcareproject.model.Review
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.Random


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var documentIds: List<String>? = null
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       // createAndUploadReviews()

       binding.btnUploadImages.setOnClickListener {
             pickImageAndUpload()
         }
        fetchDocumentIds()
    }

    fun pickImageAndUpload() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1)
    }

  /*  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
    }*/
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
      super.onActivityResult(requestCode, resultCode, data)
      if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
          val svgUri = data.data
          if (svgUri != null) {
              uploadSvgToFirebase(svgUri)
          }
      }
  }
    private fun fetchDocumentIds() {
        val db = FirebaseFirestore.getInstance()
        db.collection("service_categories").get().addOnCompleteListener { task ->
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

    fun uploadImageToFirebase(bitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val storageRef = FirebaseStorage.getInstance().reference.child("serviceCategories/images/${System.currentTimeMillis()}.jpg")
        val uploadTask = storageRef.putBytes(data)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                println("Successfully Uploaded!! URL: $imageUrl")
                if (documentIds != null && currentIndex < documentIds!!.size) {
                    updateClinicImageInFirestore(documentIds!![currentIndex], imageUrl)
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

    fun generateReviews(userIds: List<String>, clinicIds: List<String>): List<Review> {
        val reviews = mutableListOf<Review>()
        val random = Random()
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

    fun createAndUploadReviews() {
        val db = FirebaseFirestore.getInstance()
        // Fetch users and clinics
        fetchIds("users") { userIds ->
            fetchIds("veterinary_clinics") { clinicIds ->
                // Generate reviews
                val reviews = generateReviews(userIds, clinicIds)
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





}