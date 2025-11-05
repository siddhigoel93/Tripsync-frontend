package com.example.tripsync.api.models

data class CommentTextRequest(
    val text: String
)

data class CommentUser(
    val uid: Int,
    val fname: String,
    val lname: String,
    val pic: String
)
data class CommentData(
    val id: Int,
    val post: Int,
    val user: CommentUser,
    val text: String,
    val owner: Boolean,
    val created: String,
    val updated: String
)
data class CommentResponse(
    val status: String,
    val message: String,
    val data: CommentData
)

data class DeleteResponse(
    val status: String,
    val message: String
)
data class PostDetail(
    val id: Int,
    val user: CommentUser,
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
    val created: String,
    val updated: String,
    val comments: List<CommentData>
)

data class PostDetailResponse(
    val status: String,
    val data: PostDetail
)