package com.example.tripsync.api

import android.content.Context
import android.util.Log
import com.example.tripsync.api.interceptors.AuthInterceptor
import com.example.tripsync.BuildConfig
import okhttp3.Interceptor
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
        } catch (_: IOException) {}
        return DEFAULT_BASE_URL
    }

    private fun buildRetrofit(context: Context, secure: Boolean): Retrofit {
        val baseUrl = getBaseUrl(context)

        val httpLogging = HttpLoggingInterceptor { message -> Log.i("OkHttp", message) }
        httpLogging.level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE

        val requestLogger = Interceptor { chain ->
            val request = chain.request()
            val t1 = System.nanoTime()
            Log.i("ApiClient", "Request: ${request.method} ${request.url}")
            val response = chain.proceed(request)
            val t2 = System.nanoTime()
            Log.i("ApiClient", "Response: ${response.code} ${response.request.url} in ${(t2 - t1) / 1_000_000}ms")
            response
        }

        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .addInterceptor(requestLogger)

        if (BuildConfig.DEBUG) clientBuilder.addInterceptor(httpLogging)
        if (secure) clientBuilder.addInterceptor(AuthInterceptor(context.applicationContext))

        val client = clientBuilder.build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getRetrofitInstance(context: Context, secure: Boolean): Retrofit {
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
        return getRetrofitInstance(context, secure = true).create(serviceClass)
    }

    fun getItineraryService(context: Context): ItineraryService {
        return getRetrofitInstance(context, secure = true).create(ItineraryService::class.java)
    }

    fun getBudgetService(context: Context): com.example.tripsync.api.budget.BudgetService {
        return getRetrofitInstance(context, secure = true).create(com.example.tripsync.api.budget.BudgetService::class.java)
    }

    fun getTripmateService(context: Context): TripmateService {
        return getRetrofitInstance(context, secure = true).create(TripmateService::class.java)
    }
}
