package com.example.tripsync.itinerary

import com.example.tripsync.R

object IconMapper {

    fun sectionIcon(label: String): Int = when (label.lowercase()) {
        "morning" -> R.drawable.morning
        "afternoon" -> R.drawable.afternoon
        "night" -> R.drawable.night
        else -> R.drawable.morning
    }

    fun activityIcon(category: String): Int = when (category.lowercase()) {
        "flight", "plane", "arrival", "departure" -> R.drawable.airplane
        "cab", "transport", "taxi" -> R.drawable.cab
        "hotel", "check-in", "accommodation", "sleep" -> R.drawable.sleep
        "food", "lunch", "dinner", "breakfast" -> R.drawable.food
        else -> R.drawable.airplane
    }
}
