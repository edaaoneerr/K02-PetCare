package com.example.petcareproject.views.vetview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.petcareproject.R
import com.example.petcareproject.adapters.vetadapters.VetReviewRecyclerAdapter
import com.example.petcareproject.databinding.FragmentVetDetailBinding
import com.example.petcareproject.model.Review
import com.example.petcareproject.model.Vet
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
        val vet = arguments?.getParcelable<Vet>("vet")

        val vetImage = binding.vetImage
        // Populate UI with clinic details
        vet?.let {
            binding.vetName.text = it.vetName
            binding.vetRating.text = it.vetRating.toString()
            binding.vetSpecialty.text = it.vetSpecialty
            binding.vetDistance.text = it.vetDistance.toString()
            binding.vetExperienceYearsCount.text = it.vetExperienceYears.toInt().toString()
            binding.vetPatientCount.text = it.vetPatientCount.toString()
            binding.vetRatingCount.text = it.vetRating.toString()
            binding.vetReviewCount.text = it.vetReviewCount.toString()
            binding.vetAbout.text = it.vetAbout

            val dayOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
            val sortedWorkingHours = it.vetWorkingTime.entries.sortedBy { entry ->
                dayOrder.indexOf(entry.key)
            }.joinToString("\n") { "${it.key}: ${it.value}" }
            binding.vetWorkingTime.text = sortedWorkingHours

            Glide.with(vetImage.context)
                .load(it.vetImageUrl)
                .into(vetImage)

            getReviews(vet.clinicId) { reviews ->
                val adapter = VetReviewRecyclerAdapter(reviews)
                binding.vetReviewsRecyclerView.adapter = adapter
                binding.vetReviewsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            }
        }

        binding.bookAppointmentButton.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable("vet", vet)
            }
            findNavController().navigate(R.id.action_vetDetailFragment_to_appointmentFragment, bundle)
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