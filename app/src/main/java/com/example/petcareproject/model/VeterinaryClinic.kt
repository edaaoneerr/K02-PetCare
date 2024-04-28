package com.example.petcareproject.model

data class VeterinaryClinic(
    val name: String = "",
    val specialty: String = "",
    val rating: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    val address: String = "",
    var distance: Double = 0.0,
    var imageUrl: String = ""
)

