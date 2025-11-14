package com.example.tripsync.api.models

import com.google.gson.annotations.SerializedName

data class FriendRequestResponse(
    val success: Boolean,
    val message: String?,
    val data: FriendRequestData?
)

data class FriendRequestData(
    val id: Int?,
    @SerializedName("sender_info")
    val senderInfo: FriendUserInfo?,
    @SerializedName("receiver_info")
    val receiverInfo: FriendUserInfo?,
    val status: String?,
    val message: String?,
    @SerializedName("created_at")
    val createdAt: String?
)

data class FriendUserInfo(
    val id: Int?,
    val email: String?,
    @SerializedName("full_name")
    val fullName: String?,
    @SerializedName("profile_pic")
    val profilePic: String?,
    @SerializedName("phone_number")
    val phoneNumber: String?
)
