package com.example.petcareproject.view

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.petcareproject.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewPager = findViewById<ViewPager2>(R.id.view_pager)
        val tabLayout: TabLayout = findViewById(R.id.tab_layout)

        // Initialize your fragments here
        val fragments = listOf(GetStartedFragment(), GetStartedFragment2(), GetStartedFragment3())

        // Setting up the adapter for ViewPager2
        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }
        viewPager.isUserInputEnabled = false
        viewPager.adapter = adapter

        // Attaching tablayout with viewpager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // Here you can customize your tab layout. For example:
           // tab.text = "Tab ${position + 1}"
        }.attach()
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {

        return super.onCreateView(name, context, attrs)
    }


}