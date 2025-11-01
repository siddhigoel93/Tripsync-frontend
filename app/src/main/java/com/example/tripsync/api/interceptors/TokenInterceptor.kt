package com.example.tripsync.api

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = getTokenFromPrefs(context)
        return if (!token.isNullOrEmpty()) {
            val newRequest = originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()

            Log.d("TokenInterceptor", "Attached token to ${originalRequest.url}")
            chain.proceed(newRequest)
        } else {
            Log.w("TokenInterceptor", "⚠No token found — proceeding without Authorization header.")
            chain.proceed(originalRequest)
        }
    }

    private fun getTokenFromPrefs(context: Context): String? {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return prefs.getString("access_token", null)
    }
}
