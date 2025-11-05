package com.example.tripsync.api.models

interface PostActionListener {
    fun onDelete(postId: Int)
    fun onLike(postId: Int)
    fun onComment(postId: Int)
}
