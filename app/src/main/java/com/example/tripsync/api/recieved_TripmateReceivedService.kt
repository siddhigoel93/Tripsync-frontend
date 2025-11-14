package com.example.tripsync.api

import com.example.tripsync.api.models.recieved_FriendRequestItem
import com.example.tripsync.api.models.recieved_RespondBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface recieved_TripmateReceivedService {
    @GET("api/tripmate/friend-request/received/")
    suspend fun getReceivedRequests(): Response<List<recieved_FriendRequestItem>>

    @POST("api/tripmate/friend-request/respond/")
    suspend fun respondToRequest(@Body body: recieved_RespondBody): Response<Void>
}
