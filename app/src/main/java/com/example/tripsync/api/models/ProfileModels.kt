package com.example.tripsync.api.models
import com.google.gson.annotations.SerializedName

data class CreateProfileRequest(
    val fname: String,
    val lname: String,
    val phone_number: String,
    val date: String,
    val gender: String,
    val bio: String,
    val bgroup: String,
    val allergies: String,
    val medical: String,
    val ename: String,
    val enumber: String,
    val erelation: String,
    @SerializedName("prefrence") val preference: String
)

//data class RegisterRequest(
//    val email: String,
//    val preference: String
//)

data class CreateProfileResponse(
    val success: Boolean,
    val message: String,
    val data: CreateProfileData?
)
data class CreateProfileData(
    val phone_number: String?,
    val otp_expiry_minutes: Int?,
    val max_attempts: Int?,
    val profile_pic_uploaded: Boolean?
)

data class OtpCodeRequest(val otp_code: String)

data class VerifyPhoneResponse(
    val success: Boolean,
    val message: String,
    val data: VerifyPhoneData?
)
data class VerifyPhoneData(
    val profile: VerifiedProfile?
)
data class VerifiedProfile(
    val id: Int,
    val email: String,
    val fname: String,
    val lname: String,
    val is_phone_verified: Boolean
)

data class ResendOtpResponse(
    val success: Boolean,
    val message: String,
    val data: ResendOtpData?
)
data class ResendOtpData(
    val otp_expiry_minutes: Int?,
    val max_attempts: Int?
)

class EmptyJsonBody

data class Profile(
    val fname: String?,
    val lname: String?,
    val phone_number: String?,
    val bgroup: String?,
    val bio: String?,
    val is_phone_verified: Boolean?,
    val ename: String?,
    val enumber: String?,
    val erelation: String?,
    val preference: String?
)
data class GetProfileResponse(
    val success: Boolean,
    val data: ProfileContainer?
)
data class ProfileContainer(
    val profile: Profile?
)

data class UpdateProfileRequest(
    val fname: String? = null,
    val lname: String? = null,
    val bio: String? = null,
    val bgroup: String? = null,
    val ename: String? = null,
    val enumber: String? = null,
    val erelation: String? = null,
    val preference: String? = null
)
data class UpdateProfileResponse(
    val success: Boolean,
    val message: String,
    val data: UpdateProfileData?
)
data class UpdateProfileData(
    val profile: Profile?
)

data class EmergencySOSRequest(
    val message: String = "",
    val location: String = ""
)
data class EmergencySOSResponse(
    val success: Boolean,
    val message: String,
    val data: EmergencySOSData?
)
data class EmergencySOSData(
    val sent_to: String?,
    val contact_name: String?,
    val relation: String?
)
