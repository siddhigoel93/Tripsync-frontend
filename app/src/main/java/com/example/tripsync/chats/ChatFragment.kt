package com.example.tripsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.tripsync.adapters.ConversationsAdapter
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.ChatApi
import com.example.tripsync.api.models.Conversation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ConversationsAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyState: LinearLayout
    private var autoRefreshJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_chats, container, false)

        recycler = root.findViewById(R.id.recycler_trips)
        swipeRefresh = root.findViewById(R.id.swipe_refresh)
        emptyState = root.findViewById(R.id.empty_state)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        swipeRefresh.setOnRefreshListener {
            loadConversations()
        }

        val addButton = root.findViewById<ImageButton>(R.id.bottom_btn_add)
        addButton.setOnClickListener {
            showNewChatOptions()
        }

        adapter = ConversationsAdapter(mutableListOf()) { conversation ->
            openConversation(conversation)
        }

        recycler.adapter = adapter

        // Initial load
        loadConversations()

        return root
    }

    override fun onResume() {
        super.onResume()
        // Reload conversations when returning to this fragment
        loadConversations()
        // Start auto-refresh
        startAutoRefresh()
    }

    override fun onPause() {
        super.onPause()
        // Stop auto-refresh when not visible
        stopAutoRefresh()
    }

    private fun showNewChatOptions() {
        val options = arrayOf("New Chat", "New Group")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Start a conversation")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> findNavController().navigate(R.id.action_chatFragment_to_searchUsersFragment)
                    1 -> findNavController().navigate(R.id.action_chatFragment_to_newGroupFragment)
                }
            }
            .show()
    }

    private fun loadConversations() {
        lifecycleScope.launch {
            try {
                val chatApi = ApiClient.createService(requireContext(), ChatApi::class.java)
                val response = chatApi.getConversations()

                swipeRefresh.isRefreshing = false

                if (response.isSuccessful) {
                    val conversationsList = response.body() ?: emptyList()

                    if (conversationsList.isEmpty()) {
                        emptyState.visibility = View.VISIBLE
                        recycler.visibility = View.GONE
                    } else {
                        emptyState.visibility = View.GONE
                        recycler.visibility = View.VISIBLE
                        adapter.updateList(conversationsList)
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to load chats",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                swipeRefresh.isRefreshing = false
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openConversation(conversation: Conversation) {
        val bundle = Bundle().apply {
            putInt("conversationId", conversation.id)
            putString("name", getConversationName(conversation))
            putBoolean("isGroup", conversation.is_group ?: false)
        }
        findNavController().navigate(
            R.id.action_chatFragment_to_chatThreadFragment,
            bundle
        )
    }

    private fun getConversationName(conversation: Conversation): String {
        if (conversation.is_group == true) {
            return conversation.name ?: "Unnamed Group"
        }

        // For 1-on-1 chats, get the other participant's name
        val sharedPref = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getString("user_id", "-1")?.toIntOrNull() ?: -1
        val otherParticipant = conversation.participants.firstOrNull { it.id != currentUserId }

        return otherParticipant?.name ?: otherParticipant?.email ?: "Unknown"
    }

    private fun startAutoRefresh() {
        stopAutoRefresh() // Stop any existing refresh

        autoRefreshJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                delay(3000) // Refresh every 3 seconds
                if (!swipeRefresh.isRefreshing) {
                    loadConversations()
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