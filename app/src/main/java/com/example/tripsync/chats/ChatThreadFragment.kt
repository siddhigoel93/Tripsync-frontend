package com.example.tripsync

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
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
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.ChatApi
import com.example.tripsync.api.models.EditMessageRequest
import com.example.tripsync.api.models.Message
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
    private lateinit var menuButton: ImageView
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
        menuButton = view.findViewById(R.id.toolbar_options)
        val backButton = view.findViewById<ImageView>(R.id.toolbar_back)

        // Set name
        chatName.text = name

        // Setup back button
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Setup menu button
        menuButton.setOnClickListener {
            showConversationOptions()
        }

        // Setup RecyclerView
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        messagesRecycler.layoutManager = layoutManager

        adapter = MessagesAdapter(
            mutableListOf(),
            currentUserId,
            isGroup,
            onEditMessage = { message, newContent -> editMessage(message, newContent) },
            onDeleteMessage = { message -> deleteMessage(message) }
        )
        messagesRecycler.adapter = adapter

        // Setup send button
        sendButton.setOnClickListener {
            sendMessage()
        }

        // Handle Enter key press (optional: send on Enter)
        messageInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }

        // Load messages immediately
        loadMessages(scrollToBottom = true, showToast = true)

        return view
    }

    override fun onResume() {
        super.onResume()
        Log.d("ChatThread", "onResume - starting auto-refresh and reloading messages")
        loadMessages(scrollToBottom = true, showToast = false)
        startAutoRefresh()
    }

    override fun onPause() {
        super.onPause()
        Log.d("ChatThread", "onPause - stopping auto-refresh")
        stopAutoRefresh()
    }

    private fun showConversationOptions() {
        val options = arrayOf("Leave Conversation")

        com.example.tripsync.utils.DialogUtils.showOptionsDialog(
            requireContext(),
            "Conversation Options",
            options
        ) { which ->
            when (which) {
                0 -> showLeaveConfirmation()
            }
        }
    }

    private fun showLeaveConfirmation() {
        com.example.tripsync.utils.DialogUtils.showConfirmationDialog(
            requireContext(),
            "Leave Conversation",
            "Are you sure you want to leave this conversation? You will no longer receive messages from this chat.",
            positiveButtonText = "Leave",
            negativeButtonText = "Cancel",
            onPositiveClick = {
                leaveConversation()
            }
        )
    }

    private fun leaveConversation() {
        lifecycleScope.launch {
            try {
                val chatApi = ApiClient.createService(requireContext(), ChatApi::class.java)

                Log.d("ChatThread", "Leaving conversation: $conversationId")

                val response = chatApi.leaveConversation(conversationId)

                Log.d("ChatThread", "Leave conversation response code: ${response.code()}")

                if (response.isSuccessful) {
                    Log.d("ChatThread", "Successfully left conversation")
                    Toast.makeText(requireContext(), "You have left the conversation", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ChatThread", "Failed to leave conversation: ${response.code()} - $errorBody")

                    val errorMessage = when (response.code()) {
                        403 -> "You are not a participant of this conversation"
                        404 -> "Conversation not found"
                        else -> "Failed to leave conversation: ${response.code()}"
                    }

                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ChatThread", "Error leaving conversation", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendMessage() {
        val content = messageInput.text.toString().trim()

        if (content.isEmpty()) {
            return
        }

        if (isSending) {
            return
        }

        Log.d("ChatThread", "Sending message: $content")

        isSending = true

        lifecycleScope.launch {
            try {
                val chatApi = ApiClient.createService(requireContext(), ChatApi::class.java)
                val request = SendMessageRequest(content)

                val response = chatApi.sendMessage(conversationId, request)

                Log.d("ChatThread", "Send response code: ${response.code()}")

                if (response.isSuccessful) {
                    val message = response.body()
                    Log.d("ChatThread", "Message sent successfully: ${message?.id}")

                    // Clear input but keep keyboard open
                    messageInput.text.clear()

                    // Keep focus to maintain keyboard
                    messageInput.requestFocus()

                    delay(100)
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
            }
        }
    }

    private fun editMessage(message: Message, newContent: String) {
        lifecycleScope.launch {
            try {
                val chatApi = ApiClient.createService(requireContext(), ChatApi::class.java)
                val request = EditMessageRequest(newContent)

                Log.d("ChatThread", "Editing message ${message.id}: $newContent")

                val response = chatApi.editMessage(conversationId, message.id, request)

                if (response.isSuccessful) {
                    Log.d("ChatThread", "Message edited successfully")
                    Toast.makeText(requireContext(), "Message edited", Toast.LENGTH_SHORT).show()

                    delay(100)
                    loadMessages(scrollToBottom = false, showToast = false)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ChatThread", "Failed to edit message: ${response.code()} - $errorBody")
                    Toast.makeText(requireContext(), "Failed to edit message", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ChatThread", "Error editing message", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteMessage(message: Message) {
        lifecycleScope.launch {
            try {
                val chatApi = ApiClient.createService(requireContext(), ChatApi::class.java)

                Log.d("ChatThread", "Deleting message ${message.id}")

                val response = chatApi.deleteMessage(conversationId, message.id)

                if (response.isSuccessful) {
                    Log.d("ChatThread", "Message deleted successfully")
                    Toast.makeText(requireContext(), "Message deleted", Toast.LENGTH_SHORT).show()

                    delay(100)
                    loadMessages(scrollToBottom = false, showToast = false)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ChatThread", "Failed to delete message: ${response.code()} - $errorBody")
                    Toast.makeText(requireContext(), "Failed to delete message", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ChatThread", "Error deleting message", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
                        messagesRecycler.visibility = View.VISIBLE
                        adapter.updateMessages(emptyList())
                    } else {
                        Log.d("ChatThread", "Updating adapter with messages")
                        messagesRecycler.visibility = View.VISIBLE
                        adapter.updateMessages(messages)

                        if (scrollToBottom || hasNewMessages) {
                            messagesRecycler.post {
                                messagesRecycler.smoothScrollToPosition(adapter.itemCount - 1)
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
        stopAutoRefresh()

        autoRefreshJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                delay(2000)
                if (!isSending) {
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