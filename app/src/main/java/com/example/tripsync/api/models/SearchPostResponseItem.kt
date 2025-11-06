package com.example.tripsync.api.models

data class SearchPostResponseItem(
    val created: String,
    val desc: String,
    val dislikes: String,
    val id: Int,
    val img: String,
    val img_url: String,
    val likes: String,
    val loc: String,
    val owner: String,
    val rating: Int,
    val reaction: String,
    val title: String,
    val total_comments: String,
    val updated: String,
    val user: String,
    val vid: String,
    val vid_url: String
)