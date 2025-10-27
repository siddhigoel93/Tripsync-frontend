package com.example.tripsync.api

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Properties
import java.io.IOException
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val DEFAULT_BASE_URL = "http://51.20.254.52/"

    private fun getBaseUrl(context: Context): String {
        val properties = Properties()
        try {
            context.assets.open("local.properties").use { inputStream ->
                properties.load(inputStream)
                val base = properties.getProperty("BASE_URL")
                if (!base.isNullOrBlank()) {
                    return if (base.endsWith("/")) base else "$base/"
                }
            }
        } catch (e: IOException) {
        }
        return DEFAULT_BASE_URL
    }

    private fun createRetrofit(context: Context): Retrofit {
        val baseUrl = getBaseUrl(context)
        val client = OkHttpClient.Builder()
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getAuthService(context: Context): AuthService {
        val retrofit = createRetrofit(context)
        return retrofit.create(AuthService::class.java)
    }
}
