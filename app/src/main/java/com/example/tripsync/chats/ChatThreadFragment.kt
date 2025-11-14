package com.example.tripsync

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.MessagesAdapter
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.ChatApi
import com.example.tripsync.api.models.SendMessageRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ChatThreadFragment : Fragment() {

    private lateinit var messagesRecycler: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var chatName: TextView
    private lateinit var chatAvatar: ImageView
    private lateinit var adapter: MessagesAdapter

    private var conversationId: Int = -1
    private var currentUserId: Int = -1
    private var isGroup: Boolean = false
    private var autoRefreshJob: Job? = null
    private var isSending: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_chat_thread, container, false)

        // Get arguments
        conversationId = arguments?.getInt("conversationId", -1) ?: -1
        val name = arguments?.getString("name", "Chat") ?: "Chat"
        isGroup = arguments?.getBoolean("isGroup", false) ?: false

        Log.d("ChatThread", "Opening conversation ID: $conversationId, Name: $name, IsGroup: $isGroup")

        // Get current user ID
        val sharedPref = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        currentUserId = sharedPref.getString("self_id", "-1")?.toIntOrNull() ?: -1

        Log.d("ChatThread", "Current User ID: $currentUserId")

        // Initialize views
        messagesRecycler = view.findViewById(R.id.recycler_messages)
        messageInput = view.findViewById(R.id.message_edit_text)
        sendButton = view.findViewById(R.id.button_send)
        chatName = view.findViewById(R.id.toolbar_name)
        chatAvatar = view.findViewById(R.id.toolbar_profile)

        val backButton = view.findViewById<ImageView>(R.id.toolbar_back)

        // Set name
        chatName.text = name

        // Setup back button
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Setup RecyclerView
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        messagesRecycler.layoutManager = layoutManager

        adapter = MessagesAdapter(mutableListOf(), currentUserId, isGroup)
        messagesRecycler.adapter = adapter

        // Setup send button
        sendButton.setOnClickListener {
            sendMessage()
        }

        // Load messages immediately
        loadMessages(scrollToBottom = true, showToast = true)

        return view
    }

    override fun onResume() {
        super.onResume()
        Log.d("ChatThread", "onResume - starting auto-refresh")
        // Start auto-refresh when fragment becomes visible
        startAutoRefresh()
    }

    override fun onPause() {
        super.onPause()
        Log.d("ChatThread", "onPause - stopping auto-refresh")
        // Stop auto-refresh when fragment is not visible
        stopAutoRefresh()
    }

    private fun sendMessage() {
        val content = messageInput.text.toString().trim()

        if (content.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a message", Toast.LENGTH_SHORT).show()
            return
        }

        if (isSending) {
            return // Prevent double-sending
        }

        Log.d("ChatThread", "Sending message: $content")

        // Disable input while sending
        isSending = true
        messageInput.isEnabled = false
        sendButton.isEnabled = false

        lifecycleScope.launch {
            try {
                val chatApi = ApiClient.createService(requireContext(), ChatApi::class.java)
                val request = SendMessageRequest(content)

                val response = chatApi.sendMessage(conversationId, request)

                Log.d("ChatThread", "Send response code: ${response.code()}")

                if (response.isSuccessful) {
                    val message = response.body()
                    Log.d("ChatThread", "Message sent successfully: ${message?.id}")

                    messageInput.text.clear()

                    // Immediately load messages after sending
                    delay(100) // Small delay to ensure backend has processed
                    loadMessages(scrollToBottom = true, showToast = false)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ChatThread", "Failed to send message: ${response.code()} - $errorBody")
                    Toast.makeText(requireContext(), "Failed to send message: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ChatThread", "Error sending message", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isSending = false
                messageInput.isEnabled = true
                sendButton.isEnabled = true
            }
        }
    }

    private fun loadMessages(scrollToBottom: Boolean = false, showToast: Boolean = false) {
        lifecycleScope.launch {
            try {
                val chatApi = ApiClient.createService(requireContext(), ChatApi::class.java)

                Log.d("ChatThread", "Loading messages for conversation: $conversationId")

                val response = chatApi.getMessages(conversationId)

                Log.d("ChatThread", "Load messages response code: ${response.code()}")

                if (response.isSuccessful) {
                    val messages = response.body() ?: emptyList()

                    Log.d("ChatThread", "Loaded ${messages.size} messages")

                    val previousCount = adapter.itemCount
                    val hasNewMessages = messages.size > previousCount

                    if (messages.isEmpty()) {
                        Log.d("ChatThread", "No messages in conversation")
                        // Don't hide recycler, just show it's empty
                        messagesRecycler.visibility = View.VISIBLE
                        adapter.updateMessages(emptyList())
                    } else {
                        Log.d("ChatThread", "Updating adapter with messages")
                        messagesRecycler.visibility = View.VISIBLE
                        adapter.updateMessages(messages)

                        // Scroll to bottom if requested or if there are new messages
                        if (scrollToBottom || hasNewMessages) {
                            messagesRecycler.post {
                                messagesRecycler.scrollToPosition(adapter.itemCount - 1)
                            }
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ChatThread", "Failed to load messages: ${response.code()} - $errorBody")

                    if (showToast) {
                        Toast.makeText(requireContext(), "Failed to load messages: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatThread", "Error loading messages", e)

                if (showToast) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startAutoRefresh() {
        stopAutoRefresh() // Stop any existing refresh

        autoRefreshJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                delay(2000) // Refresh every 2 seconds
                if (!isSending) { // Don't refresh while sending
                    Log.d("ChatThread", "Auto-refreshing messages...")
                    loadMessages(scrollToBottom = false, showToast = false)
                }
            }
        }
    }

    private fun stopAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAutoRefresh()
    }
}