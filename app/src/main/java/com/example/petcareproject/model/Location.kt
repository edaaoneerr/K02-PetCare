package com.example.petcareproject.model

data class RouteResponse(
    val features: List<Feature>
)

data class Feature(
    val properties: Property
)

data class Property(
    val segments: List<Segment>
)

data class Segment(
    val distance: Double  // Distance in meters
)

