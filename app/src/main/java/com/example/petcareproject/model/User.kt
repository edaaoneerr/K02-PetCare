package com.example.petcareproject.model

data class User(
    val userId: String? = null,
    val userName: String = "",
    val userEmail: String = "",
    val provider: String = "",
    val userProfileImage: String = "",
    val createdAt: String = "",
    val lastSignInAt: String = "",
    val isVet: Boolean = false,
)
