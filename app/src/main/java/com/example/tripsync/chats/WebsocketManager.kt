package com.example.tripsync.websocket

import android.util.Log
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit
import org.json.JSONObject

class WebSocketManager(
    private val conversationId: String,
    private val token: String,
    private val listener: WebSocketListenerInterface
) {

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .pingInterval(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private var webSocket: WebSocket? = null
    private var isConnected = false

    fun connect() {
        val wsUrl = "wss://51.20.254.52/ws/chat/$conversationId/?token=$token"

        val request = Request.Builder()
            .url(wsUrl)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                Log.d("WebSocket", "Connected to chat $conversationId")
                listener.onConnected()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Message received: $text")
                listener.onMessageReceived(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                Log.d("WebSocket", "Binary message ignored: $bytes")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                Log.d("WebSocket", "Closing: $reason")
                webSocket.close(1000, null)
                listener.onDisconnected()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                Log.e("WebSocket", "Error: ${t.message}")
                listener.onError(t)
                reconnect()
            }
        })
    }

    fun sendMessage(sender: String, message: String) {
        if (!isConnected) {
            Log.w("WebSocket", "Can't send — not connected")
            return
        }

        try {
            val json = JSONObject()
            json.put("sender", sender)
            json.put("message", message)
            webSocket?.send(json.toString())
            Log.d("WebSocket", "Message sent: $json")
        } catch (e: Exception) {
            Log.e("WebSocket", "Send failed: ${e.message}")
        }
    }

    fun disconnect() {
        isConnected = false
        webSocket?.close(1000, "Client disconnected")
        webSocket = null
        Log.d("WebSocket", "Disconnected manually")
    }

    private fun reconnect() {
        Log.d("WebSocket", "Reconnecting in 5 seconds...")
        Thread.sleep(5000)
        connect()
    }

    interface WebSocketListenerInterface {
        fun onConnected()
        fun onMessageReceived(message: String)
        fun onDisconnected()
        fun onError(t: Throwable)
    }
}
