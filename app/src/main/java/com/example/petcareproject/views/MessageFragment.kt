package com.example.petcareproject.views

import MessageAdapter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.petcareproject.R
import com.example.petcareproject.databinding.FragmentMessageBinding
import com.example.petcareproject.model.Chat
import com.example.petcareproject.model.Message
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch


class MessageFragment : Fragment() {
    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!
    private var messageAdapter: MessageAdapter? = null
    var messageId: String? = ""
    var chatId: String? =  ""
    var senderId: String? = ""
    var messageText: String = ""
    var messageDate: Timestamp = Timestamp.now()
    var isMessageReceived: Boolean = false

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val message = binding.messageText
        val sendButton = binding.sendButton
        val userNameText = binding.receiverUserName
        val userProfileImage = binding.receiverUserImage
        fetchMessages()

        sendButton.setOnClickListener {
            sendMessage(message.text.toString())
        }
        messages.observe(viewLifecycleOwner) {
            updateMessageAdapter(it)
        }
            getUserName { userName, userImage ->
                if (userName != null && userImage != null) {
                    userNameText.text = userName
                    Glide.with(userProfileImage.context)
                        .load(userImage)
                        .into(userProfileImage)
                } else {
                    userNameText.text = "Deleted User"
                    userProfileImage.setImageResource(R.drawable.ic_profile)
                }

        }

        message.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Not needed for this implementation
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Change send button icon based on whether there is text
                if (s.toString().trim().isEmpty()) {
                    sendButton.setImageResource(R.drawable.ic_microphone) // No text, show mic icon
                } else {
                    sendButton.setImageResource(R.drawable.ic_send) // Text present, show send icon
                }
            }
            override fun afterTextChanged(s: Editable) {
                // Not needed for this implementation
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateMessageAdapter(messages: List<Message>) {
        messageAdapter = MessageAdapter(messages)
        binding.messageRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.messageRecyclerView.adapter = messageAdapter
    }

    fun fetchMessages() {
        val db = FirebaseFirestore.getInstance()
        db.collection("messages")
            .orderBy("messageTimestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                val messages = mutableListOf<Message>()
                lifecycleScope.launch { // Use lifecycleScope to launch coroutines in a Fragment
                    result.forEach { document ->
                        // Extract details from the document and call getDistance
                        // Update clinics list inside the coroutine
                        try {
                            messageId = document.id
                            chatId = document.getString("chatId") ?: ""
                            senderId = document.getString("senderId") ?: "" // Fetch the image URL
                            messageText = document.getString("messageText") ?: ""
                            messageDate = document.getTimestamp("messageTimestamp") ?: Timestamp.now()
                            messages.add(
                                Message(messageId, chatId, senderId, messageText, messageDate, isMessageReceived)
                            )
                        }
                        catch (e: Exception) {
                            println("Document null: $e") // or handle the exception as needed
                        }
                    }
                    // After the loop, update the adapter
                    _messages.postValue(messages)
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }

    fun sendMessage(messageText: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val newMessage = hashMapOf(
            "chatId" to chatId,
            "senderId" to currentUser.uid,
            "messageText" to messageText,
            "messageTimestamp" to Timestamp.now()  // Firestore Timestamp object for real-time
        )

        val db = FirebaseFirestore.getInstance()
        // Add message to 'messages' collection
        db.collection("messages")
            .add(newMessage)
            .addOnSuccessListener { documentReference ->
                updateLastMessageInChat(messageText, currentUser.uid, documentReference.id)
                displaySentMessage(Message(documentReference.id, chatId, currentUser.uid, messageText, Timestamp.now(), false))
            }
            .addOnFailureListener { e ->
                println("Error adding document: $e")
            }
    }

    private fun updateLastMessageInChat(messageText: String, senderId: String, messageId: String) {
        val chatUpdate = mapOf(
            "lastMessage" to messageText,
            "lastMessageSenderId" to senderId,
            "lastMessageTimestamp" to Timestamp.now(),
            "lastMessageId" to messageId  // Assuming you also store the last message's ID
        )

        val db = FirebaseFirestore.getInstance()
        db.collection("chats").document(chatId!!)
            .update(chatUpdate)
            .addOnFailureListener { e ->
                println("Error updating chat: $e")
            }
    }

    private fun displaySentMessage(message: Message) {
        _messages.value?.let {
            val updatedMessages = it.toMutableList()
            updatedMessages.add(message)
            _messages.postValue(updatedMessages)
        } ?: run {
            _messages.postValue(listOf(message))
        }
    }

    fun getUserName(callback: (String?, String?) -> Unit) {
        val chat = arguments?.getParcelable<Chat>("chat")
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        var otherUserId = ""

        println("Current user in fragment" + currentUserId)

        if (chat?.chatParticipants?.any { it != currentUserId }!!) {
            otherUserId = chat.chatParticipants.first { it != currentUserId }
            println("Other user in fragment" + otherUserId)
        }
        val userDocRef =
            FirebaseFirestore.getInstance().collection("users").document(otherUserId)
            userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val userName = document.getString("name") ?: "No Name"
                val userProfileImage = document.getString("userProfileImage") ?: ""
                callback(userName, userProfileImage)
            }
        }.addOnFailureListener {
            // Handle any errors
            callback(null, null)

        }
    }


}