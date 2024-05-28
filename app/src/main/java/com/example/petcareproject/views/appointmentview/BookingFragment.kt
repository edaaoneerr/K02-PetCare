package com.example.petcareproject.views.appointmentview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.petcareproject.adapters.appointmentadapters.BookingsPagerAdapter
import com.example.petcareproject.databinding.FragmentBookingBinding
import com.example.petcareproject.viewmodels.AppointmentViewModel
import com.google.android.material.tabs.TabLayoutMediator

class BookingFragment : Fragment() {

    private val viewModel: AppointmentViewModel by viewModels()
    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPagerAndTabs()
        observeViewModel()
    }

    private fun setupViewPagerAndTabs() {
        val adapter = BookingsPagerAdapter(this, viewModel)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Upcoming"
                1 -> "Completed"
                2 -> "Canceled"
                else -> null
            }
        }.attach()
    }

    private fun observeViewModel() {
        viewModel.appointments.observe(viewLifecycleOwner) { bookings ->
            val adapter = binding.viewPager.adapter as? BookingsPagerAdapter
            // Update child fragments with the new data
            adapter?.updateBookings(bookings)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
