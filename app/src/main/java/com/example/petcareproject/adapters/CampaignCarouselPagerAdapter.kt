package com.example.petcareproject.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.example.petcareproject.R



class CampaignCarouselPagerAdapter(private val items: List<Int>, private val context: Context) : PagerAdapter() {

    override fun getCount(): Int {
        return items.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = inflater.inflate(R.layout.campaign_carousel_item, container, false)

        val imageView1 = itemView.findViewById(R.id.campaignView1) as ImageView
        val imageView2 = itemView.findViewById(R.id.campaignView2) as ImageView
        val imageView3 = itemView.findViewById(R.id.campaignView3) as ImageView
        val imageView4 = itemView.findViewById(R.id.campaignView4) as ImageView

        imageView1.setImageResource(items[position])
        imageView2.setImageResource(items[position]) // Set different image resource for imageView2
        imageView3.setImageResource(items[position]) // Set different image resource for imageView3
        imageView4.setImageResource(items[position]) // Set different image resource for imageView4

        // Add the inflated view to the container
        container.addView(itemView)

        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}




