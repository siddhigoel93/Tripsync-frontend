package com.example.tripsync.api.models

import java.io.Serializable

data class FunFacts(
    val id: Int,
    val slide: Int,
    val title: String,
    val desc: String,
    val photo: String
)


data class Place(
    val id: Int,
    val name: String,
    val main: String,
    val fun_facts: List<FunFact>
) : Serializable


data class TrendingPlacesResponse(
    val status: String,
    val message: String,
    val data: List<Place>
) : Serializable



