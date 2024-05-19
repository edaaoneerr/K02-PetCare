
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.example.petcareproject.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView

class CustomDatePickerFragment : DialogFragment() {

    interface DatePickerListener {
        fun onDateSelected(year: Int, month: Int, dayOfMonth: Int)
    }

    var listener: DatePickerListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_custom_date_picker)
        dialog.setTitle("Select Date")

        val calendarView = dialog.findViewById<MaterialCalendarView>(R.id.calendarView)
        calendarView.setOnDateChangedListener { widget, date, selected ->
            // Do nothing
        }

        dialog.findViewById<Button>(R.id.okButton).setOnClickListener {
            val selectedDate = calendarView.selectedDate ?: CalendarDay.today()
            listener?.onDateSelected(selectedDate.year, selectedDate.month, selectedDate.day)
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }
}
