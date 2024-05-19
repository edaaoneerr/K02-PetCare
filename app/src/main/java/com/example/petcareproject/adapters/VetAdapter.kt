package com.example.petcareproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petcareproject.R
import com.example.petcareproject.model.Vet

class VetAdapter(private val vets: List<Vet>, private val listener: OnItemClickListener) : RecyclerView.Adapter<VetAdapter.VetViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(vet: Vet?)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VetViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.vet_item, parent, false)
        return VetViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: VetViewHolder, position: Int) {
        val vet = vets[position]
        holder.bind(vet, listener)
    }

    override fun getItemCount() = vets.size

    class VetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.vetName)
        private val specialtyTextView: TextView = itemView.findViewById(R.id.vetSpecialty)
        private val ratingTextView: TextView = itemView.findViewById(R.id.vetRating)
      //  private val clinicNameTextView: TextView = itemView.findViewById(R.id.vetClinicDistance)
        private val imageView: ImageView = itemView.findViewById(R.id.vetImage) // Reference to an ImageView in your layout


        fun bind(vet: Vet, listener: OnItemClickListener) {
            itemView.setOnClickListener { listener.onItemClick(vet) }
            nameTextView.text = vet.vetName
            specialtyTextView.text = vet.vetSpecialty
            ratingTextView.text = vet.vetRating.toString()
         //   clinicNameTextView.text = vet.vetClinicName

            Glide.with(itemView.context)
                .load(vet.vetImageUrl)
                .into(imageView) // Load the image using Glide
        }

    }
}
