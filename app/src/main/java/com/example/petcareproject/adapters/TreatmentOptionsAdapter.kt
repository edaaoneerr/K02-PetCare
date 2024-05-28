package com.example.petcareproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.petcareproject.R

data class TreatmentOption(val iconResId: Int, val label: String)

class TreatmentOptionsAdapter(
    private val treatmentOptions: List<TreatmentOption>,
    private val onOptionSelected: (String) -> Unit
) : RecyclerView.Adapter<TreatmentOptionsAdapter.TreatmentOptionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TreatmentOptionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_treatment_option, parent, false)
        return TreatmentOptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TreatmentOptionViewHolder, position: Int) {
        val treatmentOption = treatmentOptions[position]
        holder.icon.setImageResource(treatmentOption.iconResId)
        holder.label.text = treatmentOption.label

        holder.itemView.setOnClickListener {
            onOptionSelected(treatmentOption.label)
        }

        holder.addButton.setOnClickListener {
            onOptionSelected(treatmentOption.label)
        }
    }

    override fun getItemCount(): Int {
        return treatmentOptions.size
    }

    class TreatmentOptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val label: TextView = itemView.findViewById(R.id.label)
        val addButton: ImageView = itemView.findViewById(R.id.add_button)
    }
}
