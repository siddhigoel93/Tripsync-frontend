package com.example.tripsync

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson

data class CachedUserInfo(
    val userId: Int,
    val name: String,
    val email: String?,
    val fname: String?,
    val lname: String?,
    val profilePicUrl: String? = null,
    val profilePicBase64: String? = null
)

object ConversationInfoCache {
    private const val PREFS_NAME = "conversation_cache"
    private const val KEY_PREFIX = "conv_"
    private const val TAG = "ConversationCache"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Save user info for a conversation
     */
    fun saveUserInfo(context: Context, conversationId: Int, userInfo: CachedUserInfo) {
        try {
            val prefs = getPrefs(context)
            val gson = Gson()
            val json = gson.toJson(userInfo)

            val success = prefs.edit()
                .putString("$KEY_PREFIX$conversationId", json)
                .commit() // Use commit() instead of apply() to ensure immediate save

            if (success) {
                Log.d(TAG, "✅ Successfully saved user info for conversation $conversationId")
                Log.d(TAG, "   Name: ${userInfo.name}")
                Log.d(TAG, "   UserID: ${userInfo.userId}")
                Log.d(TAG, "   Email: ${userInfo.email}")
                Log.d(TAG, "   JSON: $json")

                // Verify it was saved
                val verification = prefs.getString("$KEY_PREFIX$conversationId", null)
                if (verification != null) {
                    Log.d(TAG, "✅ Verification: Data is persisted correctly")
                } else {
                    Log.e(TAG, "❌ Verification FAILED: Data not found after save!")
                }
            } else {
                Log.e(TAG, "❌ Failed to save user info for conversation $conversationId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception saving user info for conversation $conversationId", e)
        }
    }

    /**
     * Get cached user info for a conversation
     */
    fun getUserInfo(context: Context, conversationId: Int): CachedUserInfo? {
        return try {
            val prefs = getPrefs(context)
            val json = prefs.getString("$KEY_PREFIX$conversationId", null)

            Log.d(TAG, "🔍 Looking for cached info for conversation $conversationId")

            if (json != null) {
                Log.d(TAG, "✅ Found cached JSON: $json")
                val gson = Gson()
                val userInfo = gson.fromJson(json, CachedUserInfo::class.java)
                Log.d(TAG, "✅ Parsed user info: ${userInfo?.name}")
                userInfo
            } else {
                Log.d(TAG, "❌ No cached info found for conversation $conversationId")

                // List all keys to debug
                val allKeys = prefs.all.keys
                Log.d(TAG, "   Available keys in cache: ${allKeys.joinToString()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading user info for conversation $conversationId", e)
            null
        }
    }

    /**
     * Get display name for a conversation
     */
    fun getDisplayName(context: Context, conversationId: Int): String {
        Log.d(TAG, "📝 getDisplayName called for conversation $conversationId")
        val userInfo = getUserInfo(context, conversationId)
        val name = userInfo?.name ?: "Unknown"
        Log.d(TAG, "📝 Returning name: $name")
        return name
    }

    /**
     * Get profile picture URL/Base64 for a conversation
     */
    fun getProfilePic(context: Context, conversationId: Int): String? {
        Log.d(TAG, "🖼️ getProfilePic called for conversation $conversationId")
        val userInfo = getUserInfo(context, conversationId)
        val pic = userInfo?.profilePicUrl ?: userInfo?.profilePicBase64
        Log.d(TAG, "🖼️ Returning profile pic: ${if (pic != null) "Found" else "None"}")
        return pic
    }

    /**
     * Clear cache for a specific conversation
     */
    fun clearConversation(context: Context, conversationId: Int) {
        try {
            getPrefs(context).edit()
                .remove("$KEY_PREFIX$conversationId")
                .commit()
            Log.d(TAG, "🗑️ Cleared cache for conversation $conversationId")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache", e)
        }
    }

    /**
     * Clear all cached conversation info
     */
    fun clearAll(context: Context) {
        try {
            getPrefs(context).edit().clear().commit()
            Log.d(TAG, "🗑️ Cleared all conversation cache")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing all cache", e)
        }
    }

    /**
     * Debug: List all cached conversations
     */
    fun debugListAll(context: Context) {
        try {
            val prefs = getPrefs(context)
            val allEntries = prefs.all
            Log.d(TAG, "=== DEBUG: All Cached Conversations ===")
            Log.d(TAG, "Total entries: ${allEntries.size}")
            allEntries.forEach { (key, value) ->
                Log.d(TAG, "  Key: $key")
                Log.d(TAG, "  Value: $value")
            }
            Log.d(TAG, "======================================")
        } catch (e: Exception) {
            Log.e(TAG, "Error listing cache", e)
        }
    }
}