package com.example.tripsync.api.models

data class TrendingPlaceResponse(
    val status: String,
    val message: String,
    val data: List<TrendingPlace>
)

data class TrendingPlace(
    val id: Int,
    val name: String,
    val main: String,
    val fun_facts: List<FunFact>
)

data class FunFact(
    val id: Int,
    val slide: Int,
    val title: String,
    val desc: String,
    val photo: String
)

