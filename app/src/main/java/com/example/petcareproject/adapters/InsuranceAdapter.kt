package com.example.petcareproject.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.petcareproject.R
import com.example.petcareproject.databinding.ItemInsuranceBinding
import com.example.petcareproject.model.InsurancePackage

class InsuranceAdapter(private var insurancePackages: List<InsurancePackage>) : RecyclerView.Adapter<InsuranceAdapter.InsuranceViewHolder>() {

    inner class InsuranceViewHolder(private val binding: ItemInsuranceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(insurancePackage: InsurancePackage) {
            binding.insuranceName.text = insurancePackage.insuranceName
            binding.insurancePrice.text = insurancePackage.insurancePrice

            binding.insuranceDetailsContainer.removeAllViews()
            for (detail in insurancePackage.insuranceDetails) {
                val detailView = LayoutInflater.from(binding.root.context).inflate(
                    R.layout.item_insurance_detail, binding.insuranceDetailsContainer, false
                ) as LinearLayout

                val descriptionView = detailView.findViewById<TextView>(R.id.detail_description)
                val priceView = detailView.findViewById<TextView>(R.id.detail_price)

                val detailParts = detail.split(" - ")
                descriptionView.text = detailParts[0]
                priceView.text = detailParts[1]

                binding.insuranceDetailsContainer.addView(detailView)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsuranceViewHolder {
        val binding = ItemInsuranceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InsuranceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InsuranceViewHolder, position: Int) {
        holder.bind(insurancePackages[position])
    }

    override fun getItemCount(): Int = insurancePackages.size

    fun updateData(newInsurancePackages: List<InsurancePackage>) {
        insurancePackages = newInsurancePackages
        notifyDataSetChanged()
    }
}
