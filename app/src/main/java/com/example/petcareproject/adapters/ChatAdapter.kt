package com.example.petcareproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petcareproject.R
import com.example.petcareproject.model.Chat
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale


/*class ChatAdapter(private val chats: List<Chat>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        return ChatViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.bind(chat)
    }

    override fun getItemCount() = chats.size

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.chatUsernameTitle)
        private val messageTextView: TextView = itemView.findViewById(R.id.chatLastMessage)
        private val dateTextView: TextView = itemView.findViewById(R.id.chatLastMessageDate)
        private val imageView: ImageView = itemView.findViewById(R.id.chatUserImage) // Reference to an ImageView in your layout


        fun bind(chat: Chat) {
            messageTextView.text = chat.lastMessage
            dateTextView.text = chat.lastMessageDate


        // Fetch user details dynamically
        val userDocRef = FirebaseFirestore.getInstance().collection("users").document(chat.lastMessageSenderId)
        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val userName = document.getString("name") ?: "No Name"
                val userProfileImage = document.getString("userProfileImage") ?: ""
                nameTextView.text = userName

                Glide.with(itemView.context)
                    .load(userProfileImage)
                    .into(imageView)
            }
        }.addOnFailureListener {
            // Handle any errors, such as network issues
            nameTextView.text = "User Name Unavailable"
            imageView.setImageResource(R.drawable.ic_profile)
        }

    }
}
}*/

class ChatAdapter(private val chats: List<Chat>, private val currentUserId: String, private val listener: ChatAdapter.OnItemClickListener) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    interface OnItemClickListener {
        fun onItemClick(chat: Chat)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        return ChatViewHolder(itemView, listener, chats)
    }


    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.bind(chat, currentUserId)
        holder.itemView.setOnClickListener {
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(chat)
            }
        }
    }


    override fun getItemCount() = chats.size

    class ChatViewHolder(itemView: View, private val listener: OnItemClickListener, private val chats: List<Chat>) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.chatUsernameTitle)
        private val messageTextView: TextView = itemView.findViewById(R.id.chatLastMessage)
        private val dateTextView: TextView = itemView.findViewById(R.id.chatLastMessageDate)
        private val imageView: ImageView = itemView.findViewById(R.id.chatUserImage)

        fun bind(chat: Chat, currentUserId: String) {
            messageTextView.text = chat.lastMessage
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            dateTextView.text = dateFormat.format(chat.lastMessageDate.toDate())
            // Determine the other user's ID
            var otherUserId = ""
            println("Current user in Adapter: " + currentUserId)

            if (chat.chatParticipants.any { it != currentUserId }) {
                otherUserId = chat.chatParticipants.first { it != currentUserId }
                println("Other user in Adapter: " + otherUserId)

                // Continue with existing logic
                // Fetch the other user's details dynamically
                val userDocRef =
                    FirebaseFirestore.getInstance().collection("users").document(otherUserId)
                userDocRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userName = document.getString("name") ?: "No Name"
                        val userProfileImage = document.getString("userProfileImage") ?: ""
                        nameTextView.text = userName

                        Glide.with(itemView.context)
                            .load(userProfileImage)
                            .into(imageView)
                    }
                }.addOnFailureListener {
                    // Handle any errors
                    nameTextView.text = "User Name Unavailable"
                    imageView.setImageResource(R.drawable.ic_profile)
                }

            } else {
                println("No user found: ")
                println(currentUserId)
                println(chat.chatParticipants)
            }
        }

    }
}
