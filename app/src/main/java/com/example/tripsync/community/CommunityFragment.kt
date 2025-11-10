package com.example.tripsync.community

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.community.PostAdapter
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.AuthService
import com.example.tripsync.api.models.LikeRequest
import com.example.tripsync.api.models.PostActionListener
import com.example.tripsync.ui.CommentsFragment
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.launch

class CommunityFragment : Fragment(), PostActionListener {

    private var recyclerView: RecyclerView? = null
    private var postAdapter: PostAdapter? = null
    private var toolbar: Toolbar? = null
    private var searchIcon: ImageView? = null
    private var createIcon: ImageView? = null

    private var apiService: AuthService? = null
    private var api: AuthService? = null


    companion object {
        const val POST_CREATION_REQUEST_KEY = "post_creation_request"
        const val REFRESH_FEED_KEY = "refresh_feed_key"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_community, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiService = ApiClient.getAuthService(requireContext())
        api = ApiClient.getTokenService(requireContext())

        recyclerView = view.findViewById(R.id.community_posts_recycler_view)

        val appBarLayout: AppBarLayout = view.findViewById(R.id.app_bar)
        toolbar = appBarLayout.findViewById(R.id.toolbar)
        searchIcon = toolbar?.findViewById(R.id.search)
        createIcon = toolbar?.findViewById(R.id.create)


        val sharedPref = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val currentUserEmail = sharedPref.getString("userEmail", null)
        val currentUserName = sharedPref.getString("fname", "Unknown User") + " " +
                sharedPref.getString("lname", "")
        val currentUserAvatar = sharedPref.getString("userAvatarUrl", null)

        Log.d("CommunityFragment", "Loaded user: $currentUserName, $currentUserEmail")

        postAdapter = PostAdapter(emptyList(), this , currentUserEmail)
        recyclerView?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }

        searchIcon?.setOnClickListener {
         findNavController().navigate(R.id.action_nav_community_to_searchFragment)
//            Toast.makeText(requireContext(), "Search icon clicked", Toast.LENGTH_SHORT).show()
        }

        createIcon?.setOnClickListener {
            findNavController().navigate(R.id.action_communityFragment_to_createPostFragment)
        }

        setupPostCreationResultListener()
        fetchPosts()
    }

    override fun onDelete(postId: Int) {
        deletePostById(postId)
    }

    override fun onLike(postId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val api = ApiClient.getTokenService(requireContext())
                val requestBody = LikeRequest(like = true)
                val response = api.likePost(postId, requestBody)

                if (response.isSuccessful) {
                    val newLikeCount = response.body()?.data?.likes
                    if (newLikeCount != null) {
                        postAdapter?.updatePostCounts(postId, likes = newLikeCount)
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("CommunityFragment", "API Error: ${response.code()} - $errorBody")
//                    Toast.makeText(requireContext(), "Failed: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }





    override fun onComment(postId: Int) {
        val commentsFragment = CommentsFragment.newInstance(postId)

        commentsFragment.setCommentCountUpdateListener { updatedCount ->
            postAdapter?.updatePostCounts(postId, commentCount = updatedCount)
        }

        commentsFragment.show(parentFragmentManager, CommentsFragment.TAG)
    }

    private fun fetchPosts() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService?.listAllPosts()
                if (response?.isSuccessful == true && response.body() != null) {
                    val posts = response.body()!!.data
                    postAdapter?.updateData(posts)
                    Toast.makeText(requireContext(), "Feed refreshed ", Toast.LENGTH_SHORT).show()
                } else {
                    val errorMsg = response?.errorBody()?.string() ?: "Unknown error"
                    Log.e("CommunityFragment", "API Error: ${response?.code()} - $errorMsg")
                    Toast.makeText(requireContext(), "Failed to load feed", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("CommunityFragment", "Network Exception: ${e.message}", e)
                Toast.makeText(requireContext(), "Network Error: Could not connect", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupPostCreationResultListener() {
        parentFragmentManager.setFragmentResultListener(
            POST_CREATION_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val shouldRefresh = bundle.getBoolean(REFRESH_FEED_KEY, false)
            if (shouldRefresh) fetchPosts()
        }
    }

    private fun deletePostById(postId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = api?.deletePost(postId)
                if (response?.isSuccessful == true) {
                    Toast.makeText(requireContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show()
                    fetchPosts()
                } else {
                    val errorMsg = response?.errorBody()?.string() ?: "Delete failed"
                    Toast.makeText(requireContext(), "Error: $errorMsg", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Network error during deletion", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }
}