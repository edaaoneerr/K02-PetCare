package com.example.petcareproject.views.appointmentview
import com.example.petcareproject.adapters.appointmentadapters.BookingAdapter
import CustomDatePickerFragment
import CustomTimePickerFragment
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petcareproject.R
import com.example.petcareproject.databinding.FragmentBookingListBinding
import com.example.petcareproject.model.Appointment
import com.example.petcareproject.model.Review
import com.example.petcareproject.model.Vet
import com.example.petcareproject.viewmodels.AppointmentViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BookingListFragment : Fragment() {

    companion object {
        private const val ARG_TAB_TITLE = "tab_title"

        fun newInstance(tabTitle: String): BookingListFragment {
            val fragment = BookingListFragment()
            val args = Bundle()
            args.putString(ARG_TAB_TITLE, tabTitle)
            fragment.arguments = args
            return fragment
        }
    }

    private val viewModel: AppointmentViewModel by viewModels()
    private var _binding: FragmentBookingListBinding? = null
    private val binding get() = _binding!!
    private var tabTitle: String? = null
    private lateinit var adapter: BookingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tabTitle = arguments?.getString(ARG_TAB_TITLE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBookingListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.fetchAppointments()
        binding.recyclerViewBookings.layoutManager = LinearLayoutManager(requireContext())
        tabTitle?.let { currentTab ->
            adapter = BookingAdapter(
                viewModel.appointments.value ?: emptyList(),
                currentTab,
                this::onCancelClick,
                this::onRescheduleClick,
                this::onRebookClick,
                this::onAddReviewClick
            )
            binding.recyclerViewBookings.adapter = adapter

            viewModel.appointments.observe(viewLifecycleOwner) { bookings ->
                adapter.updateBookings(bookings.filter {
                    when (currentTab) {
                        "Upcoming" -> it.isActive && it.appointmentDate!!.toDate().after(Date())
                        "Completed" -> it.isActive && it.appointmentDate!!.toDate().before(Date())
                        "Canceled" -> !it.isActive
                        else -> false
                    }
                })
            }
        } ?: run {
            println("Error: tabTitle is null")
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchAppointments()
    }

    private fun onRebookClick(booking: Appointment, callback: () -> Unit) {
        checkExistingAppointmentsAndRebook(booking, "Confirm Rebooking") {
            fetchVeterinarian(booking.vetId) { vet ->
                vet?.let {
                    val datePickerFragment = CustomDatePickerFragment().apply {
                        listener = object : CustomDatePickerFragment.DatePickerListener {
                            override fun onDateSelected(year: Int, month: Int, dayOfMonth: Int) {
                                val calendar = Calendar.getInstance()
                                calendar.set(year, month, dayOfMonth)
                                val selectedDate = calendar.time
                                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                                val dayString = getDayString(dayOfWeek)
                                val workingHours = vet.vetWorkingTime[dayString]
                                if (workingHours != null) {
                                    fetchBookedSlots(vet.vetId!!, selectedDate) { bookedSlots ->
                                        val (startHour, endHour) = parseWorkingHours(workingHours)
                                        showCustomTimePicker(startHour, endHour, bookedSlots) { hourOfDay, minute ->
                                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                            calendar.set(Calendar.MINUTE, minute)
                                            val newTimestamp = Timestamp(calendar.time)
                                            isTimeSlotAvailable(booking.vetId, newTimestamp) { available ->
                                                if (available) {
                                                    saveRedatedAppointment(booking.vetId, newTimestamp)
                                                   /* fetchUpdatedAppointments()
                                                    viewModel.updateAppointments(mutableListOf(booking))
                                                    updateAdapterForTabs(newTimestamp, booking)*/
                                                    callback()
                                                } else {
                                                    Toast.makeText(requireContext(), "This time slot is already booked.", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Toast.makeText(requireContext(), "Vet is not available on this day", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                    datePickerFragment.show(parentFragmentManager, "datePicker")
                }
            }
        }
    }

    private fun updateAdapterForTabs(newTimestamp: Timestamp, booking: Appointment) {
        tabTitle?.let { currentTab ->
            if (currentTab == "Canceled") {
                booking.appointmentDate = newTimestamp
                booking.isActive = true
                viewModel.appointments.value?.filter { it.appointmentId != booking.appointmentId }
                    ?.let { viewModel.updateAppointments(it) }
                viewModel.appointments.value?.plus(booking)
                    ?.let { viewModel.updateAppointments(it) }
            }
        }
    }





    private fun showCustomTimePicker(startHour: Int, endHour: Int, bookedSlots: List<Pair<Int, Int>>, onTimeSelected: (Int, Int) -> Unit) {
        val timePickerFragment = CustomTimePickerFragment().apply {
            listener = object : CustomTimePickerFragment.TimePickerListener {
                override fun onTimeSelected(hourOfDay: Int, minute: Int) {
                    val isWithinWorkingHours = if (endHour < startHour) {
                        hourOfDay >= startHour || hourOfDay < endHour
                    } else {
                        hourOfDay in startHour until endHour
                    }

                    if (isWithinWorkingHours) {
                        onTimeSelected(hourOfDay, minute)
                    } else {
                        Toast.makeText(requireContext(), "Selected time is outside of working hours", Toast.LENGTH_LONG).show()
                    }
                }
            }
            this.startHour = startHour
            this.endHour = endHour
            this.bookedSlots = bookedSlots
        }

        timePickerFragment.show(parentFragmentManager, "timePicker")
    }



    private fun fetchBookedSlots(vetId: String, date: Date, callback: (List<Pair<Int, Int>>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val startOfDay = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val endOfDay = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        db.collection("appointments")
            .whereEqualTo("vetId", vetId)
            .whereGreaterThanOrEqualTo("appointmentDate", Timestamp(startOfDay))
            .whereLessThanOrEqualTo("appointmentDate", Timestamp(endOfDay))
            .get()
            .addOnSuccessListener { documents ->
                val bookedSlots = documents.map {
                    val appointmentDate = it.getTimestamp("appointmentDate")!!.toDate()
                    Pair(appointmentDate.hours, appointmentDate.minutes)
                }
                println("Fetched Booked Slots: $bookedSlots") // Debugging output
                callback(bookedSlots)
            }
            .addOnFailureListener { exception ->
                println("Error fetching booked slots: ${exception.message}")
                callback(emptyList()) // On failure, return an empty list
            }
    }




    private fun isTimeSlotAvailable(vetId: String, timestamp: Timestamp, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("appointments")
            .whereEqualTo("vetId", vetId)
            .whereEqualTo("appointmentDate", timestamp)
            .get()
            .addOnSuccessListener { documents ->
                callback(documents.isEmpty()) // If no documents are found, the slot is available
            }
            .addOnFailureListener { exception ->
                println("Error checking time slot availability: ${exception.message}")
                callback(false) // On failure, assume the slot is not available
            }
    }


    private fun checkExistingAppointmentsAndRebook(booking: Appointment, title: String, onConfirm: () -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        db.collection("appointments")
            .whereEqualTo("userId", userId)
            .whereEqualTo("vetId", booking.vetId)
            .whereEqualTo("isActive",true)
            .whereGreaterThan("appointmentDate", Timestamp.now())
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    val vetName = documents.first().getString("vetName") ?: "this vet"
                    val appointmentDate = documents.first().getTimestamp("appointmentDate") ?: Timestamp.now()
                    showConfirmationDialog(vetName, appointmentDate, title) {
                        onConfirm()
                    }
                } else {
                    onConfirm()
                }
            }
            .addOnFailureListener { exception ->
                println("Error checking existing appointments: ${exception.message}")
                onConfirm() // Proceed even if there's an error
            }
    }

    private fun showConfirmationDialog(vetName: String, appointmentDate: Timestamp, title: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage("You already have an appointment with $vetName on ${formatTimestampToDate(appointmentDate)}. Are you sure you want to schedule another appointment?")
            .setPositiveButton("Yes") { _, _ ->
                onConfirm()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    fun formatTimestampToDate(timestamp: Timestamp): String {
        val date = timestamp.toDate()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun getDayString(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            Calendar.SUNDAY -> "Sunday"
            else -> ""
        }
    }

    private fun parseWorkingHours(workingHours: String): Pair<Int, Int> {
        println("Parsing working hours: $workingHours")
        val parts = workingHours.replace(".", ":").split("-")
        val startHour = convertTo24Hour(parts[0].trim())
        val endHour = convertTo24Hour(parts[1].trim())
        println("Parsed start hour: $startHour, end hour: $endHour")
        return Pair(startHour, endHour)
    }

    private fun convertTo24Hour(time: String): Int {
        println("Converting time: $time")
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = dateFormat.parse(time)
        val calendar = Calendar.getInstance()
        calendar.time = date
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        println("Converted time to hour of day: $hourOfDay")
        return hourOfDay
    }


    private fun rescheduleAppointment(booking: Appointment, newTimestamp: Timestamp, callback: () -> Unit) {
        val db = FirebaseFirestore.getInstance()

        // Create a new appointment
        /*     val newAppointmentRef = db.collection("appointments").document()
             val newAppointment = hashMapOf(
                 "appointmentId" to newAppointmentRef.id,
                 "userId" to booking.userId,
                 "vetId" to booking.vetId,
                 "appointmentDate" to newTimestamp,
                 "createdAt" to Timestamp.now(),
                 "hasReview" to false,
                 "isRescheduled" to true,
                 "isActive" to true,
             )*/

        // Update the previous appointment to mark it as rescheduled
        val oldAppointmentRef = db.collection("appointments").document(booking.appointmentId!!)

        oldAppointmentRef.update("appointmentDate", newTimestamp, "isRescheduled", true)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Appointment rescheduled successfully", Toast.LENGTH_LONG).show()
                callback()
                // Add the new appointment
                /*newAppointmentRef.set(newAppointment)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Appointment rescheduled successfully", Toast.LENGTH_LONG).show()
                        callback()
                    }
                    .addOnFailureListener { e ->
                        println("Couldn't save new appointment: ${e.message}")
                    }*/
            }
            .addOnFailureListener { e ->
                println("Couldn't update appointment: ${e.message}")
            }
    }

    private fun fetchVeterinarian(vetId: String, callback: (Vet?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("veterinarians")
            .whereEqualTo("vetId", vetId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    val document = documents.first()
                    val vet = document.toObject(Vet::class.java)
                    callback(vet)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                println("Error fetching veterinarian: ${exception.message}")
                callback(null)
            }
    }

    private fun saveRedatedAppointment(vetId: String, newTimestamp: Timestamp) {
        val db = FirebaseFirestore.getInstance()
        val appointmentRef = db.collection("appointments").document()

        val appointment = hashMapOf(
            "appointmentId" to appointmentRef.id,
            "userId" to FirebaseAuth.getInstance().currentUser?.uid,
            "vetId" to vetId,
            "appointmentDate" to newTimestamp,
            "createdAt" to Timestamp.now(),
            "isActive" to true, //isCancelled or Active
            "hasReview" to false, //hasReview or Not
            "isRescheduled" to false, //rescheduled before?
        )

        appointmentRef.set(appointment)
            .addOnSuccessListener {
                println("APPOINTMENT REDATE")
                println("Appointment saved successfully with ID: ${appointmentRef.id}")
                viewModel.fetchAppointments()
            }
            .addOnFailureListener { e ->
                println("Couldn't save appointment: ${e.message}")
            }
    }

    private fun onAddReviewClick(booking: Appointment, callback: () -> Unit) {
        // Open a dialog to add a review
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_review, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add Review")
            .setView(dialogView)
            .setPositiveButton("Submit") { _, _ ->
                val rating = dialogView.findViewById<RatingBar>(R.id.ratingBar).rating.toString()
                val comment = dialogView.findViewById<EditText>(R.id.commentEditText).text.toString()
                val db = FirebaseFirestore.getInstance()
                val reviewsCollection = db.collection("reviews")
                val reviewRef = reviewsCollection.document() // This generates a new document ID within the "reviews" collection

                val review = Review(
                    reviewId = reviewRef.id,
                    userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    vetId = booking.vetId,
                    rating = rating,
                    comment = comment
                )

                saveReview(review, booking)
                callback()
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun saveReview(review: Review, booking: Appointment) {
        val db = FirebaseFirestore.getInstance()
        val reviewRef = db.collection("reviews").document()

        review.reviewId = reviewRef.id
        reviewRef.set(review)
            .addOnSuccessListener {
                println("Review saved successfully with ID: ${reviewRef.id}")
            }
            .addOnFailureListener { e ->
                println("Couldn't save review: ${e.message}")
            }

        updateAppointmentField(booking.appointmentId!!,"hasReview", true)
    }

    private fun onCancelClick(booking: Appointment, callback: () -> Unit) {
        updateAppointmentIsActive(booking.appointmentId!!, false, {
            Toast.makeText(requireContext(), "Appointment canceled successfully", Toast.LENGTH_LONG).show()
           /* viewModel.updateAppointments(mutableListOf(booking))
            fetchUpdatedAppointments()*/
            callback()
        }, { e ->
            println("Couldn't cancel appointment: ${e.message}")
        })
    }

    private fun updateAppointmentIsActive(appointmentId: String, isActive: Boolean, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val appointmentRef = db.collection("appointments").document(appointmentId)

        appointmentRef.update("isActive", isActive)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }



    /*    private fun onRescheduleClick(booking: Appointment, callback: () -> Unit) {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val timePickerDialog = TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    val newTimestamp = Timestamp(calendar.time)
                    // Create a new appointment and update the previous one
                    rescheduleAppointment(booking, newTimestamp, callback)
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)
                timePickerDialog.show()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.show()
        }*/

    private fun onRescheduleClick(booking: Appointment, callback: () -> Unit) {
        checkExistingAppointmentsAndRebook(booking, "Confirm Rescheduling") {
            fetchVeterinarian(booking.vetId) { vet ->
                vet?.let {
                    val datePickerFragment = CustomDatePickerFragment().apply {
                        listener = object : CustomDatePickerFragment.DatePickerListener {
                            override fun onDateSelected(year: Int, month: Int, dayOfMonth: Int) {
                                val calendar = Calendar.getInstance()
                                calendar.set(year, month, dayOfMonth)
                                val selectedDate = calendar.time
                                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                                val dayString = getDayString(dayOfWeek)
                                val workingHours = vet.vetWorkingTime[dayString]
                                if (workingHours != null) {
                                    fetchBookedSlots(vet.vetId!!, selectedDate) { bookedSlots ->
                                        val (startHour, endHour) = parseWorkingHours(workingHours)
                                        showCustomTimePicker(startHour, endHour, bookedSlots) { hourOfDay, minute ->
                                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                            calendar.set(Calendar.MINUTE, minute)
                                            val newTimestamp = Timestamp(calendar.time)
                                            isTimeSlotAvailable(booking.vetId, newTimestamp) { available ->
                                                if (available) {
                                                    rescheduleAppointment(booking, newTimestamp, callback)
                                                    fetchUpdatedAppointments()
                                                    updateBookings(mutableListOf(booking))
                                                    callback()
                                                } else {
                                                    Toast.makeText(requireContext(), "This time slot is already booked.", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Toast.makeText(requireContext(), "Vet is not available on this day", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                    datePickerFragment.show(parentFragmentManager, "datePicker")
                }
            }
        }
    }


    private fun updateAppointment(appointmentId: String, newTimestamp: Timestamp) {
        val db = FirebaseFirestore.getInstance()
        val appointmentRef = db.collection("appointments").document(appointmentId)

        appointmentRef.update("appointmentDate", newTimestamp)
            .addOnSuccessListener {

                println("Appointment rescheduled successfully")
            }
            .addOnFailureListener { e ->
                println("Couldn't reschedule appointment: ${e.message}")
            }
    }
    fun updateAppointmentField(appointmentId: String, field: String, value: Boolean) {
        val db = FirebaseFirestore.getInstance()
        val appointmentRef = db.collection("appointments").document(appointmentId)

        appointmentRef.update(field, value)
            .addOnSuccessListener {
                println("${field} updated successfully")
            }
            .addOnFailureListener { e ->
                println("Couldn't update ${field}: ${e.message}")
            }
    }



    fun updateBookings(bookings: List<Appointment>) {
        tabTitle?.let { title ->
            val filteredBookings = bookings.filter { booking ->
                when (title) {
                    "Upcoming" -> booking.appointmentDate?.toDate()?.after(Date()) == true && booking.isActive
                    "Completed" -> booking.appointmentDate?.toDate()?.before(Date()) == true && booking.isActive
                    "Canceled" -> !booking.isActive
                    else -> false
                }
            }
            adapter.updateBookings(filteredBookings)
        } ?: run {
            println("Error: tabTitle is null")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun fetchUpdatedAppointments() {
        viewModel.fetchAppointments()
    }
}

