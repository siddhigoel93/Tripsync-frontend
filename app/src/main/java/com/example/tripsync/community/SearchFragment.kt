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
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
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
import com.example.tripsync.ui.CommentsFragment.Companion.TAG
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

     lateinit var searchEditText: EditText
    lateinit var searchResultsRecyclerView: RecyclerView
     lateinit var noDataLayout: ImageView
     lateinit var initialSearchStateLayout: ImageView
     lateinit var backButton: ImageView
     lateinit var searchAdapter: PostAdapter

     var searchJob: Job? = null




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

//        searchAdapter = PostAdapter(emptyList<Post>(), postActionListener, currentUserEmail)
        searchResultsRecyclerView.adapter = searchAdapter
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchEditText.text.toString())
                true
            } else {
                false
            }
        }

        searchEditText.addTextChangedListener { editable ->
            val query = editable.toString().trim()
            searchJob?.cancel()

            if (query.isNotEmpty()) {
                searchJob = lifecycleScope.launch {
                    delay(500)
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
                // Hide keyboard when search is initiated (optional)
                // val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                // imm?.hideSoftInputFromWindow(searchEditText.windowToken, 0)

                val api = ApiClient.getTokenService(requireContext())

                val response = api.searchPosts(query)

                if (response.isSuccessful) {
                    val rawResults = response.body() ?: emptyList<Post>()
                    val results = rawResults as List<Post>

                    if (results.isNotEmpty()) {
                        searchAdapter.updateData(results)
                        showResults()
                    } else {
                        searchAdapter.updateData(emptyList())
                        showNoDataState()
                    }
                } else {
                    Log.e(TAG, "Search API Error: ${response.code()}")
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