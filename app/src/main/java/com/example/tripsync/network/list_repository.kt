package com.example.tripsync.network

import com.example.tripsync.network.models.ApiResponse
import com.example.tripsync.network.models.TripDetail
import com.example.tripsync.network.models.TripItem

class ListRepository(private val api: ListApiService) {
    suspend fun getTrips(): List<TripItem>? {
        return try {
            val res: ApiResponse<List<TripItem>> = api.getTrips()
            res.data ?: emptyList()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getTripDetail(id: Int): TripDetail? {
        return try {
            api.getTrip(id)
        } catch (e: Exception) {
            null
        }
    }
}
