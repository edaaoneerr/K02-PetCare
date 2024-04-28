package com.example.petcareproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.petcareproject.R
import com.example.petcareproject.model.ServiceCategory
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class ServiceCategoryAdapter(options: FirestoreRecyclerOptions<ServiceCategory>) :
    FirestoreRecyclerAdapter<ServiceCategory, ServiceCategoryAdapter.ServiceCategoryViewHolder>(options) {

    class ServiceCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewName: TextView = itemView.findViewById(R.id.serviceNameText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceCategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.service_category_item, parent, false)
        return ServiceCategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceCategoryViewHolder, position: Int, model: ServiceCategory) {
        holder.textViewName.text = model.name
    }

}



