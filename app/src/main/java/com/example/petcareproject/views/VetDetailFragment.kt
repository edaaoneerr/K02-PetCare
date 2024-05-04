package com.example.petcareproject.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.petcareproject.adapters.VetReviewRecyclerAdapter
import com.example.petcareproject.databinding.FragmentVetDetailBinding
import com.example.petcareproject.model.Review
import com.example.petcareproject.model.VeterinaryClinic
import com.google.firebase.firestore.FirebaseFirestore


class VetDetailFragment : Fragment() {
    private var _binding: FragmentVetDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVetDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Receive arguments
        val clinic = arguments?.getParcelable<VeterinaryClinic>("clinic")

        val vetImage = binding.vetClinicImage
        // Populate UI with clinic details
        clinic?.let {
            binding.vetClinicName.text = it.name
            binding.vetClinicRating.text = it.rating
            binding.vetClinicSpecialty.text = it.specialty
            binding.vetClinicDistance.text = it.distance.toString()
            Glide.with(vetImage.context)
                .load(it.imageUrl)
                .into(vetImage)

            clinic.clinicId?.let { it ->
                getReviews(it) { reviews ->
                    val adapter = VetReviewRecyclerAdapter(reviews)
                    binding.vetReviewsRecyclerView.adapter = adapter
                    binding.vetReviewsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                }
            }
        }

    }
    fun getReviews(clinicId: String, callback: (List<Review>) -> Unit) {
        val reviews = mutableListOf<Review>()
        val db = FirebaseFirestore.getInstance()
        db.collection("reviews")
            .whereEqualTo("vetId", clinicId)// Reference the document directly using the clinicId
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                for (document in result.documents) {
                    val userId = document.getString("userId") ?: ""
                    val vetId = document.getString("vetId") ?: ""
                    val rating = document.getString("rating") ?: ""
                    val comment = document.getString("comment") ?: ""

                    val review = Review(userId = userId,vetId =  vetId, rating = rating, comment =  comment)
                    reviews.add(review)
                }
                callback(reviews)
            }
            .addOnFailureListener { exception ->
                println("Error getting reviews: $exception")
                callback(emptyList()) // Pass an empty list to the callback in case of failure
            }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}