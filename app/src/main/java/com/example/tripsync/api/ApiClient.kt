package com.example.tripsync.api

import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Properties

object ApiClient {

    private fun getBaseUrl(context: Context): String {
        val properties = Properties()
        context.assets.open("local.properties").use { inputStream ->
            properties.load(inputStream)
        }
        return properties.getProperty("BASE_URL") ?: ""
    }

    fun getRetrofit(context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(getBaseUrl(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
