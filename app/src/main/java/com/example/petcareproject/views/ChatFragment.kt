package com.example.petcareproject.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petcareproject.R
import com.example.petcareproject.adapters.ChatAdapter
import com.example.petcareproject.databinding.FragmentChatBinding
import com.example.petcareproject.model.Chat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch


class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private var chatAdapter: ChatAdapter? = null
    var chatId: String? = null
    var chatParticipants: List<String> = mutableListOf()
    var lastMessage: String = ""
    var lastMessageSenderId: String = ""
    var lastMessageDate: Timestamp = Timestamp.now()
    var lastMessageId: String? = ""

    private val _chats = MutableLiveData<List<Chat>>()
    val chats: LiveData<List<Chat>> = _chats

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchChats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateChatAdapter(chats: List<Chat>) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        println("Current user in Update Function: " + userId)
            chatAdapter = ChatAdapter(chats, userId, object : ChatAdapter.OnItemClickListener {
                override fun onItemClick(chat: Chat) {
                    if (chat != null) {
                        val bundle = Bundle().apply {
                            putParcelable("chat", chat)
                        }
                        findNavController().navigate(R.id.action_chatFragment_to_messageFragment, bundle)
                    }
                    // Handle the click event
                }
            })
            binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.chatRecyclerView.adapter = chatAdapter
    }
    fun fetchChats() {
        val db = FirebaseFirestore.getInstance()
        db.collection("chats")
            .get()
            .addOnSuccessListener { result ->
                val chats = mutableListOf<Chat>()
                lifecycleScope.launch { // Use lifecycleScope to launch coroutines in a Fragment
                    result.forEach { document ->
                        // Extract details from the document and call getDistance
                        // Update clinics list inside the coroutine
                        try {
                            chatId = document.id
                            chatParticipants = document.get("chatParticipants") as? List<String> ?: mutableListOf()
                            lastMessage = document.getString("lastMessage") ?: "" // Fetch the image URL
                            lastMessageSenderId = document.getString("lastMessageSenderId") ?: ""
                            lastMessageDate = document.getTimestamp("lastMessageTimestamp") ?: Timestamp.now()
                            lastMessageId = document.getString("lastMessageId") ?: ""

                            chats.add(
                                Chat(chatId, chatParticipants, lastMessageId, lastMessage, lastMessageSenderId, lastMessageDate)
                            )
                        }
                        catch (e: Exception) {
                            println("Document null: $e") // or handle the exception as needed
                        }
                    }
                    // After the loop, update the adapter
                    updateChatAdapter(chats)
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }


}