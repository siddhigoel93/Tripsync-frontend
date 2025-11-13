package com.example.tripsync

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.adapters.MessagesAdapter
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.ChatApi
import com.example.tripsync.api.models.Message
import com.example.tripsync.api.models.SendMessageRequest
// import com.example.tripsync.websocket.WebSocketManager // COMMENTED OUT - WebSocket
// import org.json.JSONObject // COMMENTED OUT - WebSocket
import kotlinx.coroutines.launch

class ChatThreadFragment : Fragment() {
    // Removed WebSocketListenerInterface - using REST API only

    private var conversationId: Int = -1
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: MessagesAdapter
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageView
    private lateinit var conversationNameTv: TextView
    private lateinit var toolbarOptions: ImageView

    // COMMENTED OUT - WebSocket
    // private var webSocketManager: WebSocketManager? = null

    private var selfEmail: String? = null
    private var selfId: Int = -1
    private var isEditingMessage = false
    private var editingMessageId: Int = -1

    // Auto-refresh handler
    private val refreshHandler = Handler(Looper.getMainLooper())
    private var isRefreshing = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_chat_thread, container, false)

        val back = v.findViewById<ImageView>(R.id.toolbar_back)
        conversationNameTv = v.findViewById<TextView>(R.id.toolbar_name)
        toolbarOptions = v.findViewById<ImageView>(R.id.toolbar_options) // Add this to your layout if missing

        val args = requireArguments()
        conversationNameTv.text = args.getString("name", "")
        conversationId = args.getInt("conversationId", -1)

        Log.d("ChatThread", "ConversationId: $conversationId")

        back.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        // Options menu - conversation details, leave conversation
        toolbarOptions.setOnClickListener {
            showConversationOptionsMenu()
        }

        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
//        selfId = prefs.getString("self_id", "-1")?.toIntOrNull() ?: -1
//        Log.d("ChatThread", "SelfId: $selfId")
        selfId = try {
            prefs.getInt("self_id", -1)
        } catch (e: ClassCastException) {
            val stringId = prefs.getString("self_id", "-1")
            val fixedId = stringId?.toIntOrNull() ?: -1

            // rewrite it properly as Int for next time
            prefs.edit().putInt("self_id", fixedId).apply()
            fixedId
        }
        selfEmail = prefs.getString("currentUserEmail", null)

        recycler = v.findViewById(R.id.recycler_messages)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        // Pass message click listener for edit/delete
        adapter = MessagesAdapter(
            messages = mutableListOf(),
            selfId = selfId,
            onMessageLongClick = { message -> showMessageOptionsDialog(message) }
        )
        recycler.adapter = adapter

        loadMessages()

        // COMMENTED OUT - WebSocket connection
        // connectWebSocket()

        // Start auto-refresh (every 3 seconds)
        startAutoRefresh()

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        messageEditText = view.findViewById(R.id.message_edit_text)
        sendButton = view.findViewById(R.id.button_send)

        sendButton.setOnClickListener {
            val content = messageEditText.text.toString().trim()
            if (content.isNotEmpty()) {
                if (isEditingMessage) {
                    editMessage(editingMessageId, content)
                } else {
                    sendMessage(content)
                }
                messageEditText.text.clear()
                cancelEditMode()
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

    // COMMENTED OUT - WebSocket connection
    /*
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
    */

    private fun sendMessage(content: String) {
        lifecycleScope.launch {
            try {
                val chatApi = ApiClient.createService(requireContext(), ChatApi::class.java)
                val request = SendMessageRequest(content = content)
                val response = chatApi.sendMessage(conversationId, request)

                if (response.isSuccessful) {
                    val message = response.body()
                    if (message != null) {
                        adapter.addMessage(message)
                        recycler.scrollToPosition(adapter.itemCount - 1)
                        Toast.makeText(requireContext(), "Message sent", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to send message", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ChatThread", "Error sending message: ${e.message}", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // NEW: Edit message API
    private fun editMessage(messageId: Int, newContent: String) {
        lifecycleScope.launch {
            try {
                val chatApi = ApiClient.createService(requireContext(), ChatApi::class.java)
                val request = SendMessageRequest(content = newContent)
                val response = chatApi.editMessage(conversationId, messageId, request)

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Message edited", Toast.LENGTH_SHORT).show()
                    loadMessages() // Refresh to show edited message
                } else {
                    Toast.makeText(requireContext(), "Failed to edit message", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ChatThread", "Error editing message: ${e.message}", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // NEW: Delete message API
    private fun deleteMessage(messageId: Int) {
        lifecycleScope.launch {
            try {
                val chatApi = ApiClient.createService(requireContext(), ChatApi::class.java)
                val response = chatApi.deleteMessage(conversationId, messageId)

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Message deleted", Toast.LENGTH_SHORT).show()
                    loadMessages() // Refresh to remove deleted message
                } else {
                    Toast.makeText(requireContext(), "Failed to delete message", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ChatThread", "Error deleting message: ${e.message}", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // NEW: Get message details API
    private fun getMessageDetails(messageId: Int) {
        lifecycleScope.launch {
            try {
                val chatApi = ApiClient.createService(requireContext(), ChatApi::class.java)
                val response = chatApi.getMessageDetails(conversationId, messageId)

                if (response.isSuccessful) {
                    val message = response.body()
                    if (message != null) {
                        showMessageDetailsDialog(message)
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load message details", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ChatThread", "Error loading message details: ${e.message}", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // NEW: Show message options (Edit, Delete, Details)
    private fun showMessageOptionsDialog(message: Message) {
        val options = if (message.sender.id == selfId) {
            arrayOf("Edit", "Delete", "View Details")
        } else {
            arrayOf("View Details")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Message Options")
            .setItems(options) { _, which ->
                when (options[which]) {
                    "Edit" -> enterEditMode(message)
                    "Delete" -> confirmDeleteMessage(message.id)
                    "View Details" -> getMessageDetails(message.id)
                }
            }
            .show()
    }

    private fun enterEditMode(message: Message) {
        isEditingMessage = true
        editingMessageId = message.id
        messageEditText.setText(message.content)
        messageEditText.requestFocus()
        sendButton.setImageResource(android.R.drawable.ic_menu_edit) // Change icon to edit
        Toast.makeText(requireContext(), "Editing message", Toast.LENGTH_SHORT).show()
    }

    private fun cancelEditMode() {
        isEditingMessage = false
        editingMessageId = -1
        sendButton.setImageResource(android.R.drawable.ic_menu_send) // Back to send icon
    }

    private fun confirmDeleteMessage(messageId: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Message")
            .setMessage("Are you sure you want to delete this message?")
            .setPositiveButton("Delete") { _, _ ->
                deleteMessage(messageId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMessageDetailsDialog(message: Message) {
        val details = """
            Message ID: ${message.id}
            Sender: ${message.sender.email}
            Content: ${message.content}
            Timestamp: ${message.timestamp}
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Message Details")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }

    // NEW: Conversation options menu
    private fun showConversationOptionsMenu() {
        val options = arrayOf("View Conversation Details", "Leave Conversation")

        AlertDialog.Builder(requireContext())
            .setTitle("Conversation Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> loadConversationDetails()
                    1 -> confirmLeaveConversation()
                }
            }
            .show()
    }

    // NEW: Get conversation details API
    private fun loadConversationDetails() {
        lifecycleScope.launch {
            try {
                val chatApi = ApiClient.createService(requireContext(), ChatApi::class.java)
                val response = chatApi.getConversationDetails(conversationId)

                if (response.isSuccessful) {
                    val conversation = response.body()
                    if (conversation != null) {
                        showConversationDetailsDialog(conversation)
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load conversation details", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ChatThread", "Error loading conversation details: ${e.message}", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showConversationDetailsDialog(conversation: com.example.tripsync.api.models.Conversation) {
        val participants = conversation.participants.joinToString("\n") { "• ${it.email}" }
        val details = """
            Conversation ID: ${conversation.id}
            Name: ${conversation.name}
            Is Group: ${conversation.is_group ?: false}
            Message Count: ${conversation.message_count ?: 0}
            Created: ${conversation.created_at ?: "N/A"}
            Updated: ${conversation.updated_at}
            
            Participants:
            $participants
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Conversation Details")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }

    // NEW: Leave conversation API
    private fun confirmLeaveConversation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Leave Conversation")
            .setMessage("Are you sure you want to leave this conversation?")
            .setPositiveButton("Leave") { _, _ ->
                leaveConversation()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun leaveConversation() {
        lifecycleScope.launch {
            try {
                val chatApi = ApiClient.createService(requireContext(), ChatApi::class.java)
                val response = chatApi.leaveConversation(conversationId)

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Left conversation", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                } else {
                    Toast.makeText(requireContext(), "Failed to leave conversation", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ChatThread", "Error leaving conversation: ${e.message}", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startAutoRefresh() {
        isRefreshing = true
        refreshHandler.postDelayed(object : Runnable {
            override fun run() {
                if (isRefreshing) {
                    loadMessages()
                    refreshHandler.postDelayed(this, 3000)
                }
            }
        }, 3000)
    }

    private fun stopAutoRefresh() {
        isRefreshing = false
        refreshHandler.removeCallbacksAndMessages(null)
    }

    // COMMENTED OUT - WebSocket callbacks
    /*
    override fun onConnected() {
        requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), "Connected to chat", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMessageReceived(message: String) {
        try {
            val json = JSONObject(message)
            val msg = Message(
                id = json.optInt("id", 0),
                content = json.optString("message", ""),
                sender = MessageSender(
                    id = json.optInt("sender_id", 0),
                    email = json.optString("sender", "")
                ),
                timestamp = json.optString("timestamp", "")
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
    */

    override fun onDestroyView() {
        super.onDestroyView()
        stopAutoRefresh()
        // COMMENTED OUT - WebSocket disconnect
        // webSocketManager?.disconnect()
    }
}