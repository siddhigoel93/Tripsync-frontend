package com.example.tripsync.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.models.CommentActionListener
import com.example.tripsync.api.models.CommentData
import com.example.tripsync.api.models.CommentTextRequest
import com.example.tripsync.community.CommentAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class CommentsFragment : BottomSheetDialogFragment(), CommentActionListener {

    private var postId: Int = -1
    private lateinit var commentsAdapter: CommentAdapter
    private lateinit var recyclerView: RecyclerView

    private val commentsList = mutableListOf<CommentData>()
    private var commentCountUpdateListener: ((Int) -> Unit)? = null

    companion object {
        const val TAG = "CommentsBottomSheet"
        private const val ARG_POST_ID = "postId"

        fun newInstance(postId: Int): CommentsFragment {
            val fragment = CommentsFragment()
            val args = Bundle()
            args.putInt(ARG_POST_ID, postId)
            fragment.arguments = args
            return fragment
        }
    }
    fun setCommentCountUpdateListener(listener: (Int) -> Unit) {
        commentCountUpdateListener = listener
    }
    override fun onStart() {
        super.onStart()
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_comments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postId = arguments?.getInt(ARG_POST_ID) ?: run { dismiss(); return }

        setupRecyclerView(view)
        setupListeners(view)
        fetchPostDetailsAndComments(postId)
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.comments_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        commentsAdapter = CommentAdapter(requireContext(), commentsList, this)
        recyclerView.adapter = commentsAdapter

    }

    private fun setupListeners(view: View) {
        val commentEditText: EditText = view.findViewById(R.id.edit_text_comment)
        val sendButton: ImageView = view.findViewById(R.id.btn_send_comment)

        sendButton.setOnClickListener {
            val commentText = commentEditText.text.toString().trim()
            if (commentText.isNotEmpty()) {
                sendCommentToApi(postId, commentText, commentEditText)
            }
        }
    }

    private fun fetchPostDetailsAndComments(postId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val api = ApiClient.getTokenService(requireContext())

                val response = api.getPostDetails(postId)

                if (response.isSuccessful) {
                    val comments = response.body()?.data?.comments
                    if (comments != null) {
                        commentsList.clear()

                        commentsList.addAll(comments.reversed())

                        commentsAdapter.notifyDataSetChanged()
                        commentCountUpdateListener?.invoke(comments.size)

                        if (comments.isNotEmpty()) {
                            recyclerView.scrollToPosition(0)
                        }
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to load comments"
                    Log.e(TAG, "Fetch Error: ${response.code()} - $errorMsg")
                    Toast.makeText(context, "Failed to load comments.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network Error fetching post details", e)
                Toast.makeText(context, "Network error.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun sendCommentToApi(postId: Int, commentText: String, inputField: EditText) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val api = ApiClient.getTokenService(requireContext())
                val requestBody = CommentTextRequest(text = commentText)
                val response = api.addComment(postId, requestBody)

                if (response.isSuccessful) {
                    val newCommentData = response.body()?.data
                    if (newCommentData != null) {
                        commentsAdapter.addComment(newCommentData)

                        inputField.setText("")
                        recyclerView.scrollToPosition(0)
                        commentCountUpdateListener?.invoke(commentsList.size)
                        Toast.makeText(context, "Comment added successfully!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(context, "Failed to add comment: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network Error adding comment", e)
                Toast.makeText(context, "Network error" , Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onUpdate(commentId: Int, newText: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val position = commentsList.indexOfFirst { it.id == commentId }
            if (position == -1) return@launch

            try {
                val api = ApiClient.getTokenService(requireContext())
                val requestBody = CommentTextRequest(text = newText)
                val response = api.updateComment(commentId, requestBody)

                if (response.isSuccessful) {
                    val updatedCommentData = response.body()?.data
                    if (updatedCommentData != null) {
                        commentsAdapter.updateComment(updatedCommentData, position)
                        Toast.makeText(context, "Comment updated.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Failed to update comment.", Toast.LENGTH_SHORT).show()
                    commentsAdapter.notifyItemChanged(position)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error updating comment.", Toast.LENGTH_LONG).show()
                commentsAdapter.notifyItemChanged(position)
            }
        }
    }
    override fun onDelete(commentId: Int, position: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            if (position == -1) return@launch

            try {
                val api = ApiClient.getTokenService(requireContext())
                val response = api.deleteComment(commentId)

                if (response.isSuccessful) {
                    commentsList.removeAt(position)
                    commentsAdapter.removeComment(position)

                    commentCountUpdateListener?.invoke(commentsList.size)

                    Toast.makeText(context, "Comment deleted successfully.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to delete comment.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error deleting comment.", Toast.LENGTH_LONG).show()
            }
        }
    }
}