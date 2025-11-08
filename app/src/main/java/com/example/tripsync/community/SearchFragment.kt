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
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.models.LikeRequest
import com.example.tripsync.api.models.Post
import com.example.tripsync.api.models.PostActionListener
import com.example.tripsync.api.models.SearchPostResponseItem
import com.example.tripsync.api.models.UserP
import com.example.tripsync.ui.CommentsFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment(), PostActionListener {

    private lateinit var searchEditText: EditText
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var noDataLayout: ImageView
    private lateinit var initialSearchStateLayout: ImageView
    private lateinit var backButton: ImageView

    private lateinit var searchAdapter: PostAdapter
    private var searchJob: Job? = null

    companion object {
        private const val TAG = "SearchFragment"
        private const val SEARCH_DELAY_MS = 500L
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_search, container, false)

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

        val currentUserEmail = requireContext().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
            .getString("userEmail", null)

        searchAdapter = PostAdapter(emptyList(), this, currentUserEmail)
        searchResultsRecyclerView.adapter = searchAdapter
    }

    private fun setupListeners() {
        backButton.setOnClickListener { findNavController().popBackStack() }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchJob?.cancel()
                performSearch(searchEditText.text.toString())
                true
            } else false
        }

        searchEditText.addTextChangedListener { editable ->
            val query = editable.toString().trim()
            searchJob?.cancel()
            if (query.length >= 2) {
                searchJob = lifecycleScope.launch {
                    delay(SEARCH_DELAY_MS)
                    performSearch(query)
                }
            } else {
                showInitialSearchState()
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

                if (response.isSuccessful) {
                    val rawResults: List<SearchPostResponseItem> = response.body()?.data ?: emptyList()
                    val results = rawResults.map { mapToPost(it) }

                    if (results.isNotEmpty()) {
                        searchAdapter.updateData(results)
                        showResults()
                    } else {
                        searchAdapter.updateData(emptyList())
                        showNoDataState()
                    }
                } else {
                    searchAdapter.updateData(emptyList())
                    showNoDataState()
                    Log.e(TAG, "Search API failed: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                searchAdapter.updateData(emptyList())
                showNoDataState()
                Log.e(TAG, "Search Exception: ${e.message}", e)
            }
        }
    }

    // Map API response item to Post safely
    private fun mapToPost(item: SearchPostResponseItem): Post {
        return Post(
            id = item.id,
            title = item.title ?: "",
            desc = item.desc ?: "",
            img = item.img ?: "",
            img_url = item.img_url ?: "",
            vid = item.vid ?: "",
            vid_url = item.vid_url ?: "",
            likes = item.likes ,
            dislikes = item.dislikes ,
            rating = item.rating ,
            loc = item.loc ?: "",
            created = item.created ?: "",
            updated = item.updated ?: "",
            total_comments = item.total_comments ?: 0,
            reaction = item.reaction ?: "",
            user = null as UserP?,
            owner = item.owner ?: false
        )
    }

    // --- PostActionListener Implementation ---
    override fun onDelete(postId: Int) { Log.d(TAG, "Delete $postId") }
    override fun onLike(postId: Int) {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getTokenService(requireContext())
                api.likePost(postId, LikeRequest(like = true))
            } catch (e: Exception) {
                Log.e(TAG, "Like API failed: ${e.message}")
            }
        }
    }
    override fun onComment(postId: Int) {
        CommentsFragment.newInstance(postId).show(parentFragmentManager, CommentsFragment.TAG)
    }

    // --- UI State Helpers ---
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
