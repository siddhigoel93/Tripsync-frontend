package com.example.tripsync.itinerary

data class TripListResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val data: TripListData? = null
)
data class TripListData(
    val results: List<TripItem>? = null
)
data class TripItem(
    val id: Int,
    val tripname: String? = null,
    val current_loc: String? = null,
    val destination: String? = null,
    val start_date: String? = null,
    val end_date: String? = null,
    val days: Int? = null,
    val trip_type: String? = null,
    val trip_preferences: String? = null,
    val budget: Double? = null
)

data class CreateTripRequest(
    val tripname: String,
    val current_loc: String,
    val destination: String,
    val start_date: String,
    val end_date: String,
    val days: Int,
    val trip_type: String,
    val trip_preferences: String,
    val budget: Double
)

data class CreateTripResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val data: CreateTripData? = null
)
data class CreateTripData(
    val id: Int? = null,
    val tripname: String? = null,
    val current_loc: String? = null,
    val destination: String? = null,
    val trending: Boolean? = null,
    val start_date: String? = null,
    val end_date: String? = null,
    val days: Int? = null,
    val trip_type: String? = null,
    val trip_preferences: String? = null,
    val budget: Double? = null,
    val itinerary: ItinerarySummary? = null
)
data class ItinerarySummary(
    val id: Int? = null,
    val day_plans: List<DayPlan>? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)
data class DayPlan(
    val id: Int? = null,
    val day_number: Int? = null,
    val title: String? = null,
    val activities: List<ActivityItem>? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)
data class ActivityItem(
    val time: String? = null,
    val title: String? = null,
    val description: String? = null,
    val location: String? = null,
    val duration: String? = null,
    val cost: Double? = null,
    val category: String? = null
)

data class ItineraryDayResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val data: ItineraryDayData? = null
)
data class ItineraryDayData(
    val trip_id: Int? = null,
    val day_number: Int? = null,
    val title: String? = null,
    val sections: List<ItinerarySection>? = null,
    val activities: List<ActivityItem>? = null
)
data class ItinerarySection(
    val label: String? = null,
    val icon: String? = null,
    val cards: List<ItineraryCard>? = null
)
data class ItineraryCard(
    val title: String? = null,
    val subtitle: String? = null,
    val icon: String? = null
)
