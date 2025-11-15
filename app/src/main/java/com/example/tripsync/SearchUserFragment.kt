package com.example.tripsync

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.adapters.UsersAdapter
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.ChatApi
import com.example.tripsync.api.UserApi
import com.example.tripsync.api.UserSearchRequest
import com.example.tripsync.api.models.Conversation
import com.example.tripsync.api.models.CreateConversationRequest
import com.example.tripsync.api.models.UserSearchResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchUsersFragment : Fragment() {

    private lateinit var searchEditText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UsersAdapter
    private lateinit var progressBar: ProgressBar
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_search_users, container, false)

        val backButton = view.findViewById<ImageView>(R.id.toolbar_back)
        searchEditText = view.findViewById(R.id.search_edit_text)
        recyclerView = view.findViewById(R.id.recycler_users)
        progressBar = view.findViewById(R.id.progress_bar)

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = UsersAdapter(emptyList()) { userResponse ->
            createConversationWithUser(userResponse)
        }

        recyclerView.adapter = adapter
        setupSearchListener()

        return view
    }

    private fun setupSearchListener() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(3000)
                    val query = s.toString().trim()
                    if (query.isNotEmpty()) {
                        searchUsers(query)
                    } else {
                        adapter.updateUsers(emptyList())
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun searchUsers(query: String) {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                val userApi = ApiClient.createService(requireContext(), UserApi::class.java)

                val parts = query.trim().split("\\s+".toRegex(), limit = 2)
                val firstName = parts.getOrNull(0) ?: ""
                val lastName = if (parts.size > 1) parts[1] else ""

                val request = UserSearchRequest(fname = firstName, lname = lastName)
                val response = userApi.searchUsers(request)

                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val users = response.body()?.data?.users ?: emptyList()
                    adapter.updateUsers(users)
                    if (users.isEmpty()) {
                        Toast.makeText(requireContext(), "No users found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Search failed (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Log.e("SearchUsers", "Error searching users: ${e.message}", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createConversationWithUser(user: UserSearchResponse) {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                val chatApi = ApiClient.createService(requireContext(), ChatApi::class.java)

                Log.d("CreateConversation", "Checking/creating conversation with user ID: ${user.id}")

                val existingConversations = try {
                    val listResponse = chatApi.getConversations()
                    if (listResponse.isSuccessful) {
                        listResponse.body() ?: emptyList()
                    } else {
                        emptyList()
                    }
                } catch (e: Exception) {
                    Log.e("CreateConversation", "Error fetching conversations: ${e.message}")
                    emptyList()
                }

                // Check if conversation with this user already exists
                val existingConversation = existingConversations.find { conversation ->
                    conversation.participants.any { participant -> participant.id == user.id }
                }

                val conversationId: Int
                val isNewConversation: Boolean

                if (existingConversation != null) {
                    // Conversation already exists
                    conversationId = existingConversation.id
                    isNewConversation = false
                    Log.d("CreateConversation", "Found existing conversation: ID=$conversationId")
                } else {
                    val request = CreateConversationRequest(
                        participant_ids = listOf(user.id),
                        name = null
                    )

                    val response = chatApi.createConversation(request)
                    Log.d("CreateConversation", "Create response code: ${response.code()}")

                    if (response.isSuccessful) {
                        val conversation = response.body()

                        if (conversation != null) {
                            conversationId = conversation.id
                            isNewConversation = true
                            Log.d("CreateConversation", "New conversation created: ID=$conversationId")
                        } else {
                            progressBar.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                "Failed to create conversation",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@launch
                        }
                    } else if (response.code() == 409) {
                        try {
                            val errorBody = response.errorBody()?.string()
                            Log.d("CreateConversation", "409 error body: $errorBody")

                            val gson = com.google.gson.Gson()
                            val existingConv = gson.fromJson(errorBody, Conversation::class.java)

                            if (existingConv != null) {
                                conversationId = existingConv.id
                                isNewConversation = false
                                Log.d("CreateConversation", "Parsed existing conversation from 409: ID=$conversationId")
                            } else {
                                progressBar.visibility = View.GONE
                                Toast.makeText(requireContext(), "Conversation already exists", Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                        } catch (parseException: Exception) {
                            progressBar.visibility = View.GONE
                            Log.e("CreateConversation", "Failed to parse 409 response: ${parseException.message}")
                            Toast.makeText(requireContext(), "Conversation already exists", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                    } else {
                        progressBar.visibility = View.GONE
                        val errorBody = response.errorBody()?.string()
                        Log.e("CreateConversation", "Failed: ${response.code()} - $errorBody")

                        when (response.code()) {
                            400 -> Toast.makeText(requireContext(), "Invalid request", Toast.LENGTH_SHORT).show()
                            401 -> Toast.makeText(requireContext(), "Unauthorized - Please login again", Toast.LENGTH_SHORT).show()
                            403 -> Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
                            404 -> Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                            500 -> Toast.makeText(requireContext(), "Server error", Toast.LENGTH_SHORT).show()
                            else -> Toast.makeText(requireContext(), "Error ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }
                }

                progressBar.visibility = View.GONE

                // Navigate to conversation
                val displayName = when {
                    !user.fname.isNullOrEmpty() && !user.lname.isNullOrEmpty() ->
                        "${user.fname} ${user.lname}"
                    !user.fname.isNullOrEmpty() -> user.fname
                    !user.lname.isNullOrEmpty() -> user.lname
                    !user.email.isNullOrEmpty() -> user.email
                    else -> "Unknown User"
                }

                try {
                    val cachedInfo = com.example.tripsync.CachedUserInfo(
                        userId = user.id,
                        name = displayName,
                        email = user.email,
                        fname = user.fname,
                        lname = user.lname,
                        profilePicUrl = null // Add user.profilePicUrl if available in your API model
                    )
                    com.example.tripsync.ConversationInfoCache.saveUserInfo(
                        requireContext(),
                        conversationId,
                        cachedInfo
                    )
                    Log.d("CreateConversation", "Cached user info for conversation $conversationId")
                } catch (cacheException: Exception) {
                    // Don't fail the navigation if caching fails
                    Log.e("CreateConversation", "Failed to cache user info: ${cacheException.message}")
                }
                // ========== END NEW CODE ==========

                val bundle = Bundle().apply {
                    putString("name", displayName)
                    putInt("conversationId", conversationId)
                    putBoolean("isGroup", false)
                }

                val message = if (isNewConversation) "Conversation created" else "Opening conversation"
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                findNavController().navigate(
                    R.id.action_searchUsersFragment_to_chatThreadFragment,
                    bundle
                )

            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Log.e("CreateConversation", "Exception: ${e.message}", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}