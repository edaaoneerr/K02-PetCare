package com.example.petcareproject.views

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.petcareproject.databinding.FragmentProfileBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream


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


       binding.btnUploadImages.setOnClickListener {
             pickImageAndUpload()
         }
         fetchDocumentIds()


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
    private fun fetchDocumentIds() {
        val db = FirebaseFirestore.getInstance()
        db.collection("veterinary_clinics").get().addOnCompleteListener { task ->
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

        val storageRef = FirebaseStorage.getInstance().reference.child("veterinary_clinics/images/${System.currentTimeMillis()}.jpg")
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
        val clinicRef = db.collection("veterinary_clinics").document(clinicId)

        clinicRef.update("clinic_image", imageUrl)
            .addOnSuccessListener {
                println("DocumentSnapshot successfully updated with image URL!")
            }
            .addOnFailureListener { e ->
                println("Error updating document: $e")
            }
    }



}