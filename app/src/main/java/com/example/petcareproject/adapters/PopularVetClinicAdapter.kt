package com.example.petcareproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petcareproject.R
import com.example.petcareproject.model.VeterinaryClinic


class PopularVeterinaryClinicAdapter(private val clinics: List<VeterinaryClinic>) : RecyclerView.Adapter<PopularVeterinaryClinicAdapter.PopularVeterinaryClinicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularVeterinaryClinicViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.popular_vet_clinic_item, parent, false)
        return PopularVeterinaryClinicViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PopularVeterinaryClinicViewHolder, position: Int) {
        val clinic = clinics[position]
        holder.bind(clinic)
    }

    override fun getItemCount() = clinics.size

    class PopularVeterinaryClinicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.vetClinicName)
        private val specialtyTextView: TextView = itemView.findViewById(R.id.vetClinicSpecialty)
        private val ratingTextView: TextView = itemView.findViewById(R.id.vetClinicRating)
        private val distanceTextView: TextView = itemView.findViewById(R.id.vetClinicDistance)
        private val imageView: ImageView = itemView.findViewById(R.id.vetClinicImage) // Reference to an ImageView in your layout


        fun bind(clinic: VeterinaryClinic) {
            nameTextView.text = clinic.name
            specialtyTextView.text = clinic.specialty
            ratingTextView.text = clinic.rating
            distanceTextView.text = clinic.distance.toString()

            Glide.with(itemView.context)
                .load(clinic.imageUrl)
                .into(imageView) // Load the image using Glide
        }

        }
}