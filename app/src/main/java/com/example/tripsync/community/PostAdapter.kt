package com.example.tripsync.community

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tripsync.R
import com.example.tripsync.api.models.Post
import com.example.tripsync.api.models.PostActionListener

class PostAdapter(
    private var posts: List<Post>,
    private val listener: PostActionListener,
    private val currentUserEmail: String?
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val optionsMenu: ImageView = itemView.findViewById(R.id.three_dots)
        val userAvatar: ImageView = itemView.findViewById(R.id.post_user_avatar)
        val userName: TextView = itemView.findViewById(R.id.post_user_name)
        val time: TextView = itemView.findViewById(R.id.post_time)
        val caption: TextView = itemView.findViewById(R.id.post_caption)
        val mediaImage: ImageView = itemView.findViewById(R.id.post_media_image)
        val iconLike: ImageView = itemView.findViewById(R.id.icon_like)
        val iconComment: ImageView = itemView.findViewById(R.id.icon_comment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]


        val context = holder.itemView.context
        val sharedPref = context.getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val currentUserName = sharedPref.getString("userName", "Unknown User")
        val currentUserAvatarUri = sharedPref.getString("userAvatarUrl", null)

        holder.userName.text = currentUserName

        holder.caption.text = post.desc.ifEmpty { "No caption" }

        val liked = getLikeState(context, post.id)
        holder.iconLike.setImageResource(if (liked) R.drawable.liked else R.drawable.like)



        holder.time.text = "Just now"

        if (!post.img_url.isNullOrEmpty()) {
            holder.mediaImage.visibility = View.VISIBLE
            val imageUrl = post.img_url
            Log.d("GlideDebug", "Loading image: ${post.img_url}")

            if (imageUrl.startsWith("content://") || imageUrl.startsWith("file://") || imageUrl.startsWith("/storage")) {

                Glide.with(context)
                    .load(Uri.parse(imageUrl))
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.mediaImage)
            } else {
                Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.mediaImage)
            }
        } else {
            holder.mediaImage.visibility = View.GONE
        }

        if (post.user_email == currentUserEmail) {
            holder.optionsMenu.visibility = View.VISIBLE
            holder.optionsMenu.setOnClickListener { showPopupMenu(it, post) }
        } else {
            holder.optionsMenu.visibility = View.GONE
        }

        holder.iconLike.setOnClickListener {
            val newState = !getLikeState(context, post.id)
            saveLikeState(context, post.id, newState)
            holder.iconLike.setImageResource(if (newState) R.drawable.liked else R.drawable.like)
            listener.onLike(post.id)
        }

        holder.iconComment.setOnClickListener {
            listener.onComment(post.id)
        }

    }

    private fun showPopupMenu(view: View, post: Post) {
        val context = view.context
        val popup = PopupMenu(context, view)
        popup.menuInflater.inflate(R.menu.post_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete -> {
                    confirmAndDelete(context, post)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun confirmAndDelete(context: Context, post: Post) {
        AlertDialog.Builder(context)
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to permanently delete this post?")
            .setPositiveButton("Delete") { dialog, _ ->
                listener.onDelete(post.id)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun getItemCount(): Int = posts.size

    fun updateData(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }

    fun saveLikeState(context: Context, postId: Int, liked: Boolean) {
        val sp = context.getSharedPreferences("liked_posts", Context.MODE_PRIVATE)
        sp.edit().putBoolean(postId.toString(), liked).apply()
    }

    fun getLikeState(context: Context, postId: Int): Boolean {
        val sp = context.getSharedPreferences("liked_posts", Context.MODE_PRIVATE)
        return sp.getBoolean(postId.toString(), false)
    }

}