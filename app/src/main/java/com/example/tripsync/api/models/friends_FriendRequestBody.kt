package com.example.tripsync.api.models

data class FriendRequestBody(
    val receiver_id: Int,
    val message: String
)
