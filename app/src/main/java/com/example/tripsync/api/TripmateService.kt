package com.example.tripsync.api

import com.example.tripsync.api.models.friend_FriendRequestBody
import com.example.tripsync.api.models.friend_FriendRequestResponse
import com.example.tripsync.api.models.friend_TripmateSearchResult
import com.example.tripsync.api.models.friend_MyTripmateResponseItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TripmateService {

    @GET("api/tripmate/search/")
    suspend fun searchUsers(@Query("q") q: String): Response<List<friend_TripmateSearchResult>>

    @POST("api/tripmate/friend-request/send/")
    suspend fun sendFriendRequest(@Body body: friend_FriendRequestBody): Response<friend_FriendRequestResponse>

    @GET("api/tripmate/my-tripmates/")
    suspend fun getMyTripmates(): Response<List<friend_MyTripmateResponseItem>>
}
