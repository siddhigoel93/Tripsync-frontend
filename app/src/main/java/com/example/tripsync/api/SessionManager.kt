package com.example.tripsync.api

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * SessionManager handles all shared preferences operations
 * Separates auth tokens from user profile data to prevent data loss on logout
 */
object SessionManager {

    private const val AUTH_PREF = "auth"
    private const val APP_PREF = "app_prefs"

    // Auth keys (cleared on logout)
    private const val KEY_TOKEN = "token"
    private const val KEY_REFRESH_TOKEN = "refreshToken"
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"

    // Profile keys (persisted across sessions)
    private const val KEY_USER_ID = "userId"
    private const val KEY_FIRST_NAME = "fname"
    private const val KEY_LAST_NAME = "lname"
    private const val KEY_EMAIL = "userEmail"
    private const val KEY_CURRENT_EMAIL = "currentUserEmail"
    private const val KEY_PHONE = "phone_number"
    private const val KEY_AVATAR_URL = "userAvatarUrl"
    private const val KEY_BIO = "bio"
    private const val KEY_GENDER = "gender"
    private const val KEY_PREFERENCE = "preference"
    private const val KEY_BLOOD_GROUP = "bgroup"
    private const val KEY_ALLERGIES = "allergies"

    private fun getAuthPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(AUTH_PREF, Context.MODE_PRIVATE)
    }

    private fun getAppPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(APP_PREF, Context.MODE_PRIVATE)
    }

    // ============ Auth Methods ============

    fun saveAuthToken(context: Context, token: String, refreshToken: String? = null) {
        getAuthPrefs(context).edit().apply {
            putString(KEY_TOKEN, token)
            refreshToken?.let { putString(KEY_REFRESH_TOKEN, it) }
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun getAuthToken(context: Context): String? {
        return getAuthPrefs(context).getString(KEY_TOKEN, null)
    }

    fun isLoggedIn(context: Context): Boolean {
        return getAuthPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false) &&
                getAuthToken(context) != null
    }

    fun logout(context: Context) {
        // Clear ONLY auth tokens, not user profile data
        getAuthPrefs(context).edit().clear().apply()

        // Also clear tokens from app_prefs if they exist there
        getAppPrefs(context).edit().apply {
            remove(KEY_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_IS_LOGGED_IN)
            apply()
        }
    }

    // ============ Profile Methods ============

    fun saveUserProfile(
        context: Context,
        userId: Int? = null,
        firstName: String? = null,
        lastName: String? = null,
        email: String? = null,
        phone: String? = null,
        avatarUrl: String? = null,
        bio: String? = null,
        gender: String? = null,
        preference: String? = null,
        bloodGroup: String? = null,
        allergies: String? = null
    ) {
        getAppPrefs(context).edit().apply {
            userId?.let { putInt(KEY_USER_ID, it) }
            firstName?.let { putString(KEY_FIRST_NAME, it) }
            lastName?.let { putString(KEY_LAST_NAME, it) }
            email?.let {
                putString(KEY_EMAIL, it)
                putString(KEY_CURRENT_EMAIL, it)
            }
            phone?.let { putString(KEY_PHONE, it) }
            avatarUrl?.let { putString(KEY_AVATAR_URL, it) }
            bio?.let { putString(KEY_BIO, it) }
            gender?.let { putString(KEY_GENDER, it) }
            preference?.let { putString(KEY_PREFERENCE, it) }
            bloodGroup?.let { putString(KEY_BLOOD_GROUP, it) }
            allergies?.let { putString(KEY_ALLERGIES, it) }
            apply()
        }
    }

    fun getUserId(context: Context): Int {
        return getAppPrefs(context).getInt(KEY_USER_ID, -1)
    }

    fun getFirstName(context: Context): String? {
        return getAppPrefs(context).getString(KEY_FIRST_NAME, null)
    }

    fun getLastName(context: Context): String? {
        return getAppPrefs(context).getString(KEY_LAST_NAME, null)
    }

    fun getFullName(context: Context): String {
        val firstName = getFirstName(context) ?: ""
        val lastName = getLastName(context) ?: ""
        return "$firstName $lastName".trim().ifEmpty { "Unknown User" }
    }

    fun getEmail(context: Context): String? {
        return getAppPrefs(context).getString(KEY_EMAIL, null)
            ?: getAppPrefs(context).getString(KEY_CURRENT_EMAIL, null)
    }

    fun getPhone(context: Context): String? {
        return getAppPrefs(context).getString(KEY_PHONE, null)
    }

    fun getAvatarUrl(context: Context): String? {
        return getAppPrefs(context).getString(KEY_AVATAR_URL, null)
    }

    fun getBio(context: Context): String? {
        return getAppPrefs(context).getString(KEY_BIO, null)
    }

    fun getGender(context: Context): String? {
        return getAppPrefs(context).getString(KEY_GENDER, null)
    }

    fun getPreference(context: Context): String? {
        return getAppPrefs(context).getString(KEY_PREFERENCE, null)
    }

    fun getBloodGroup(context: Context): String? {
        return getAppPrefs(context).getString(KEY_BLOOD_GROUP, null)
    }

    fun getAllergies(context: Context): String? {
        return getAppPrefs(context).getString(KEY_ALLERGIES, null)
    }

    // ============ Utility Methods ============

    fun clearAllData(context: Context) {
        getAuthPrefs(context).edit { clear() }
        getAppPrefs(context).edit { clear() }
    }
}