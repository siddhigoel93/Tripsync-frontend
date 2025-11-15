package com.example.tripsync.api.interceptors

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val appContext: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        val original = chain.request()
        val builder = original.newBuilder()

        val incomingAuth = original.header("Authorization")

        if (incomingAuth.isNullOrBlank()) {

            val sp = appContext.getSharedPreferences("auth", Context.MODE_PRIVATE)
            val token = sp.getString("access_token", null)

            if (!token.isNullOrEmpty()) {
                builder.addHeader("Authorization", "Bearer $token")
                Log.d("AuthInterceptor", "Added Authorization header with access_token")
            } else {
                Log.d("AuthInterceptor", "access_token NOT FOUND in SharedPreferences('auth')")
            }

        } else {
            Log.d("AuthInterceptor", "Request already has Authorization header, skipping.")
        }

        return chain.proceed(builder.build())
    }
}
