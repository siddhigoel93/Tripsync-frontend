package com.example.tripsync.api.models

import com.google.gson.annotations.SerializedName

data class friend_FriendRequestBody(
    @SerializedName("receiver_id")
    val receiver_id: Int,
    @SerializedName("message")
    val message: String
)

data class friend_FriendRequestResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: friend_FriendRequestData?
)

data class friend_FriendRequestData(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("sender_info")
    val senderInfo: friend_UserInfo?,
    @SerializedName("receiver_info")
    val receiverInfo: friend_UserInfo?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)

data class friend_UserInfo(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("full_name")
    val fullName: String?,
    @SerializedName("profile_pic")
    val profilePic: String?,
    @SerializedName("phone_number")
    val phoneNumber: String?
)
