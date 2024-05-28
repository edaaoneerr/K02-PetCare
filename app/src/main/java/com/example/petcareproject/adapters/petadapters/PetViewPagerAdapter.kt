package com.example.petcareproject.adapters.petadapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.petcareproject.databinding.ItemPetProfileBinding
import com.example.petcareproject.model.Pet

class PetViewPagerAdapter( private val petProfiles: List<Pet>
) : RecyclerView.Adapter<PetViewPagerAdapter.PetProfileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetProfileViewHolder {
        val binding =
            ItemPetProfileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PetProfileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PetProfileViewHolder, position: Int) {
        holder.bind(petProfiles[position])
    }

    override fun getItemCount(): Int = petProfiles.size

    inner class PetProfileViewHolder(private val binding: ItemPetProfileBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(pet: Pet) {
            binding.petName.text = pet.petName
            binding.petType.text = "${pet.petType}  |  ${pet.petBreed}"
            binding.executePendingBindings()
        }
    }
}