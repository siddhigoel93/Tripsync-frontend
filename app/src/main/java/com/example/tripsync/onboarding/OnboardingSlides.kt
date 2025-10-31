package com.example.tripsync.onboarding

import com.example.tripsync.R

object OnboardingSlides {

    val slides = listOf(
        OnboardingData(
            title = "PLAN",
            heading = "Smart Destination Discovery",
            description = "Explore trending destinations, hidden gems,\nand personalized recommendations — all in\none place.",
            imageRes = R.drawable.plan,
            buttonText = "Next"
        ),
        OnboardingData(
            title = "SYNC",
            heading = "Collaborative Travel Planning",
            description = "Plan trips together with friends and family —\nadd activities, split expenses, and share\nitineraries.",
            imageRes = R.drawable.sync,
            buttonText = "Next"
        ),
        OnboardingData(
            title = "CONNECT",
            heading = "Trip Budget & Cost Estimator",
            description = "Plan smarter with real-time cost estimates\nfor flights, stays, and activities.",
            imageRes = R.drawable.connect,
            buttonText = "Next"
        ),
        OnboardingData(
            title = "ENJOY",
            heading = "AI-Powered Itinerary Builder",
            description = "Get a day-by-day itinerary tailored to your\nstyle in minutes.",
            imageRes = R.drawable.enjoy,
            buttonText = "Get Started"
        )
    )
}
