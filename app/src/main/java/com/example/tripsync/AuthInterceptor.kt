package com.example.tripsync.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val appContext: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()


        val hasAuth = original.header("Authorization") != null
        if (!hasAuth) {
            val sp = appContext.getSharedPreferences("auth", Context.MODE_PRIVATE)
            val token = sp.getString("access_token", null)
            if (!token.isNullOrEmpty()) {
                builder.addHeader("Authorization", "Bearer $token")
            }
        }



        return chain.proceed(builder.build())
    }
}
