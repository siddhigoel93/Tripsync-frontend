package com.example.tripsync.api

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.Properties
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val DEFAULT_BASE_URL = "http://51.20.254.52/"
    @Volatile private var retrofit: Retrofit? = null

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
            Log.e("ApiClient", "Error loading BASE_URL", e)
        }
        return DEFAULT_BASE_URL
    }

    private fun buildRetrofit(context: Context): Retrofit {
        val baseUrl = getBaseUrl(context)
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context.applicationContext))
            .addInterceptor(logging)
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
        val r = retrofit ?: synchronized(this) {
            retrofit ?: buildRetrofit(context).also { retrofit = it }
        }
        return r.create(AuthService::class.java)
    }
}
