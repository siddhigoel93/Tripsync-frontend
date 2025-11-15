package com.example.tripsync.chats

import android.content.Context
import android.util.Log
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.UserApi
import com.example.tripsync.api.UserSearchRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UserCacheManager {
    private val userCache = mutableMapOf<String, String>()


    suspend fun getDisplayName(context: Context, email: String): String {
        if (userCache.containsKey(email)) {
            return userCache[email] ?: email
        }

        return try {
            withContext(Dispatchers.IO) {
                val userApi = ApiClient.createService(context, UserApi::class.java)

                val emailName = email.split("@").firstOrNull() ?: email
                val parts = emailName.split(".", "_", "-")
                val firstName = parts.getOrNull(0) ?: ""
                val lastName = parts.getOrNull(1) ?: ""

                if (firstName.isNotEmpty()) {
                    val request = UserSearchRequest(fname = firstName, lname = lastName)
                    val response = userApi.searchUsers(request)

                    if (response.isSuccessful) {
                        val users = response.body()?.data?.users ?: emptyList()
                        val user = users.find { it.email == email }

                        if (user != null) {
                            val displayName = buildString {
                                if (!user.fname.isNullOrEmpty()) {
                                    append(user.fname)
                                }
                                if (!user.lname.isNullOrEmpty()) {
                                    if (isNotEmpty()) append(" ")
                                    append(user.lname)
                                }
                            }.ifEmpty { email }

                            userCache[email] = displayName
                            Log.d("UserCache", "Cached: $email -> $displayName")
                            return@withContext displayName
                        }
                    }
                }

                userCache[email] = email
                email
            }
        } catch (e: Exception) {
            Log.e("UserCache", "Error fetching user info for $email", e)
            email
        }
    }

    suspend fun preloadUsers(context: Context, emails: List<String>) {
        val uncachedEmails = emails.filter { !userCache.containsKey(it) }
        if (uncachedEmails.isEmpty()) return

        withContext(Dispatchers.IO) {
            uncachedEmails.forEach { email ->
                try {
                    getDisplayName(context, email)
                } catch (e: Exception) {
                    Log.e("UserCache", "Error preloading $email", e)
                }
            }
        }
    }


    fun getCachedName(email: String): String {
        return userCache[email] ?: email
    }

    fun clearCache() {
        userCache.clear()
    }
}