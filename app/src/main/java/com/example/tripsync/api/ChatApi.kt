package com.example.tripsync.api

import com.example.tripsync.api.models.Conversation
import com.example.tripsync.api.models.CreateConversationRequest
import com.example.tripsync.api.models.CreateConversationResponse
import com.example.tripsync.api.models.Message
import com.example.tripsync.api.models.MessagesResponse
import com.example.tripsync.api.models.SendMessageRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatApi {
    @GET("/api/chat/conversations/")
    suspend fun getConversations():Response<List<Conversation>>

    @GET("/api/chat/conversations/{id}/messages/")
    suspend fun getMessages(@Path("id") conversationId: Int): Response<List<Message>>

    @POST("/api/chat/conversations/")
    suspend fun createConversation(
        @Body body: CreateConversationRequest
    ): Response<Conversation>

    @POST("/api/chat/conversations/{conversationId}/messages/")
    suspend fun sendMessage(
        @Path("conversationId") conversationId: Int,
        @Body request: SendMessageRequest
    ): Response<Message>

}
