package com.example.petcareproject.views.appointmentview

import HourAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petcareproject.R
import com.example.petcareproject.model.Vet
import com.example.petcareproject.viewmodels.AppointmentViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.util.Calendar

class AppointmentFragment : Fragment() {

    private val viewModel: AppointmentViewModel by viewModels()
    private lateinit var datePicker: MaterialCalendarView
    private lateinit var recyclerViewHours: RecyclerView
    private lateinit var btnConfirm: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_appointment, container, false)
        datePicker = view.findViewById(R.id.calendarView)
        recyclerViewHours = view.findViewById(R.id.recyclerViewHours)
        btnConfirm = view.findViewById(R.id.btnConfirm)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        datePicker.setOnDateChangedListener { widget, date, selected ->
            val calendar = Calendar.getInstance()
            calendar.set(date.year, date.month, date.day)
            viewModel.setDate(calendar.timeInMillis)
        }

        recyclerViewHours.layoutManager = GridLayoutManager(requireContext(), 3)
        val adapter = HourAdapter(viewModel.hours.value ?: emptyList()) { hour ->
            viewModel.setHour(hour)
        }
        recyclerViewHours.adapter = adapter

        viewModel.hours.observe(viewLifecycleOwner, { hours ->
            adapter.notifyDataSetChanged()
        })

        btnConfirm.setOnClickListener {
            viewModel.confirmAppointment()
            if (viewModel.appointmentConfirmed.value == true) {
                val vet = arguments?.getParcelable<Vet>("vet")
                viewModel.saveAppointment(vet?.vetId!!)
                showConfirmationDialog()
            } else {
                Toast.makeText(requireContext(), "Please select a date and hour", Toast.LENGTH_SHORT).show()
            }
        }


    }



    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Congratulations!")
            .setMessage("Your appointment is sent doctor to be confirmed for ${viewModel.getFormattedDate()}, at ${viewModel.getFormattedHour()}.")
            .setPositiveButton("Done") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

}
