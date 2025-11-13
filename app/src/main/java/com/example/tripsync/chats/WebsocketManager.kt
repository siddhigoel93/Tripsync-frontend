package com.example.tripsync

import android.util.Log
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit
import org.json.JSONObject
import kotlinx.coroutines.*

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
    private var reconnectJob: Job? = null

    fun connect() {
        val wsUrl = "ws://51.20.254.52/ws/chat/$conversationId/?token=$token"

        Log.d("WebSocket", "Connecting to: $wsUrl")

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

                try {
                    val json = JSONObject(text)
                    if (json.optString("type") == "error") {
                        Log.e("WebSocket", "Backend error: ${json.optString("message")}")
                    }
                } catch (e: Exception) {
                    Log.e("WebSocket", "JSON parse error: ${e.message}")
                }

                listener.onMessageReceived(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                Log.d("WebSocket", "Binary message ignored: ${bytes.hex()}")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.w("WebSocket", "Closing: $reason (code: $code)")
                isConnected = false
                webSocket.close(1000, null)
                listener.onDisconnected()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                Log.e("WebSocket", "Failure: ${t.message}")
                listener.onError(t)
                reconnect()
            }
        })
    }

    fun sendMessage(message: String) {
        if (!isConnected) {
            Log.w("WebSocket", "Can't send — not connected")
            return
        }

        try {
            val format = JSONObject().apply {
                put("type", "chat_message")
                put("message", message)
            }

            Log.d("WebSocket", "Sending message: $format")
            webSocket?.send(format.toString())

        } catch (e: Exception) {
            Log.e("WebSocket", "Send failed: ${e.message}")
        }
    }

    fun disconnect() {
        isConnected = false
        reconnectJob?.cancel()
        webSocket?.close(1000, "Client disconnected")
        webSocket = null
        Log.d("WebSocket", "Disconnected manually")
    }

    private fun reconnect() {
        if (reconnectJob?.isActive == true) return

        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            Log.d("WebSocket", " Reconnecting in 5 seconds...")
            delay(5000)
            connect()
        }
    }

    interface WebSocketListenerInterface {
        fun onConnected()
        fun onMessageReceived(message: String)
        fun onDisconnected()
        fun onError(t: Throwable)
    }
}
