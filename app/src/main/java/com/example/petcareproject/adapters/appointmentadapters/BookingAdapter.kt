package com.example.petcareproject.adapters.appointmentadapters
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petcareproject.databinding.BookingItemBinding
import com.example.petcareproject.model.Appointment
import com.example.petcareproject.model.Vet
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookingAdapter(
    private var bookings: List<Appointment>,
    private val currentTab: String,
    private val onCancelClick: (Appointment, () -> Unit) -> Unit,
    private val onRescheduleClick: (Appointment, () -> Unit) -> Unit,
    private val onRebookClick: (Appointment, () -> Unit) -> Unit,
    private val onAddReviewClick: (Appointment, () -> Unit) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = BookingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        holder.bind(booking)
    }

    override fun getItemCount(): Int = bookings.size

    fun updateBookings(newBookings: List<Appointment>) {
        bookings = newBookings
        notifyDataSetChanged()
    }

    inner class BookingViewHolder(private val binding: BookingItemBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(booking: Appointment) {
            val formattedDate = formatTimestamp(booking.appointmentDate)
            binding.vetAppointmentDate.text = formattedDate

            fetchVeterinarian(booking.vetId) { vet ->
                vet?.let {
                    binding.vetAppointmentName.text = it.vetName
                    binding.vetAppointmentSpecialty.text = it.vetSpecialty
                    binding.vetAppointmentClinicName.text = it.vetClinicName
                    Glide.with(binding.vetAppointmentImage.context).load(it.vetImageUrl).into(binding.vetAppointmentImage)
                }
            }
            println(currentTab)
            when (currentTab) {
                "Upcoming" -> {
                    println(booking.appointmentDate!!.toDate())
                    if (booking.isActive) {
                        binding.root.visibility = View.VISIBLE
                        binding.upcomingAppointmentButtonLayout.visibility = View.VISIBLE
                        binding.completedAppointmentButtonLayout.visibility = View.GONE
                        binding.cancelAppointmentButton.setOnClickListener {
                            onCancelClick(booking) {
                                booking.isActive = false
                                updateBookings(bookings.filter { it.isActive && it.appointmentDate!!.toDate().after(Date()) })
                            }
                        }
                        binding.rescheduleAppointmentButton.setOnClickListener {
                            onRescheduleClick(booking) {
                                updateBookings(bookings.filter {
                                    it.isActive && it.appointmentDate!!.toDate().after(Date()) })
                            }
                        }
                    } else {
                        println("Else")
                        binding.root.visibility = View.GONE
                    }
                }
                "Completed" -> {
                    if (booking.isActive && booking.appointmentDate!!.toDate().before(Date())) {
                        binding.root.visibility = View.VISIBLE
                        binding.upcomingAppointmentButtonLayout.visibility = View.GONE
                        binding.completedAppointmentButtonLayout.visibility = View.VISIBLE

                        if (booking.hasReview) {
                            binding.addReviewAppointmentButton.isEnabled = false
                        }
                        binding.rebookAppointmentButton.setOnClickListener {
                            onRebookClick(booking) {
                                updateBookings(bookings.filter { it.isActive && it.appointmentDate!!.toDate().before(Date()) })
                            }

                        }
                        binding.addReviewAppointmentButton.setOnClickListener {
                            onAddReviewClick(booking) {
                                booking.hasReview = true
                                updateBookings(bookings.filter { it.isActive && it.appointmentDate!!.toDate().before(Date()) })
                            }
                        }
                    } else {
                        binding.root.visibility = View.GONE
                    }
                }
                "Canceled" -> {
                    if (!booking.isActive) {
                        binding.root.visibility = View.VISIBLE
                        binding.upcomingAppointmentButtonLayout.visibility = View.GONE
                        binding.completedAppointmentButtonLayout.visibility = View.VISIBLE
                        binding.rebookAppointmentButton.visibility = View.VISIBLE
                        binding.addReviewAppointmentButton.visibility = View.GONE

                        binding.rebookAppointmentButton.setOnClickListener {
                            onRebookClick(booking) {
                                booking.isActive = true
                                updateBookings(bookings.filter { !it.isActive })
                            }
                        }
                    } else {
                        binding.root.visibility = View.GONE
                    }
                }
            }
        }

        private fun formatTimestamp(timestamp: Timestamp?): String {
            val dateFormat = SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault())
            return timestamp?.toDate()?.let { dateFormat.format(it) } ?: ""
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
}
