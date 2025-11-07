package com.example.tripsync.api.models

data class SearchPostsResponse(
    val status: String,
    val count: Int,
    val data: List<SearchPostResponseItem>
)
