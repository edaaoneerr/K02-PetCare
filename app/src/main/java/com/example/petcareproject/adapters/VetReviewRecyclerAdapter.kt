package com.example.petcareproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petcareproject.R
import com.example.petcareproject.model.Review
import com.google.firebase.firestore.FirebaseFirestore

class VetReviewRecyclerAdapter(private val reviews: List<Review>) : RecyclerView.Adapter<VetReviewRecyclerAdapter.VetReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VetReviewViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.vet_review_item, parent, false)
        return VetReviewViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: VetReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.bind(review)
    }

    override fun getItemCount() = reviews.size

    class VetReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val reviewerNameTextView: TextView = itemView.findViewById(R.id.reviewerName)
        private val reviewTextView: TextView = itemView.findViewById(R.id.reviewText)
        private val reviewRatingBar: RatingBar = itemView.findViewById(R.id.reviewRatingBar)
        private val imageView: ImageView = itemView.findViewById(R.id.reviewerProfileImage) // Reference to an ImageView in your layout

        fun bind(review: Review) {
            reviewTextView.text = review.comment
            reviewRatingBar.rating = review.rating.toFloat()

            // Fetch user details dynamically
            val userDocRef = FirebaseFirestore.getInstance().collection("users").document(review.userId)
            userDocRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val userName = document.getString("name") ?: "No Name"
                    val userProfileImage = document.getString("userProfileImage") ?: ""
                    reviewerNameTextView.text = userName

                    Glide.with(itemView.context)
                        .load(userProfileImage)
                        .into(imageView)
                }
            }.addOnFailureListener {
                // Handle any errors, such as network issues
                reviewerNameTextView.text = "User Name Unavailable"
                imageView.setImageResource(R.drawable.ic_profile)
            }
    }
}
}
