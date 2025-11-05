package com.example.tripsync.api.models

data class LikeRequest(
    val like: Boolean
)

data class LikeResponse(
    val status: String,
    val message: String,
    val data: LikeResponseData
)

data class LikeResponseData(
    val action: String,
    val like: Boolean,
    val likes: Int,
    val dislikes: Int
)