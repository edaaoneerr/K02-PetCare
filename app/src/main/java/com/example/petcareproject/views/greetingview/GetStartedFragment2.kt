package com.example.petcareproject.views.greetingview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.viewpager2.widget.ViewPager2
import com.example.petcareproject.R


class GetStartedFragment2 : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_get_started2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /* views.findViewById<Button>(R.id.registerButton).setOnClickListener {
            val action = GetStartedFragment2Directions.actionGetStartedFragment2ToGetStartedFragment3()
            findNavController().navigate(action)
        }*/
        view.findViewById<Button>(R.id.registerButton).setOnClickListener {
            // Assuming you have a ViewPager2 named viewPager in your MainActivity
            val viewPager: ViewPager2? = activity?.findViewById(R.id.view_pager)
            viewPager?.let {
                val currentItem = it.currentItem

                // Check if it's not the last page
                if (currentItem < (it.adapter?.itemCount ?: 1) - 1) {
                    it.currentItem = currentItem + 1
                }
            }
        }
    }



}