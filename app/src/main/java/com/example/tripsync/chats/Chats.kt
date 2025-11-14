package com.example.tripsync.api.models

data class User(
    val id: Int,
    val email: String,
    val name: String? = null,
    val first_name: String? = null,
    val last_name: String? = null,
    val profile_picture: String? = null
)


data class Conversation(
    val id: Int,
    val name: String?,
    val participants: List<Participant>,
    val message_count: Int? = null,
    val updated_at: String,
    val last_message: Message? = null,
    val unread_count: Int? = null,
    val is_group: Boolean? = false,
    val created_at: String? = null
)

data class Participant(
    val id: Int,
    val email: String,
    val name: String? = null
)

data class Message(
    val id: Int,
    val content: String,
    val sender: MessageSender,
    val timestamp: String
)

data class MessageSender(
    val id: Int,
    val email: String,
    val name: String? = null
)

data class CreateConversationRequest(
    val participant_ids: List<Int>,
    val name: String? = null
)

data class SendMessageRequest(
    val content: String
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
data class ConversationsListResponse(
    val status: String?,
    val message: String?,
    val data: List<Conversation>?
)