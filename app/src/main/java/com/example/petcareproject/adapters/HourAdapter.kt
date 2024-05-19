import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.petcareproject.R


class HourAdapter(private val hours: List<String>, private val onHourSelected: (String) -> Unit) : RecyclerView.Adapter<HourAdapter.HourViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hour, parent, false)
        return HourViewHolder(view)
    }

    override fun onBindViewHolder(holder: HourViewHolder, position: Int) {
        val hour = hours[position]
        holder.bind(hour, position == selectedPosition)
        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            onHourSelected(hour)
        }
    }

    override fun getItemCount(): Int = hours.size

    class HourViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val hourText: TextView = itemView.findViewById(R.id.hourText)

        fun bind(hour: String, isSelected: Boolean) {
            hourText.text = hour
            itemView.isSelected = isSelected
        }
    }
}
