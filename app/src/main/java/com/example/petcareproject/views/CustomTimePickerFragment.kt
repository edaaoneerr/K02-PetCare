
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.petcareproject.R

class CustomTimePickerFragment : DialogFragment() {

    interface TimePickerListener {
        fun onTimeSelected(hourOfDay: Int, minute: Int)
    }

    var listener: TimePickerListener? = null
    var startHour: Int = 0
    var endHour: Int = 0
    var bookedSlots: List<Pair<Int, Int>> = emptyList()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_custom_time_picker)
        dialog.setTitle("Select Time")

        val hourPicker = dialog.findViewById<NumberPicker>(R.id.hourPicker)
        val minutePicker = dialog.findViewById<NumberPicker>(R.id.minutePicker)

        val availableSlots = generateAvailableSlots(startHour, endHour).filter { it !in bookedSlots }

        println("Available Slots: $availableSlots")
        println("Booked Slots: $bookedSlots")

        if (availableSlots.isEmpty()) {
            Toast.makeText(requireContext(), "No available slots for the selected date", Toast.LENGTH_LONG).show()
            dialog.dismiss()
            return dialog
        }

        hourPicker.minValue = 0
        hourPicker.maxValue = availableSlots.size - 1
        hourPicker.displayedValues = availableSlots.map { "${it.first.toString().padStart(2, '0')}:${if (it.second == 0) "00" else "30"}" }.toTypedArray()


        // Hide minute picker as it's not needed when showing specific slots
        minutePicker.visibility = View.GONE

        dialog.findViewById<Button>(R.id.okButton).setOnClickListener {
            val selectedSlot = availableSlots[hourPicker.value]
            listener?.onTimeSelected(selectedSlot.first, selectedSlot.second)
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }

    private fun generateAvailableSlots(startHour: Int, endHour: Int): List<Pair<Int, Int>> {
        val slots = mutableListOf<Pair<Int, Int>>()
        println("Generating slots from $startHour to $endHour")

        if (startHour <= endHour) {
            // Standard case where the start hour is less than or equal to the end hour
            println("standard case")
            for (hour in startHour..endHour) { // Including endHour in the range
                println("Hour: $hour")
                slots.add(Pair(hour, 0))
                slots.add(Pair(hour, 30))
            }
        } else {
            // Handle wrap-around from one day to the next
            for (hour in startHour until 24) { // From start hour to midnight, not including 24
                println("Hour: $hour")
                slots.add(Pair(hour, 0))
                slots.add(Pair(hour, 30))
            }
            for (hour in 0 until endHour) { // From midnight to end hour, not including endHour
                println("Hour: $hour")
                slots.add(Pair(hour, 0))
                slots.add(Pair(hour, 30))
            }
            slots.add(Pair(endHour, 0)) // Explicitly add the last slot for endHour at 0 minutes
        }

        println("Generated slots: $slots")
        return slots
    }




}