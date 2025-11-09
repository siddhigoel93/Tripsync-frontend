package com.example.tripsync.chatbot
data class ChatMessage(
    val message: String,
    val isUser: Boolean // true if user sent, false if bot
)
data class ChatRequest(val message: String)
data class ChatResponse(
    val success: Boolean,
    val message: String,
    val response: String,
    val session_id: String?,
    val created_at: String?
)