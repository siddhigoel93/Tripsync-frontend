package com.example.tripsync.api

import android.content.Context
import android.util.Log
import com.example.tripsync.api.interceptors.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.Properties
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val DEFAULT_BASE_URL = "http://51.20.254.52/"
    private var retrofitSecure: Retrofit? = null
    private var retrofitInsecure: Retrofit? = null


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

    private fun buildRetrofit(context: Context, secure: Boolean): Retrofit {
        val baseUrl = getBaseUrl(context)
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val clientBuilder = OkHttpClient.Builder()
//            .addInterceptor(AuthInterceptor(context.applicationContext))
//            .addInterceptor(logging)
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)


        if (secure) {
            clientBuilder.addInterceptor(AuthInterceptor(context.applicationContext))
        }

        val client = clientBuilder.build()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun getRetrofitInstance(context: Context, secure: Boolean): Retrofit {
        return if (secure) {
            retrofitSecure ?: synchronized(this) {
                retrofitSecure ?: buildRetrofit(context, true).also { retrofitSecure = it }
            }
        } else {
            retrofitInsecure ?: synchronized(this) {
                retrofitInsecure ?: buildRetrofit(context, false).also { retrofitInsecure = it }
            }
        }
    }

    fun getAuthService(context: Context): AuthService {
        return getRetrofitInstance(context, secure = false).create(AuthService::class.java)
    }
    fun getTokenService(context: Context): AuthService {
        return getRetrofitInstance(context, secure = true).create(AuthService::class.java)
    }

    fun <T> createService(context: Context, serviceClass: Class<T>): T {
        return getRetrofitInstance(context, secure = false).create(serviceClass)
    }
    fun getItineraryService(context: Context): ItineraryService {
        return getRetrofitInstance(context, secure = true).create(ItineraryService::class.java)
    }
}
