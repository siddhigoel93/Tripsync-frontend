package com.example.tripsync.api.models
data class Post(
    val id: Int,
    val user_id: Int,
    val user_email: String,
    val title: String,
    val desc: String,
    val loc: String,
    val loc_rating: Int,
    val img: String?,
    val vid: String?,
    val img_url: String?,
    val vid_url: String?,
    val created_at: String,
    val updated_at: String
)
