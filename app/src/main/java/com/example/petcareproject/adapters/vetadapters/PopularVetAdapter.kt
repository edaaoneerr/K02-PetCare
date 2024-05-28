package com.example.petcareproject.adapters.vetadapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petcareproject.R
import com.example.petcareproject.model.Vet

class PopularVetAdapter(private val vets: List<Vet>, private val listener: OnItemClickListener) : RecyclerView.Adapter<PopularVetAdapter.PopularVetViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(vet: Vet?)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularVetViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.popular_vet_item, parent, false)
        return PopularVetViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PopularVetViewHolder, position: Int) {
        val vet = vets[position]
        holder.bind(vet, listener)
    }

    override fun getItemCount() = vets.size

    class PopularVetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.vetName)
        private val specialtyTextView: TextView = itemView.findViewById(R.id.vetSpecialty)
        private val ratingTextView: TextView = itemView.findViewById(R.id.vetRating)
        private val imageView: ImageView = itemView.findViewById(R.id.vetImage) // Reference to an ImageView in your layout
        private val distanceTextView: TextView = itemView.findViewById(R.id.vetDistance)


        fun bind(vet: Vet, listener: OnItemClickListener) {
            itemView.setOnClickListener { listener.onItemClick(vet) }
            nameTextView.text = vet.vetName
            specialtyTextView.text = vet.vetSpecialty
            ratingTextView.text = vet.vetRating.toString()
            distanceTextView.text = vet.vetDistance

            Glide.with(itemView.context)
                .load(vet.vetImageUrl)
                .into(imageView) // Load the image using Glide
        }

    }
}