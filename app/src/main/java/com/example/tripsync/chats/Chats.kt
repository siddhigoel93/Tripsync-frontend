package com.example.tripsync.api.models

// Keep your existing models
data class Conversation(
    val id: Int,
    val name: String?,
    val participants: List<Participant>,
    val message_count: Int? = null,
    val updated_at: String,
    val last_message: Message? = null,
    val unread_count: Int? = null,
    val is_group: Boolean? = false,
    val created_at: String? = null,
    val created_by: Int? = null,
    val group_avatar: String? = null
)

data class Participant(
    val id: Int,
    val email: String,
    val name: String? = null,
    val avatar: String? = null,
    val is_admin: Boolean? = false
)

data class Message(
    val id: Int,
    val content: String,
    val sender: MessageSender,
    val timestamp: String,
    val is_edited: Boolean? = false,
    val attachments: List<Attachment>? = null
)

data class MessageSender(
    val id: Int,
    val email: String,
    val name: String? = null,
    val avatar: String? = null
)

data class Attachment(
    val id: Int,
    val file_url: String,
    val file_type: String,
    val file_name: String
)

data class CreateConversationRequest(
    val participant_ids: List<Int>,
    val name: String? = null
)

data class AddParticipantsRequest(
    val participant_ids: List<Int>
)

data class RemoveParticipantRequest(
    val participant_id: Int
)

data class UpdateGroupRequest(
    val name: String? = null,
    val group_avatar: String? = null
)

data class SendMessageRequest(
    val content: String
)

// REMOVED - Not needed since API returns Conversation directly
// data class CreateConversationResponse(
//     val status: String?,
//     val message: String?,
//     val data: Conversation?
// )

data class MessagesResponse(
    val status: String?,
    val message: String?,
    val data: List<Message>?
)