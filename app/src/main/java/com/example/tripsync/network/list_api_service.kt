package com.example.tripsync.network

import com.example.tripsync.network.models.ApiResponse
import com.example.tripsync.network.models.TripDetail
import com.example.tripsync.network.models.TripItem
import retrofit2.http.GET
import retrofit2.http.Path

interface ListApiService {
    @GET("api/itinerary/trip/list/")
    suspend fun getTrips(): ApiResponse<List<TripItem>>

    @GET("api/itinerary/trip/{id}/")
    suspend fun getTrip(@Path("id") id: Int): TripDetail
}
