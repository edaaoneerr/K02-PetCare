package com.example.petcareproject.abstracts

import com.google.gson.annotations.SerializedName

data class RouteResponse(
    @SerializedName("features")
    val features: List<Feature>
)

data class Feature(
    @SerializedName("properties")
    val properties: Properties
)

data class Properties(
    @SerializedName("segments")
    val segments: List<Segment>
)

data class Segment(
    @SerializedName("distance")
    val distance: Double,
    @SerializedName("duration")
    val duration: Double
)
