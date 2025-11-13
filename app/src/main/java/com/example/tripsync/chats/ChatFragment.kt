package com.example.tripsync

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
//import com.example.tripsync.adapters.ConversationsAdapter
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.ChatApi
import com.example.tripsync.api.models.Conversation
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ConversationsAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private val conversations = mutableListOf<Conversation>()

    // Auto-refresh handler
    private val refreshHandler = Handler(Looper.getMainLooper())
    private var isRefreshing = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_chats, container, false)

        recycler = root.findViewById(R.id.recycler_trips)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        // Add SwipeRefreshLayout to your layout if not present
        swipeRefresh = root.findViewById(R.id.swipe_refresh) // Add this to XML
        swipeRefresh.setOnRefreshListener {
            loadConversations()
        }

        val add = root.findViewById<ImageButton>(R.id.bottom_btn_add)

        add.setOnClickListener {
            findNavController().navigate(R.id.action_chatFragment_to_searchUsersFragment)
        }

        adapter = ConversationsAdapter(conversations) { conversation ->
            // Navigate to chat thread
            val bundle = Bundle().apply {
                putString("name", conversation.name)
                putInt("conversationId", conversation.id)
            }
            findNavController().navigate(R.id.action_chatFragment_to_chatThreadFragment, bundle)
        }

        recycler.adapter = adapter

        loadConversations()
        startAutoRefresh()

        return root
    }

    private fun loadConversations() {
        lifecycleScope.launch {
            try {
                val chatApi = ApiClient.createService(requireContext(), ChatApi::class.java)
                val response = chatApi.getConversations()

                swipeRefresh.isRefreshing = false

                if (response.isSuccessful) {
                    val conversationsList = response.body() ?: emptyList()
                    adapter.updateList(conversationsList)
                } else {
                    Toast.makeText(requireContext(), "Failed to load conversations", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                swipeRefresh.isRefreshing = false
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Auto-refresh conversations every 5 seconds
    private fun startAutoRefresh() {
        isRefreshing = true
        refreshHandler.postDelayed(object : Runnable {
            override fun run() {
                if (isRefreshing) {
                    loadConversations()
                    refreshHandler.postDelayed(this, 5000) // Every 5 seconds
                }
            }
        }, 5000)
    }

    private fun stopAutoRefresh() {
        isRefreshing = false
        refreshHandler.removeCallbacksAndMessages(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAutoRefresh()
    }
}