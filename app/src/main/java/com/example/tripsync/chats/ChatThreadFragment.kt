package com.example.tripsync

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.adapters.MessagesAdapter
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.ChatApi
import com.example.tripsync.api.models.Message
import com.example.tripsync.api.models.MessageSender
import com.example.tripsync.websocket.WebSocketManager
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChatThreadFragment : Fragment(), WebSocketManager.WebSocketListenerInterface {

    private var conversationId: Int = -1
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: MessagesAdapter
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageView
    private var webSocketManager: WebSocketManager? = null
    private var selfEmail: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_chat_thread, container, false)

        val back = v.findViewById<ImageView>(R.id.toolbar_back)
        val nameTv = v.findViewById<TextView>(R.id.toolbar_name)

        val args = requireArguments()
        nameTv.text = args.getString("name", "")
        conversationId = args.getInt("conversationId", -1)

        Log.d("ChatThread", "ConversationId: $conversationId")

        back.setOnClickListener { requireActivity().onBackPressed() }

        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val selfId = prefs.getInt("self_id", -1)
        selfEmail = prefs.getString("currentUserEmail", null)

        recycler = v.findViewById(R.id.recycler_messages)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = MessagesAdapter(mutableListOf(), selfId)
        recycler.adapter = adapter

        loadMessages()
        connectWebSocket()

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        messageEditText = view.findViewById(R.id.message_edit_text)
        sendButton = view.findViewById(R.id.button_send)

        sendButton.setOnClickListener {
            val content = messageEditText.text.toString().trim()
            if (content.isNotEmpty()) {
                sendMessage(content)
                messageEditText.text.clear()
            }
        }
    }

    private fun loadMessages() {
        if (conversationId == -1) {
            Log.e("ChatThread", "Invalid conversation ID")
            return
        }

        lifecycleScope.launch {
            try {
                val chatApi = ApiClient.createService(requireContext(), ChatApi::class.java)
                val response = chatApi.getMessages(conversationId)

                if (response.isSuccessful) {
                    val messages = response.body() ?: emptyList()
                    adapter.updateMessages(messages)
                    if (messages.isNotEmpty()) {
                        recycler.scrollToPosition(messages.size - 1)
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load messages", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ChatThread", "Error loading messages: ${e.message}", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun connectWebSocket() {
        if (conversationId == -1) {
            Log.e("ChatThread", "Cannot connect WebSocket: Invalid conversation ID")
            return
        }

        val sp = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
        val token = sp.getString("access_token", null)

        if (token.isNullOrEmpty()) {
            Log.e("ChatThread", "Cannot connect WebSocket: No token found")
            Toast.makeText(requireContext(), "Missing token", Toast.LENGTH_SHORT).show()
            return
        }

        webSocketManager = WebSocketManager(conversationId.toString(), token, this)
        webSocketManager?.connect()
    }

    private fun sendMessage(content: String) {
        val sender = selfEmail ?: "unknown@user.com"
        webSocketManager?.sendMessage(sender, content)
            ?: Toast.makeText(requireContext(), "WebSocket not connected", Toast.LENGTH_SHORT).show()
    }

    override fun onConnected() {
        requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), "Connected to chat", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMessageReceived(message: String) {
        try {
            val json = JSONObject(message)
            val msg = Message(
                id = json.optInt("id"),
                content = json.optString("message"),
                sender = MessageSender(
                    id = json.optInt("sender_id"),
                    email = json.optString("sender")
                ),
                timestamp = json.optString("timestamp")
            )

            requireActivity().runOnUiThread {
                adapter.addMessage(msg)
                recycler.scrollToPosition(adapter.itemCount - 1)
            }
        } catch (e: Exception) {
            Log.e("ChatThread", "Error parsing incoming message: ${e.message}", e)
        }
    }

    override fun onDisconnected() {
        requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), "Disconnected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onError(t: Throwable) {
        Log.e("ChatThread", "WebSocket error: ${t.message}", t)
        requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), "WebSocket Error: ${t.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webSocketManager?.disconnect()
    }
}
