package com.example.tripsync.api

import com.example.tripsync.api.models.*
import retrofit2.Response
import retrofit2.http.*

interface ChatApi {
    @GET("/api/chat/conversations/")
    suspend fun getConversations(): Response<List<Conversation>>

    @POST("/api/chat/conversations/")
    suspend fun createConversation(
        @Body body: CreateConversationRequest
    ): Response<Conversation>

    @GET("/api/chat/conversations/{id}/")
    suspend fun getConversationDetails(@Path("id") conversationId: Int): Response<Conversation>

    @DELETE("/api/chat/conversations/{id}/")
    suspend fun leaveConversation(@Path("id") conversationId: Int): Response<Unit>

    @GET("/api/chat/conversations/{conversation_id}/messages/")
    suspend fun getMessages(@Path("conversation_id") conversationId: Int): Response<List<Message>>

    @POST("/api/chat/conversations/{conversation_id}/messages/")
    suspend fun sendMessage(
        @Path("conversation_id") conversationId: Int,
        @Body request: SendMessageRequest
    ): Response<Message>

    @GET("/api/chat/conversations/{conversation_id}/messages/{id}/")
    suspend fun getMessageDetails(
        @Path("conversation_id") conversationId: Int,
        @Path("id") messageId: Int
    ): Response<Message>

    @PATCH("/api/chat/conversations/{conversation_id}/messages/{id}/")
    suspend fun editMessage(
        @Path("conversation_id") conversationId: Int,
        @Path("id") messageId: Int,
        @Body request: SendMessageRequest
    ): Response<Message>

    @DELETE("/api/chat/conversations/{conversation_id}/messages/{id}/")
    suspend fun deleteMessage(
        @Path("conversation_id") conversationId: Int,
        @Path("id") messageId: Int
    ): Response<Unit>
}