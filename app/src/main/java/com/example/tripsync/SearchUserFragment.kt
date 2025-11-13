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
                    delay(500) // Debounce
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

                // Split query into first and last name
                val parts = query.trim().split(" ", limit = 2)
                val firstName = parts.getOrNull(0) ?: ""
                val lastName = parts.getOrNull(1) ?: ""

                Log.d("SearchUsers", "Searching - First: '$firstName', Last: '$lastName'")

                // Create request body
                val request = UserSearchRequest(
                    fname = firstName.ifEmpty { null },
                    lname = lastName.ifEmpty { null }
                )

                val response = userApi.searchUsers(request)

                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val users = response.body() ?: emptyList()

                    Log.d("SearchUsers", "Found ${users.size} users")

                    adapter.updateUsers(users)

                    if (users.isEmpty()) {
                        Toast.makeText(requireContext(), "No users found. Try 'FirstName LastName'", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("SearchUsers", "Search failed: ${response.code()} - $errorBody")

                    when (response.code()) {
                        400 -> Toast.makeText(requireContext(), "Invalid search query", Toast.LENGTH_SHORT).show()
                        403 -> Toast.makeText(requireContext(), "Phone verification required", Toast.LENGTH_SHORT).show()
                        404 -> Toast.makeText(requireContext(), "No users found", Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(requireContext(), "Search failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
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

                Log.d("CreateConversation", "Creating conversation with user ID: ${user.id}")

                val request = CreateConversationRequest(
                    participant_ids = listOf(user.id),
                    name = null // Backend auto-generates for DMs
                )

                val response = chatApi.createConversation(request)

                Log.d("CreateConversation", "Response code: ${response.code()}")

                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val responseBody = response.body()

                    Log.d("CreateConversation", "Response: $responseBody")

                    // FIXED: Extract conversation from nested 'data' field
                    val conversation = responseBody?.data

                    if (conversation != null) {
                        Log.d("CreateConversation", "Conversation created: ID=${conversation.id}")

                        // Build display name
                        val displayName = when {
                            !user.name.isNullOrEmpty() -> user.name
                            !user.first_name.isNullOrEmpty() && !user.last_name.isNullOrEmpty() ->
                                "${user.first_name} ${user.last_name}"
                            !user.first_name.isNullOrEmpty() -> user.first_name
                            !user.last_name.isNullOrEmpty() -> user.last_name
                            !user.email.isNullOrEmpty() -> user.email
                            else -> "Unknown User"
                        }

                        val bundle = Bundle().apply {
                            putString("name", displayName)
                            putInt("conversationId", conversation.id)
                        }

                        Toast.makeText(requireContext(), "Conversation created!", Toast.LENGTH_SHORT).show()

                        // Navigate to chat thread
                        findNavController().navigate(
                            R.id.action_searchUsersFragment_to_chatThreadFragment,
                            bundle
                        )
                    } else {
                        Log.e("CreateConversation", "Response status: ${responseBody?.status}")
                        Log.e("CreateConversation", "Response message: ${responseBody?.message}")
                        Toast.makeText(requireContext(), responseBody?.message ?: "Failed to create conversation", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("CreateConversation", "Failed: ${response.code()}")
                    Log.e("CreateConversation", "Error: $errorBody")

                    when (response.code()) {
                        400 -> Toast.makeText(requireContext(), "Invalid request - Check participant ID", Toast.LENGTH_LONG).show()
                        401 -> Toast.makeText(requireContext(), "Unauthorized - Please login again", Toast.LENGTH_SHORT).show()
                        403 -> Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
                        404 -> Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                        409 -> Toast.makeText(requireContext(), "Conversation already exists", Toast.LENGTH_SHORT).show()
                        500 -> Toast.makeText(requireContext(), "Server error - Try again later", Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(requireContext(), "Error ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Log.e("CreateConversation", "Exception: ${e.message}", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}