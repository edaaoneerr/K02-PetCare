package com.example.petcareproject.model

import com.google.firebase.Timestamp


data class Review(
    var reviewId: String? = null,
    val userId: String,
    val vetId: String,
    val rating: String,
    val comment: String,
    val timestamp: Timestamp = Timestamp.now()
)
