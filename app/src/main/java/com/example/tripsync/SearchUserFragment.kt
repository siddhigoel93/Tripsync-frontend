package com.example.tripsync

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
                val response = userApi.searchUsers(query)

                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val users = response.body()?.map {
                        UserSearchResponse(
                            id = it.id,
                            email = it.email,
                            name = it.name,
                            first_name = it.first_name,
                            last_name = it.last_name,
                            profile_picture = it.profile_picture
                        )
                    } ?: emptyList()

                    adapter.updateUsers(users)
                } else {
                    Toast.makeText(requireContext(), "Search failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createConversationWithUser(user: UserSearchResponse)
        {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                val chatApi = ApiClient.createService(requireContext(), ChatApi::class.java)

                val request = CreateConversationRequest(
                    participant_ids = listOf(user.id),
                    name = null
                )

                val response = chatApi.createConversation(request)
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val conversation = response.body()
                    if (conversation != null) {
                        val displayName = user.name
                            ?: listOfNotNull(user.first_name, user.last_name).joinToString(" ").ifEmpty { user.email }

                        val bundle = Bundle().apply {
                            putString("name", displayName)
                            putInt("conversationId", conversation.id)
                        }
                        findNavController().navigate(
                            R.id.action_searchUsersFragment_to_chatThreadFragment,
                            bundle
                        )
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to create conversation", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}