package com.example.tripsync.api.models

interface CommentActionListener {
    fun onUpdate(commentId: Int, newText: String)
    fun onDelete(commentId: Int , position: Int)
}