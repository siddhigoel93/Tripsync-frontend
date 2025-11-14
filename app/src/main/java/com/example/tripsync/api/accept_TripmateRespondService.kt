package com.example.tripsync.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

data class accept_RespondBody(val action: String)

interface accept_TripmateRespondService {
    @POST("api/tripmate/friend-request/{request_id}/respond/")
    suspend fun respond(
        @Path("request_id") requestId: Int,
        @Body body: accept_RespondBody
    ): Response<Void>
}
