package com.example.tripsync.network.models

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("count") val count: Int = 0,
    @SerializedName("data") val data: T? = null
)

data class TripItem(
    @SerializedName("id") val id: Int,
    @SerializedName("tripname") val tripname: String?
)

data class TripDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("tripname") val tripname: String?,
    @SerializedName("current_loc") val current_loc: String?,
    @SerializedName("destination") val destination: String?,
    @SerializedName("trending") val trending: Boolean?,
    @SerializedName("start_date") val start_date: String?,
    @SerializedName("end_date") val end_date: String?,
    @SerializedName("days") val days: Int?,
    @SerializedName("trip_type") val trip_type: String?,
    @SerializedName("trip_preferences") val trip_preferences: String?,
    @SerializedName("budget") val budget: Double?,
    @SerializedName("itinerary") val itinerary: JsonElement?
)
