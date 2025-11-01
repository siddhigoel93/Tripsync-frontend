package com.example.tripsync.api

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object SecureApiClient {
    private const val DEFAULT_BASE_URL = "http://51.20.254.52/"

    private fun createSecureRetrofit(context: Context): Retrofit {
        val logging = HttpLoggingInterceptor { message ->
            Log.d("SecureApiClient", message)
        }.apply { level = HttpLoggingInterceptor.Level.BODY }

        val client = OkHttpClient.Builder()
            .addInterceptor(TokenInterceptor(context)) // <-- Auto adds Bearer token
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(DEFAULT_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getSecureProfileService(context: Context): ProfileService {
        val retrofit = createSecureRetrofit(context)
        return retrofit.create(ProfileService::class.java)
    }
}
