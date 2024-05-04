package com.example.petcareproject.model

import com.google.firebase.Timestamp


data class Message(
    val messageId: String? = null,
    val chatId: String? = null,
    val senderId: String? = null,
    val messageText: String,
    val messageDate: Timestamp,
    val isMessageReceived: Boolean
)