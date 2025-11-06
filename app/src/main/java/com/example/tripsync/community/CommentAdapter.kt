package com.example.tripsync.community

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tripsync.R
import com.example.tripsync.api.models.CommentActionListener
import com.example.tripsync.api.models.CommentData

class CommentAdapter(
    private val context: Context,
    private var comments: MutableList<CommentData>,
    private val listener: CommentActionListener
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userAvatar: ImageView = itemView.findViewById(R.id.comment_user_avatar)
        val userName: TextView = itemView.findViewById(R.id.username)
        val commentText: TextView = itemView.findViewById(R.id.comment_text)
        val optionsMenu: ImageView = itemView.findViewById(R.id.three_dots)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]

        // Use safe access to ensure 'comment.user' is not null before accessing its properties
        comment.user?.let { user ->
            // This code block only executes if 'comment.user' is NOT null

            // 1. Set the user name (Safely access fname and lname)
            holder.userName.text = "${user.fname} ${user.lname}"

            // 2. Load the avatar (Safely access pic)
            val avatarUrl = user.pic

            if (!avatarUrl.isNullOrEmpty()) {
                Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .circleCrop()
                    .into(holder.userAvatar)
            } else {
                holder.userAvatar.setImageResource(R.drawable.placeholder_image)
            }

        } ?: run {
            holder.userName.text = "Unknown User"
            holder.userAvatar.setImageResource(R.drawable.placeholder_image)
            // Optionally, log an error or hide the entire comment if user data is critical
        }

        holder.commentText.text = comment.text

        if (comment.owner) {
            holder.optionsMenu.visibility = View.VISIBLE
            holder.optionsMenu.setOnClickListener {
                showPopupMenu(it, comment, position)
            }
        } else {
            holder.optionsMenu.visibility = View.GONE
        }

        if (comment.owner) {
            holder.optionsMenu.visibility = View.VISIBLE
            holder.optionsMenu.setOnClickListener {
                showPopupMenu(it, comment, position)
            }
        } else {
            holder.optionsMenu.visibility = View.GONE
        }
    }

    fun addComment(newComment: CommentData) {
        comments.add(0, newComment)
        notifyItemInserted(0)
    }

    fun removeComment(position: Int) {
        comments.removeAt(position)
        notifyItemRemoved(position)
    }

    fun updateComment(updatedComment: CommentData, position: Int) {
        comments[position] = updatedComment
        notifyItemChanged(position)
    }



    private fun showPopupMenu(view: View, comment: CommentData, position: Int) {
        val popup = android.widget.PopupMenu(context, view)
        // Ensure you have a 'comment_menu.xml' defined with action_edit and action_delete
        popup.menuInflater.inflate(R.menu.comment_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    showEditDialog(comment, position)
                    true
                }
                R.id.action_delete -> {
                    confirmAndDelete(comment, position)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showEditDialog(comment: CommentData, position: Int) {
        val paddingDp = 16
        val paddingPx = (paddingDp * context.resources.displayMetrics.density).toInt()

        val editText = EditText(context).apply {
            setText(comment.text)
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        AlertDialog.Builder(context)
            .setTitle("Edit Comment")
            .setView(editText)
            .setPositiveButton("Save") { dialog, _ ->
                val newText = editText.text.toString().trim()
                if (newText.isNotEmpty() && newText != comment.text) {
                    listener.onUpdate(comment.id, newText)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun confirmAndDelete(comment: CommentData, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Delete Comment")
            .setMessage("Are you sure you want to delete this comment?")
            .setPositiveButton("Delete") { dialog, _ ->
                listener.onDelete(comment.id, position)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun getItemCount(): Int = comments.size
}