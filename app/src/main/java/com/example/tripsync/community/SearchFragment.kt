package com.example.tripsync.community

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.models.Post
import com.example.tripsync.api.models.PostActionListener
import com.example.tripsync.community.PostAdapter
import com.example.tripsync.ui.CommentsFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.widget.addTextChangedListener
import com.example.tripsync.api.models.LikeRequest
import com.example.tripsync.api.models.SearchPostResponseItem // Import the data class if Post is not the exact type
import com.example.tripsync.api.models.UserP

class SearchFragment : Fragment(), PostActionListener { // Implement PostActionListener here

    private lateinit var searchEditText: EditText
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var noDataLayout: ImageView
    private lateinit var initialSearchStateLayout: ImageView
    private lateinit var backButton: ImageView

    // Initialized in setupRecyclerView
    private lateinit var searchAdapter: PostAdapter

    private var searchJob: Job? = null

    // Constants or Companion Object for Logging
    companion object {
        private const val TAG = "SearchFragment"
        private const val SEARCH_DELAY_MS = 500L
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchEditText = view.findViewById(R.id.edit_text_search)
        searchResultsRecyclerView = view.findViewById(R.id.search_results_recyclerview)
        noDataLayout = view.findViewById(R.id.img_no_data)
        initialSearchStateLayout = view.findViewById(R.id.initial)
        backButton = view.findViewById(R.id.btn_back)

        setupRecyclerView()
        setupListeners()

        showInitialSearchState()
    }

    private fun setupRecyclerView() {
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val sharedPref = requireContext().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val currentUserEmail = sharedPref.getString("userEmail", null)

        // 🌟 FIX: Initialize the adapter here
        // The adapter must be initialized with an empty list and the listener (this fragment)
        searchAdapter = PostAdapter(emptyList<Post>(), this, currentUserEmail)
        searchResultsRecyclerView.adapter = searchAdapter
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // Search action on keyboard ENTER/Search button
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Cancel any pending debounced job and perform the search immediately
                searchJob?.cancel()
                performSearch(searchEditText.text.toString())
                true
            } else {
                false
            }
        }

        // Debounced search on text change
        searchEditText.addTextChangedListener { editable ->
            val query = editable.toString().trim()
            searchJob?.cancel() // Cancel the previous debounced search

            if (query.isNotEmpty() && query.length >= 2) { // Optional: require minimum 2 characters
                searchJob = lifecycleScope.launch {
                    delay(SEARCH_DELAY_MS) // Wait for 500ms
                    performSearch(query)
                }
            } else {
                showInitialSearchState() // Show initial state if query is empty
            }
        }
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) {
            showInitialSearchState()
            return
        }

        lifecycleScope.launch {
            try {
                val api = ApiClient.getTokenService(requireContext())
                val response = api.searchPosts(query)
                Log.d(TAG, "Search URL: ${response.raw().request.url}")

                if (response.isSuccessful) {
                    val rawResults = response.body()?.data ?: emptyList()
                    val results = rawResults.map { it ->
                        Post(
                            id = it.id,
                            title = it.title,
                            desc = it.desc,
                            img = it.img ?: "",
                            img_url = it.img_url ?: "",
                            vid = it.vid ?: "",
                            vid_url = it.vid_url ?: "",
                            likes = it.likes,
                            dislikes = it.dislikes,
                            rating = it.rating,
                            loc = it.loc ?: "",
                            created = it.created ?: "",
                            updated = it.updated ?: "",
                            total_comments = it.total_comments,
                            reaction = it.reaction ?: "",
                            user = null as UserP?,
                            owner = it.owner
                        )
                    }




                    if (results.isNotEmpty()) {
                        searchAdapter.updateData(results)
                        showResults()
                    } else {
                        searchAdapter.updateData(emptyList())
                        showNoDataState()
                    }
                } else {
                    Log.e(TAG, "Search API Error: ${response.code()} - ${response.message()}")
                    searchAdapter.updateData(emptyList())
                    showNoDataState()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Network Exception during search: ${e.message}", e)
                searchAdapter.updateData(emptyList())
                showNoDataState()
            }

        }
    }

        // --- PostActionListener Implementation for Search Results ---

        override fun onDelete(postId: Int) {
            // Implement post deletion logic for posts found via search, if required.
            Log.d(TAG, "Delete action triggered for post ID: $postId (from search)")
            // Typically, you'd navigate back to the main CommunityFragment to handle the delete and refresh the feed.
        }

        override fun onLike(postId: Int) {
            // Implement the like API call (similar to CommunityFragment)
            // Since PostAdapter handles the local UI change, you just need to sync the API.
            lifecycleScope.launch {
                try {
                    val api = ApiClient.getTokenService(requireContext())
                    val response = api.likePost(
                        postId,
                        LikeRequest(like = true)
                    ) // Use actual LikeRequest model
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Like sync failed for post $postId: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Network error liking post $postId: ${e.message}")
                }
            }
        }


    override fun onComment(postId: Int) {
        // Show the comments fragment (CommentsFragment)
        val commentsFragment = CommentsFragment.newInstance(postId)
        commentsFragment.show(parentFragmentManager, CommentsFragment.TAG)
    }

    // --- State Management Functions ---

    private fun showResults() {
        initialSearchStateLayout.visibility = View.GONE
        noDataLayout.visibility = View.GONE
        searchResultsRecyclerView.visibility = View.VISIBLE
    }

    private fun showNoDataState() {
        initialSearchStateLayout.visibility = View.GONE
        noDataLayout.visibility = View.VISIBLE
        searchResultsRecyclerView.visibility = View.GONE
    }

    private fun showInitialSearchState() {
        initialSearchStateLayout.visibility = View.VISIBLE
        noDataLayout.visibility = View.GONE
        searchResultsRecyclerView.visibility = View.GONE
    }
}