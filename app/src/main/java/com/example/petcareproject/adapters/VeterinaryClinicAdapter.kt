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


class VeterinaryClinicAdapter(private val clinics: List<VeterinaryClinic>, private val listener: OnItemClickListener) : RecyclerView.Adapter<VeterinaryClinicAdapter.VeterinaryClinicViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(clinic: VeterinaryClinic?)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VeterinaryClinicViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.vet_clinic_item, parent, false)
        return VeterinaryClinicViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: VeterinaryClinicViewHolder, position: Int) {
        val clinic = clinics[position]
        holder.bind(clinic, listener)
    }

    override fun getItemCount() = clinics.size

    class VeterinaryClinicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.vetClinicName)
        private val specialtyTextView: TextView = itemView.findViewById(R.id.vetClinicSpecialty)
        private val ratingTextView: TextView = itemView.findViewById(R.id.vetClinicRating)
        private val distanceTextView: TextView = itemView.findViewById(R.id.vetClinicDistance)
        private val imageView: ImageView = itemView.findViewById(R.id.vetClinicImage) // Reference to an ImageView in your layout


        fun bind(clinic: VeterinaryClinic, listener: OnItemClickListener) {
            itemView.setOnClickListener { listener.onItemClick(clinic) }
            nameTextView.text = clinic.name
            specialtyTextView.text = clinic.specialty
            ratingTextView.text = clinic.rating
            distanceTextView.text = clinic.distance.toString() + " km"

            Glide.with(itemView.context)
                .load(clinic.imageUrl)
                .into(imageView) // Load the image using Glide
        }

    }
}

