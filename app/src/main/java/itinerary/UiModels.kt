package com.example.tripsync.itinerary

data class UiDay(
    val dayNumber: Int,
    val title: String,
    val sections: List<UiSection>
)

data class UiSection(
    val label: String,
    val cards: List<UiCard>
)

data class UiCard(
    val iconRes: Int,
    val title: String,
    val subtitle: String?
)
