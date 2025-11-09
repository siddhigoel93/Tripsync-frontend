package com.example.tripsync

import android.os.Bundle
import android.util.Log
import android.util.Log.e
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.adapters.SearchAdapter
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.ChatApi
import com.example.tripsync.api.models.CreateConversationRequest
import com.example.tripsync.api.models.UserS
import kotlinx.coroutines.launch

class SearchUsersFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: SearchAdapter
    private lateinit var searchBar: EditText

    private var allUsers = listOf<UserS>()
    private var filteredUsers = mutableListOf<UserS>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_search_user, container, false)
        recycler = view.findViewById(R.id.user_recycler_view)
        searchBar = view.findViewById(R.id.search_bar)

        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = SearchAdapter(filteredUsers) { user ->
            createOrOpenConversation(user)
        }
        recycler.adapter = adapter

        loadUsers()
        setupSearch()

        return view
    }

    private fun loadUsers() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getTokenService(requireContext())
                val response = api.getAllUsers()
                if (response.isSuccessful) {
                    val usersFromApi = response.body()?.data?.users ?: emptyList()
                    allUsers = usersFromApi.map { user ->
                        UserS(id = user.id, fname = user.fname, lname = user.lname)
                    }
                    filteredUsers.clear()
                    filteredUsers.addAll(allUsers)
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireContext(), "Failed to load users", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSearch() {
        searchBar.addTextChangedListener {
            val query = it.toString().lowercase()
            filteredUsers.clear()
            filteredUsers.addAll(allUsers.filter { user ->
                user.fullName.lowercase().contains(query)
            })
            adapter.notifyDataSetChanged()
        }
    }

    private fun createOrOpenConversation(user: UserS) {
        lifecycleScope.launch {
            try {
                val chatApi = ApiClient.createService(requireContext(), ChatApi::class.java)

                val response = chatApi.createConversation(
                    CreateConversationRequest(listOf(user.id))
                )

                if (response.isSuccessful) {
                    val conversation = response.body()?.data
                    if (conversation != null) {
                        val action = SearchUsersFragmentDirections
                            .actionSearchUsersFragmentToChatThreadFragment(
                                conversationId = conversation.id,
                                name = conversation.name ?: user.fullName
                            )
                        findNavController().navigate(action)
                    } else {
                        Toast.makeText(requireContext(), "Conversation returned null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to create conversation", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


}
