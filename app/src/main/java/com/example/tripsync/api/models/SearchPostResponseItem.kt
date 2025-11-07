package com.example.tripsync.api.models

data class SearchPostResponseItem(
    val id: Int,
    val user: Any?,
    val title: String,
    val desc: String,
    val loc: String,
    val rating: Int,
    val img: String?,
    val vid: String?,
    val img_url: String?,
    val vid_url: String?,
    val likes: Int,
    val dislikes: Int,
    val total_comments: Int,
    val reaction: String?,
    val owner: Boolean,
    val created: String?,
    val updated: String?
)
