package com.example.petcareproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.petcareproject.R
import com.example.petcareproject.model.ServiceCategory

class ServiceCategoryAdapter(private val serviceCategories: List<ServiceCategory>) : RecyclerView.Adapter<ServiceCategoryAdapter.ServiceCategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceCategoryViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.service_category_item, parent, false)
        return ServiceCategoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ServiceCategoryViewHolder, position: Int) {
        val serviceCategory = serviceCategories[position]
        holder.bind(serviceCategory)
    }

    override fun getItemCount() = serviceCategories.size

    class ServiceCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val serviceCategoryNameTextView: TextView = itemView.findViewById(R.id.serviceCategoryName)
        private val serviceCategoryIcon: ImageView = itemView.findViewById(R.id.serviceCategoryImage) // Reference to an ImageView in your layout

        fun bind(serviceCategory: ServiceCategory) {

            serviceCategoryNameTextView.text = serviceCategory.serviceCategoryName
            loadUrl(serviceCategoryIcon, serviceCategory.serviceCategoryImage)
    }

        fun loadUrl(imageView: ImageView, url: String) {
            val imageLoader = ImageLoader.Builder(imageView.context)
                .components { add(SvgDecoder.Factory()) }
                .build()

            val request = ImageRequest.Builder(imageView.context)
                .data(url)
                .size(30, 40)  // Specify width and height in pixels
                .target(imageView)
                .build()

            imageLoader.enqueue(request)
        }

}}



