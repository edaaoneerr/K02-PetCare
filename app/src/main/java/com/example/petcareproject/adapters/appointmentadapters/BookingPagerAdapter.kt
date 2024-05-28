package com.example.petcareproject.adapters.appointmentadapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.petcareproject.model.Appointment
import com.example.petcareproject.viewmodels.AppointmentViewModel
import com.example.petcareproject.views.appointmentview.BookingListFragment


class BookingsPagerAdapter(fragment: Fragment, private val viewModel: AppointmentViewModel) : FragmentStateAdapter(fragment) {

    private val tabTitles = arrayOf("Upcoming", "Completed", "Canceled")

    override fun getItemCount(): Int = tabTitles.size

    override fun createFragment(position: Int): Fragment {
        return BookingListFragment.newInstance(tabTitles[position])
    }

    fun updateBookings(bookings: List<Appointment>) {
        viewModel.updateAppointments(bookings)
    }
}

