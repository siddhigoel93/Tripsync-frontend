package com.example.tripsync

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.models.friend_MyTripmateResponseItem
import com.example.tripsync.api.models.friend_TripmateSearchResult
import kotlinx.coroutines.launch

class ConnectionsTripmatesFragment : Fragment() {

    private lateinit var rvConnections: RecyclerView
    private lateinit var rvTripmates: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var ivSearchIcon: ImageView
    private lateinit var connectionAdapter: ConnectionAdapter
    private lateinit var myTripmateAdapter: friend_MyTripmateAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_connections_tripmates, container, false)

        rvConnections = root.findViewById(R.id.rv_connections)
        rvTripmates = root.findViewById(R.id.rv_tripmates)
        etSearch = root.findViewById(R.id.et_search)
        ivSearchIcon = root.findViewById(R.id.iv_search_icon)

        val backBtn = root.findViewById<ImageButton>(R.id.btn_back)
        backBtn?.setOnClickListener {
            try { findNavController().popBackStack() }
            catch (_: Exception) {}
        }

        val bellBtn = root.findViewById<ImageButton>(R.id.btn_bell)
        bellBtn?.setOnClickListener {
            findNavController().navigate(R.id.action_fragment_connections_tripmates_to_recievedRequestsFragment)
        }

        setupRecyclerViews()
        setupSearchHandlers()
        loadMyTripmates()

        return root
    }

    private fun setupRecyclerViews() {
        rvConnections.layoutManager = LinearLayoutManager(requireContext())
        connectionAdapter = ConnectionAdapter(mutableListOf<TripmateConnect>())
        rvConnections.adapter = connectionAdapter
        rvConnections.isNestedScrollingEnabled = false

        rvTripmates.layoutManager = LinearLayoutManager(requireContext())
        myTripmateAdapter = friend_MyTripmateAdapter(mutableListOf())
        rvTripmates.adapter = myTripmateAdapter
        rvTripmates.isNestedScrollingEnabled = false
    }

    private fun setupSearchHandlers() {
        etSearch.setOnEditorActionListener { _, actionId, event ->
            val enterKey = event?.keyCode == KeyEvent.KEYCODE_ENTER
            if (actionId == EditorInfo.IME_ACTION_SEARCH || enterKey) {
                performSearch(etSearch.text.toString().trim())
                true
            } else false
        }

        ivSearchIcon.setOnClickListener {
            performSearch(etSearch.text.toString().trim())
        }
    }

    private fun performSearch(query: String) {
        if (query.length < 2) {
            Toast.makeText(requireContext(), "Enter at least 2 characters", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val resp = ApiClient.getTripmateService(requireContext()).searchUsers(query)
                if (resp.isSuccessful) {
                    val results = resp.body() ?: emptyList<friend_TripmateSearchResult>()
                    val mapped = results.map { r ->
                        TripmateConnect(
                            id = r.id.toString(),
                            name = r.profileData?.fullName ?: r.email ?: "Unknown",
                            meta = if (r.isTripmate == true) "Tripmate" else (r.profileData?.preference ?: ""),
                            avatarUrl = r.profileData?.profilePic
                        )
                    }
                    connectionAdapter.updateItems(mapped)
                } else {
                    Toast.makeText(requireContext(), "Search failed: ${resp.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadMyTripmates() {
        lifecycleScope.launch {
            try {
                val resp = ApiClient.getTripmateService(requireContext()).getMyTripmates()
                if (resp.isSuccessful) {
                    val list = resp.body() ?: emptyList<friend_MyTripmateResponseItem>()
                    myTripmateAdapter.update(list)
                } else {
                    Toast.makeText(requireContext(), "Failed to load tripmates: ${resp.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
