package com.example.tripsync.api.models

import java.io.Serializable

data class FunFact(
    val id: Int,
    val slide: Int,
    val title: String,
    val desc: String,
    val photo: String
) : Serializable

data class TrendingPlace(
    val id: Int,
    val name: String,
    val main: String,
    val fun_facts: List<FunFact> = emptyList()
) : Serializable

data class TrendingPlacesResponse(
    val status: String,
    val message: String,
    val data: List<TrendingPlace>
) : Serializable
