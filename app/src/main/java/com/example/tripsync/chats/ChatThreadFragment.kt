package com.example.tripsync

import android.content.Context
import android.os.Bundle
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
import com.example.tripsync.websocket.WebSocketManager
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChatThreadFragment : Fragment() {

    private var conversationId: Int = -1
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: MessagesAdapter
    private var webSocketManager: WebSocketManager? = null
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_chat_thread, container, false)

        val back = v.findViewById<ImageView>(R.id.toolbar_back)
        val profileImg = v.findViewById<ImageView>(R.id.toolbar_profile)
        val nameTv = v.findViewById<TextView>(R.id.toolbar_name)


        val args = requireArguments()
        nameTv.text = args.getString("name", "")
        conversationId = args.getInt("conversationId", -1)

        back.setOnClickListener { requireActivity().onBackPressed() }

        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val selfId = prefs.getInt("self_id", -1)

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
        if (conversationId == -1) return

        lifecycleScope.launch {
            try {
                val chatApi = ApiClient.createService(requireContext(), ChatApi::class.java)
                val response = chatApi.getMessages(conversationId)
                if (response.isSuccessful) {
                    val messages = response.body()?.data ?: emptyList()
                    adapter.updateMessages(messages)
                    recycler.scrollToPosition(messages.size - 1)
                } else {
                    Toast.makeText(requireContext(), "Failed to load messages", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun connectWebSocket() {
        if (conversationId == -1) return

        val sp = requireContext().getSharedPreferences("auth" , Context.MODE_PRIVATE)
        val token = sp.getString("access_token" , null) ?: return

        val wsUrl = "wss://51.20.254.52/ws/chat/$conversationId/?token=$token"

        webSocketManager = WebSocketManager(wsUrl, object : WebSocketManager.WebSocketEvents {
            override fun onOpen() {
                Toast.makeText(requireContext(), "Connected to chat", Toast.LENGTH_SHORT).show()
            }

            override fun onMessageReceived(message: String) {
                val json = JSONObject(message)
                val data = json.optJSONObject("data") ?: return
                val msg = Message(
                    id = data.optInt("id"),
                    content = data.optString("content"),
                    sender = com.example.tripsync.api.models.MessageSender(
                        id = data.getJSONObject("sender").optInt("id"),
                        email = data.getJSONObject("sender").optString("email")
                    ),
                    timestamp = data.optString("timestamp")
                )
                requireActivity().runOnUiThread {
                    adapter.addMessage(msg)
                    recycler.scrollToPosition(adapter.itemCount - 1)
                }
            }

            override fun onFailure(t: Throwable) {
                Toast.makeText(requireContext(), "WebSocket error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
        webSocketManager?.connect()
    }
    private fun sendMessage(content: String) {
        if (webSocketManager == null) return

        val json = JSONObject()
        json.put("content", content)
        json.put("conversation_id", conversationId)

        webSocketManager?.sendMessage(json.toString())
    }


    override fun onDestroyView() {
        super.onDestroyView()
        webSocketManager?.disconnect()
    }
}
