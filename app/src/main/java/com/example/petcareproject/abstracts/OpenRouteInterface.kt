package com.example.petcareproject.abstracts

import com.example.petcareproject.model.RouteResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenRouteServiceApi {
    @GET("v2/directions/driving-car")
    fun getRoute(
        @Query("api_key") apiKey: String,
        @Query("start") start: String,
        @Query("end") end: String
    ): Call<RouteResponse>
}
