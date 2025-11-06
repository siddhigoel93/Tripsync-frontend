package com.example.tripsync.api.models

data class Post(
    val id: Int,
    val user: UserP?,
    val title: String,
    val desc: String,
    val loc: String,
    val rating: Int?,
    val img: String?,
    val vid: String?,
    val img_url: String?,
    val vid_url: String?,
    val likes: Int,
    val dislikes: Int,
    val total_comments: Int,
    val reaction: String?,
    val owner: Boolean,
    val created: String,
    val updated: String
)


data class PostCreateRequest(
    val title: String,
    val desc: String,
    val loc: String,
    val loc_rating: Int,
    val img: String?,
    val vid: String?
)


data class UserP(
    val uid: Int,
    val fname: String?,
    val lname: String?,
    val pic: String?
)

data class PostResponse(
    val status: String,
    val count: Int,
    val data: List<Post>
)

