package com.example.tripsync.api.models

import com.google.gson.annotations.SerializedName

data class recieved_SenderInfo(
    val id: Int,
    val email: String?,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("profile_pic") val profilePic: String?,
    @SerializedName("phone_number") val phoneNumber: String?
)

data class recieved_ReceiverInfo(
    val id: Int,
    val email: String?,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("profile_pic") val profilePic: String?,
    @SerializedName("phone_number") val phoneNumber: String?
)

data class recieved_FriendRequestItem(
    val id: Int,
    @SerializedName("sender_info") val senderInfo: recieved_SenderInfo?,
    @SerializedName("receiver_info") val receiverInfo: recieved_ReceiverInfo?,
    val status: String?,
    val message: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

data class recieved_RespondBody(
    val request_id: Int,
    val action: String
)
