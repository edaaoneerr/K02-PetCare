package com.example.petcareproject.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.petcareproject.R
import com.example.petcareproject.adapter.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator


class OnboardingFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_onboarding, container, false)

        // Initialize your fragments here
        val fragments = arrayListOf<Fragment>(GetStartedFragment(), GetStartedFragment2(), GetStartedFragment3())
        val adapter = ViewPagerAdapter(fragments, requireActivity().supportFragmentManager, lifecycle)
        val viewPager = view.findViewById<ViewPager2>(R.id.view_pager)
        viewPager.adapter = adapter
        val indicator = view.findViewById<SpringDotsIndicator>(R.id.dots_indicator)
        indicator.attachTo(viewPager)
        return view
    }


}