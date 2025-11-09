// Conversation.kt
package com.example.tripsync.api.models

data class Conversation(
    val id: Int,
    val name: String?,
    val participants: List<Participant>,
    val message_count: Int,
    val updated_at: String,
    val last_message: Message? = null
)

data class Participant(
    val id: Int,
    val email: String
)

data class Message(
    val id: Int,
    val content: String,
    val sender: MessageSender,
    val timestamp: String
)

data class MessageSender(
    val id: Int,
    val email: String
)

data class CreateConversationRequest(
    val participant_ids: List<Int>
)

data class CreateConversationResponse(
    val status: String?,
    val message: String?,
    val data: Conversation?
)
data class MessagesResponse(
    val status: String?,
    val message: String?,
    val data: List<Message>?
)