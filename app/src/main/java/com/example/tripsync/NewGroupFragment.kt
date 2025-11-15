package com.example.tripsync

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.adapters.SelectableUsersAdapter
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.ChatApi
import com.example.tripsync.api.UserApi
import com.example.tripsync.api.UserSearchRequest
import com.example.tripsync.api.models.CreateConversationRequest
import com.example.tripsync.api.models.UserSearchResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NewGroupFragment : Fragment() {

    private lateinit var groupNameEditText: EditText
    private lateinit var searchEditText: EditText
    private lateinit var participantsRecyclerView: RecyclerView
    private lateinit var createButton: Button
    private lateinit var adapter: SelectableUsersAdapter

    private val selectedUsers = mutableSetOf<UserSearchResponse>()
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.group_layout, container, false)

        // Initialize views
        val backArrow = view.findViewById<ImageView>(R.id.backArrow)
        groupNameEditText = view.findViewById(R.id.groupNameEditText)
        searchEditText = view.findViewById(R.id.searchEditText)
        participantsRecyclerView = view.findViewById(R.id.participantsRecyclerView)
        createButton = view.findViewById(R.id.createButton)

        backArrow.setOnClickListener {
            findNavController().navigateUp()
        }

        createButton.setOnClickListener {
            createGroup()
        }

        groupNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkCreateButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        participantsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = SelectableUsersAdapter(emptyList(), selectedUsers) { user, isSelected ->
            Log.d("NewGroup", "User ${user.fname} ${user.lname} selected: $isSelected")
            if (isSelected) {
                selectedUsers.add(user)
            } else {
                selectedUsers.remove(user)
            }
            Log.d("NewGroup", "Total selected: ${selectedUsers.size}")
            checkCreateButtonState()
        }
        participantsRecyclerView.adapter = adapter

        setupSearchListener()
        checkCreateButtonState()

        return view
    }

    private fun setupSearchListener() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(500) // Reduced from 2500ms
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
                Log.d("NewGroup", "Searching for: $query")

                val userApi = ApiClient.createService(requireContext(), UserApi::class.java)

                val parts = query.trim().split("\\s+".toRegex(), limit = 2)
                val firstName = parts.getOrNull(0) ?: ""
                val lastName = if (parts.size > 1) parts[1] else ""

                val request = UserSearchRequest(fname = firstName, lname = lastName)
                val response = userApi.searchUsers(request)

                if (response.isSuccessful) {
                    val users = response.body()?.data?.users ?: emptyList()
                    Log.d("NewGroup", "Found ${users.size} users")
                    adapter.updateUsers(users)
                } else {
                    Log.e("NewGroup", "Search failed: ${response.code()}")
                    Toast.makeText(requireContext(), "Please enter full name", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("NewGroup", "Error searching: ${e.message}", e)
                Toast.makeText(requireContext(), "Search failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkCreateButtonState() {
        val groupName = groupNameEditText.text.toString().trim()
        val hasValidName = groupName.isNotEmpty()
        val hasEnoughParticipants = selectedUsers.size >= 2

        createButton.isEnabled = hasValidName && hasEnoughParticipants

        Log.d("NewGroup", "Button state: name=$hasValidName, participants=${selectedUsers.size}, enabled=${createButton.isEnabled}")
    }

    private fun createGroup() {
        val groupName = groupNameEditText.text.toString().trim()

        if (groupName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter group name", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedUsers.size < 2) {
            Toast.makeText(requireContext(), "Select at least 2 participants", Toast.LENGTH_SHORT).show()
            return
        }

        createButton.isEnabled = false

        lifecycleScope.launch {
            try {
                Log.d("NewGroup", "Creating group: $groupName with ${selectedUsers.size} participants")

                val chatApi = ApiClient.createService(requireContext(), ChatApi::class.java)

                val participantIds = selectedUsers.map { it.id }
                Log.d("NewGroup", "Participant IDs: $participantIds")

                val request = CreateConversationRequest(
                    participant_ids = participantIds,
                    name = groupName
                )

                val response = chatApi.createConversation(request)

                Log.d("NewGroup", "Create response code: ${response.code()}")

                if (response.isSuccessful) {
                    val conversation = response.body()
                    if (conversation != null) {
                        Log.d("NewGroup", "Group created successfully! ID: ${conversation.id}")
                        Toast.makeText(requireContext(), "Group created!", Toast.LENGTH_SHORT).show()

                        val bundle = Bundle().apply {
                            putInt("conversationId", conversation.id)
                            putString("name", groupName)
                            putBoolean("isGroup", true)
                        }

                        findNavController().navigate(
                            R.id.action_newGroupFragment_to_chatThreadFragment,
                            bundle
                        )
                    } else {
                        Log.e("NewGroup", "Response body is null")
                        Toast.makeText(requireContext(), "Failed to create group", Toast.LENGTH_SHORT).show()
                        createButton.isEnabled = true
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("NewGroup", "Failed: ${response.code()} - $errorBody")
                    Toast.makeText(requireContext(), "Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    createButton.isEnabled = true
                }
            } catch (e: Exception) {
                Log.e("NewGroup", "Error creating group", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                createButton.isEnabled = true
            }
        }
    }
}