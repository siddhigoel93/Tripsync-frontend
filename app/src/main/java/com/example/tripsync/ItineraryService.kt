package com.example.tripsync.api

import com.example.tripsync.itinerary.CreateTripRequest
import com.example.tripsync.itinerary.CreateTripResponse
import com.example.tripsync.itinerary.ItineraryDayResponse
import com.example.tripsync.itinerary.TripListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ItineraryService {

    @GET("api/itinerary/trip/list/")
    suspend fun listTrips(): Response<TripListResponse>

    @POST("api/itinerary/trip/create/")
    suspend fun createTrip(@Body request: CreateTripRequest): Response<CreateTripResponse>

    @GET("api/itinerary/trip/{trip_id}/")
    suspend fun getTrip(@Path("trip_id") tripId: Int): Response<CreateTripResponse>

    @GET("api/itinerary/itinerary/{trip_id}/day/{day_number}/")
    suspend fun getDayItinerary(
        @Path("trip_id") tripId: Int,
        @Path("day_number") dayNumber: Int
    ): Response<ItineraryDayResponse>
}
