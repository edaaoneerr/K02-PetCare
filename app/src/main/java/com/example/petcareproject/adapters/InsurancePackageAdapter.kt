package com.example.petcareproject.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.petcareproject.databinding.ItemInsurancePackageBinding
import com.example.petcareproject.model.InsurancePackage

class InsurancePackageAdapter(
    private val packages: List<InsurancePackage>,
    private val onPackageSelected: (InsurancePackage) -> Unit
) : RecyclerView.Adapter<InsurancePackageAdapter.PackageViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageViewHolder {
        val binding = ItemInsurancePackageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PackageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PackageViewHolder, position: Int) {
        val insurancePackage = packages[position]
        holder.bind(insurancePackage, position == selectedPosition)

        holder.itemView.setOnClickListener {
            onPackageSelected(insurancePackage)
            notifyItemChanged(selectedPosition)
            selectedPosition = holder.adapterPosition
            notifyItemChanged(selectedPosition)
        }
    }

    override fun getItemCount() = packages.size

    class PackageViewHolder(private val binding: ItemInsurancePackageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(insurancePackage: InsurancePackage, isSelected: Boolean) {
            binding.packageName.text = insurancePackage.insuranceName
            binding.packagePrice.text = insurancePackage.insurancePrice
            binding.root.isSelected = isSelected
        }
    }
}
