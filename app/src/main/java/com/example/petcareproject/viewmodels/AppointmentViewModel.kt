package com.example.petcareproject.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcareproject.model.Appointment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AppointmentViewModel : ViewModel() {

    private val _hours = MutableLiveData<List<String>>()
    val hours: LiveData<List<String>> get() = _hours

    private val _appointmentConfirmed = MutableLiveData<Boolean>()
    val appointmentConfirmed: LiveData<Boolean> get() = _appointmentConfirmed

    private var selectedDate: Long? = null
    private var selectedHour: String? = null

    private val _appointments = MutableLiveData<List<Appointment>>()
    val appointments: LiveData<List<Appointment>> = _appointments

    init {
        _hours.value = listOf(
            "09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM",
            "11:30 AM", "03:00 PM", "03:30 PM", "04:00 PM", "04:30 PM",
            "05:00 PM", "05:30 PM"
        )
    }



   /* private fun fetchAppointments() {
        val db = FirebaseFirestore.getInstance()
        db.collection("appointments")
            .get()
            .addOnSuccessListener { result ->
                val appointmentList = result.map { document ->
                    document.toObject(Appointment::class.java)
                }
                _appointments.value = appointmentList
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: ${exception.message}")
            }
    }
*/
    fun getBookingsForTab(tabTitle: String): LiveData<List<Appointment>> {
        val filteredAppointments = MutableLiveData<List<Appointment>>()
        _appointments.observeForever { allAppointments ->
            println(allAppointments)
            val filtered = when (tabTitle) {
                "Upcoming" -> allAppointments.filter { it.appointmentDate?.toDate()?.after(Date()) == true && it.isActive }
                "Completed" -> allAppointments.filter { it.appointmentDate?.toDate()?.before(Date()) == true && it.isActive }
                "Canceled" -> allAppointments.filter { !it.isActive }
                else -> emptyList()
            }
            filteredAppointments.value = filtered
        }
        return filteredAppointments
    }

    fun setDate(date: Long) {
        selectedDate = date
    }

    fun setHour(hour: String) {
        selectedHour = hour
    }

    fun confirmAppointment() {
        if (selectedDate != null && selectedHour != null) {
            _appointmentConfirmed.value = true
        }
    }

    fun getFormattedDate(): String {
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(Date(selectedDate ?: 0))
    }


    fun getFormattedHour(): String {
        return selectedHour ?: ""
    }

    /*fun fetchAppointments() {
        val db = FirebaseFirestore.getInstance()
        db.collection("appointments")
            .get()
            .addOnSuccessListener { result ->
                val appointments = mutableListOf<Appointment>()
                viewModelScope.launch { // Use lifecycleScope to launch coroutines in a Fragment
                    result.forEach { document ->
                        println(document)
                        try {
                            val appointmentId = document.id
                            val vetId = document.getString("vetId") ?: ""
                            val userId = document.getString("userId") ?: ""
                            val appointmentDate = document.getTimestamp("appointmentDate")
                            val hasReview = document.getBoolean("hasReview")
                            val isRescheduled = document.getBoolean("isRescheduled")
                            val isActive = document.getBoolean("isActive")
                         //   val isApproved = document.getBoolean("isApproved")

                            val appointment = Appointment(appointmentId, vetId, userId, appointmentDate, hasReview!!, isRescheduled!!, isActive!!)
                            appointments.add(appointment)
                                if (appointments.size == result.size()) {
                                    updateAppointments(appointments)
                                }

                        } catch (e: Exception) {
                            println("Document null: ${e.message}")
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }

    fun updateAppointments(appointmentList: List<Appointment>) {
        _appointments.postValue(appointmentList)
    }*/
    fun fetchAppointments() {
        val db = FirebaseFirestore.getInstance()
        db.collection("appointments")
            .get()
            .addOnSuccessListener { result ->
                val appointments = mutableListOf<Appointment>()
                viewModelScope.launch {
                    result.forEach { document ->
                        val appointmentId = document.id
                        val vetId = document.getString("vetId") ?: ""
                        val userId = document.getString("userId") ?: ""
                        val appointmentDate = document.getTimestamp("appointmentDate")
                        val hasReview = document.getBoolean("hasReview")
                        val isRescheduled = document.getBoolean("isRescheduled")
                        val isActive = document.getBoolean("isActive")

                        val appointment = Appointment(
                            appointmentId,
                            vetId,
                            userId,
                            appointmentDate,
                            hasReview!!,
                            isRescheduled!!,
                            isActive!!
                        )
                        println("VIEWMODEL" + appointment.isActive)
                        appointments.add(appointment)
                    }
                    updateAppointments(appointments)
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }

    fun updateAppointments(appointmentList: List<Appointment>) {
        _appointments.postValue(appointmentList)
    }


    fun saveAppointment(vetId: String) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDate ?: 0
        val timeParts = selectedHour?.split(":") ?: listOf("0", "0")
        val amPm = timeParts[1].split(" ")[1]
        var hour = timeParts[0].toInt()
        val minute = timeParts[1].split(" ")[0].toInt()
        if (amPm == "PM" && hour != 12) hour += 12
        if (amPm == "AM" && hour == 12) hour = 0
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        val timestamp = Timestamp(calendar.time)

        val db = FirebaseFirestore.getInstance()
        val appointmentRef = db.collection("appointments").document()

        val appointment = hashMapOf(
            "appointmentId" to appointmentRef.id,
            "userId" to FirebaseAuth.getInstance().currentUser?.uid,
            "vetId" to vetId,
            "appointmentDate" to timestamp,
            "createdAt" to Timestamp.now(),
            "isActive" to true,
            "isRescheduled" to false,
            "hasReview" to false,
        )
        appointmentRef.set(appointment)
            .addOnSuccessListener {
                println("Appointment saved successfully with ID: ${appointmentRef.id}")
            }
            .addOnFailureListener { e ->
                println("Couldn't save appointment: ${e.message}")
            }
    }




}
