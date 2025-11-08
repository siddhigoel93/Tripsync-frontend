package com.example.tripsync.api.models

data class Participant(
    val id: Int,
    val email: String
)

data class Sender(
    val id: Int
)

data class LastMessage(
    val id: Int,
    val content: String,
    val sender: Sender,
    val timestamp: String
)

data class Conversation(
    val id: Int,
    val name: String,
    val participants: List<Participant>,
    val last_message: LastMessage?,
    val message_count: Int,
    val updated_at: String
)

data class ConversationResponse(
    val status: String,
    val message: String,
    val data: List<Conversation>
)

data class MessageSender(
    val id: Int,
    val email: String? = null
)

data class Message(
    val id: Int,
    val content: String,
    val sender: MessageSender,
    val timestamp: String
)

data class MessagesResponse(
    val status: String,
    val message: String,
    val data: List<Message>
)

data class CreateConversationRequest(
    val participant_ids: List<Int>
)

data class CreateConversationResponse(
    val status: String,
    val message: String,
    val data: Conversation?
)

