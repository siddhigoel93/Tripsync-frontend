package com.example.tripsync.community

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tripsync.R
import com.example.tripsync.api.models.Post
import com.example.tripsync.api.models.PostActionListener
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders


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

        val user = post.user
        holder.userName.text = if (user != null) {
            val fullName = "${user.fname ?: ""} ${user.lname ?: ""}".trim()
            if (fullName.isNotEmpty()) fullName else "Unknown User"
        } else {
            "Unknown User"
        }

        Log.d("PostDebug", "User object for post ${post.id}: ${post.user}")

        holder.caption.text = post.desc.ifEmpty { "No caption" }

        holder.time.text = formatTimeAgo(post.created)

        val userPic = user?.pic
        if (!userPic.isNullOrEmpty()) {
            Glide.with(context)
                .load(userPic)
                .placeholder(R.drawable.placeholder_image)
                .into(holder.userAvatar)
        } else {
            holder.userAvatar.setImageResource(R.drawable.placeholder_image)
        }

//        if (!post.img_url.isNullOrEmpty()) {
//            holder.mediaImage.visibility = View.VISIBLE
//            val imageUrl = post.img_url
//            Log.d("GlideDebug", "Loading image: $imageUrl")
//
//            if (imageUrl.startsWith("content://") ||
//                imageUrl.startsWith("file://") ||
//                imageUrl.startsWith("/storage")
//            ) {
//                Glide.with(context)
//                    .load(Uri.parse(imageUrl))
//                    .placeholder(R.drawable.placeholder_image)
//                    .into(holder.mediaImage)
//            } else {
//                // ✅ Only add Authorization header for your API URLs (not S3)
//                val glideUrl = if (imageUrl.contains("tripsync-media.s3.amazonaws.com")) {
//                    GlideUrl(imageUrl) // public S3 link, no auth needed
//                } else {
//                    val token = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
//                        .getString("access_token", null)
//
//                    if (token != null) {
//                        GlideUrl(
//                            imageUrl,
//                            LazyHeaders.Builder()
//                                .addHeader("Authorization", "Bearer $token")
//                                .addHeader("Accept", "image/*")
//                                .build()
//                        )
//                    } else GlideUrl(imageUrl)
//                }
//
//                Glide.with(context)
//                    .load(glideUrl)
//                    .placeholder(R.drawable.placeholder_image)
//                    .error(R.drawable.placeholder_image)
//                    .into(holder.mediaImage)
//            }
//        } else {
//            holder.mediaImage.visibility = View.GONE
//        }
        if (!post.img_url.isNullOrEmpty()) {
            holder.mediaImage.visibility = View.VISIBLE

            val imageUrl = post.img_url

            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(holder.mediaImage)
        } else {
            holder.mediaImage.visibility = View.GONE
        }


        val liked = getLikeState(context, post.id)
        holder.iconLike.setImageResource(if (liked) R.drawable.liked else R.drawable.like)

        holder.iconLike.setOnClickListener {
            val newState = !getLikeState(context, post.id)
            saveLikeState(context, post.id, newState)
            holder.iconLike.setImageResource(if (newState) R.drawable.liked else R.drawable.like)
            listener.onLike(post.id)
        }
        holder.iconComment.setOnClickListener {
            listener.onComment(post.id)
        }

        if (post.owner) {
            holder.optionsMenu.visibility = View.VISIBLE
            holder.optionsMenu.setOnClickListener { showPopupMenu(it, post) }
        } else {
            holder.optionsMenu.visibility = View.GONE
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

    private fun saveLikeState(context: Context, postId: Int, liked: Boolean) {
        val sp = context.getSharedPreferences("liked_posts", Context.MODE_PRIVATE)
        sp.edit().putBoolean(postId.toString(), liked).apply()
    }

    private fun getLikeState(context: Context, postId: Int): Boolean {
        val sp = context.getSharedPreferences("liked_posts", Context.MODE_PRIVATE)
        return sp.getBoolean(postId.toString(), false)
    }

    private fun formatTimeAgo(isoDate: String?): String {
        if (isoDate.isNullOrEmpty()) return "Unknown time"
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val time = sdf.parse(isoDate)?.time ?: return "Unknown time"

            val now = System.currentTimeMillis()
            val diff = now - time

            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            val days = TimeUnit.MILLISECONDS.toDays(diff)

            when {
                minutes < 1 -> "Just now"
                minutes < 60 -> "$minutes min ago"
                hours < 24 -> "$hours hr ago"
                days == 1L -> "Yesterday"
                days < 7 -> "$days days ago"
                else -> {
                    val outputFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    outputFormat.format(Date(time))
                }
            }
        } catch (e: Exception) {
            Log.e("TimeFormat", "Error parsing time: ${e.message}")
            "Unknown time"
        }
    }
}
