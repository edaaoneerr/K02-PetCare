
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.petcareproject.R
import com.example.petcareproject.model.Message
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

class MessageAdapter(private val messages: List<Message>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize views
        private val receivedMessageTextView: TextView = itemView.findViewById(R.id.receivedMessageTextView)
        private val sentMessageTextView: TextView = itemView.findViewById(R.id.sentMessageTextView)
        private val sentMessageLayout: LinearLayout = itemView.findViewById(R.id.sentMessageLayout)
        private val receivedMessageLayout: LinearLayout = itemView.findViewById(R.id.receivedMessageLayout)
        private val sentTimestampTextView: TextView = itemView.findViewById(R.id.sentMessageTimestamp)
        private val receivedTimestampTextView: TextView = itemView.findViewById(R.id.receivedMessageTimestamp)

        fun bind(message: Message) {
            val TAG = "MessageAdapter"
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            val isSender = message.senderId == currentUserId
            Log.d(TAG, currentUserId.toString())
            if (isSender) {
                sentMessageTextView.text = message.messageText
                sentTimestampTextView.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.messageDate.toDate())
                sentMessageLayout.visibility = View.VISIBLE
                receivedMessageLayout.visibility = View.GONE

            } else {
                receivedMessageTextView.text = message.messageText
                receivedTimestampTextView.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.messageDate.toDate())
                receivedMessageLayout.visibility = View.VISIBLE
                sentMessageLayout.visibility = View.GONE
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
        return MessageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size
}
