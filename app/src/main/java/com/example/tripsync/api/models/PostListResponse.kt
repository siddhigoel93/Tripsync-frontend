package com.example.tripsync.api.models

import com.google.gson.annotations.SerializedName

data class PostListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("count") val count: Int,
    @SerializedName("data") val data: List<Post>
)