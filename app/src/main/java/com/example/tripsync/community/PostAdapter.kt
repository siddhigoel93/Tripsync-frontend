package com.example.tripsync.community

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tripsync.R
import com.example.tripsync.api.models.Post
import com.example.tripsync.api.models.PostActionListener
import com.example.tripsync.utils.DialogUtils
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class PostAdapter(
    private var posts: List<Post>,
    private val listener: PostActionListener,
    private val currentUserEmail: String?
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val commentCounts = mutableMapOf<Int, Int>()

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val optionsMenu: ImageView = itemView.findViewById(R.id.three_dots)
        val userAvatar: ImageView = itemView.findViewById(R.id.post_user_avatar)
        val userName: TextView = itemView.findViewById(R.id.post_user_name)
        val time: TextView = itemView.findViewById(R.id.post_time)
        val title: TextView = itemView.findViewById(R.id.post_title)
        val caption: TextView = itemView.findViewById(R.id.post_caption)
        val mediaImage: ImageView = itemView.findViewById(R.id.post_media_image)
        val mediaVideo: VideoView? = itemView.findViewById(R.id.post_media_video)
        val videoPlayButton: ImageView? = itemView.findViewById(R.id.video_play_button)
        val iconLike: ImageView = itemView.findViewById(R.id.icon_like)
        val iconComment: ImageView = itemView.findViewById(R.id.icon_comment)
        val likeCount: TextView = itemView.findViewById(R.id.like_count)
        val commentCount: TextView = itemView.findViewById(R.id.comment_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        val context = holder.itemView.context

        // Set user info
        val user = post.user
        holder.userName.text = if (user != null) {
            val fullName = "${user.fname ?: ""} ${user.lname ?: ""}".trim()
            if (fullName.isNotEmpty()) fullName else "Unknown User"
        } else {
            "Unknown User"
        }

        Log.d("PostDebug", "User object for post ${post.id}: ${post.user}")

        holder.caption.text = post.desc.ifEmpty { "No caption" }
        holder.title.text = post.title.ifEmpty { "No title" }
        holder.time.text = formatTimeAgo(post.created)

        // Set user avatar
        val userPic = user?.pic
        if (!userPic.isNullOrEmpty()) {
            Glide.with(context)
                .load(userPic)
                .placeholder(R.drawable.placeholder_image)
                .into(holder.userAvatar)
        } else {
            holder.userAvatar.setImageResource(R.drawable.placeholder_image)
        }

        // Handle media display (image or video)
        handleMediaDisplay(holder, post, context)

        // Set comment count
        val displayCommentCount = commentCounts[post.id] ?: post.total_comments ?: post.comments?.size ?: 0
        holder.commentCount.text = displayCommentCount.toString()

        // Set like count
        holder.likeCount.text = post.likes?.toString() ?: "0"

        Log.d("PostAdapter", "Post ${post.id}: likes=${post.likes}, comments original=${post.comments?.size}, tracked=${commentCounts[post.id]}, displaying=$displayCommentCount")

        // Handle like button
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

        Log.d("PostAdapter", "Post ${post.id} owner: ${post.owner}")

        holder.optionsMenu.setOnClickListener { showPopupMenu(it, post) }
    }

    private fun handleMediaDisplay(holder: PostViewHolder, post: Post, context: Context) {
        // Check if post has video
        if (!post.vid_url.isNullOrEmpty()) {
            // Show video, hide image
            holder.mediaVideo?.visibility = View.VISIBLE
            holder.videoPlayButton?.visibility = View.VISIBLE
            holder.mediaImage.visibility = View.GONE

            val videoView = holder.mediaVideo
            val playButton = holder.videoPlayButton

            if (videoView != null && playButton != null) {
                try {
                    Log.d("PostAdapter", "Loading video: ${post.vid_url}")

                    videoView.setVideoURI(Uri.parse(post.vid_url))

                    videoView.setOnPreparedListener { mp ->
                        mp.isLooping = false
                        playButton.visibility = View.GONE
                        mp.start()
                    }

                    videoView.setOnErrorListener { _, what, extra ->
                        Log.e("PostAdapter", "Video error: what=$what, extra=$extra, url=${post.vid_url}")
                        playButton.visibility = View.VISIBLE
                        true
                    }

                    // Play button click
                    playButton.setOnClickListener {
                        if (videoView.isPlaying) {
                            videoView.pause()
                            playButton.visibility = View.VISIBLE
                        } else {
                            videoView.start()
                            playButton.visibility = View.GONE
                        }
                    }

                    // Video click to pause/play
                    videoView.setOnClickListener {
                        if (videoView.isPlaying) {
                            videoView.pause()
                            playButton.visibility = View.VISIBLE
                        } else {
                            videoView.start()
                            playButton.visibility = View.GONE
                        }
                    }

                } catch (e: Exception) {
                    Log.e("PostAdapter", "Error setting up video", e)
                    videoView.visibility = View.GONE
                    playButton?.visibility = View.GONE
                }
            }
        }
        // Check if post has image
        else if (!post.img_url.isNullOrEmpty()) {
            // Show image, hide video
            holder.mediaImage.visibility = View.VISIBLE
            holder.mediaVideo?.visibility = View.GONE
            holder.videoPlayButton?.visibility = View.GONE

            Glide.with(context)
                .load(post.img_url)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(holder.mediaImage)
        }
        // No media
        else {
            holder.mediaImage.visibility = View.GONE
            holder.mediaVideo?.visibility = View.GONE
            holder.videoPlayButton?.visibility = View.GONE
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
        DialogUtils.showConfirmationDialog(
            context,
            "Delete Post",
            "Are you sure you want to permanently delete this post?",
            positiveButtonText = "Delete",
            negativeButtonText = "Cancel",
            onPositiveClick = {
                listener.onDelete(post.id)
            }
        )
    }

    override fun getItemCount(): Int = posts.size

    fun updateData(newPosts: List<Post>) {
        posts = newPosts
        newPosts.forEach { post ->
            if (!commentCounts.containsKey(post.id)) {
                val count = post.total_comments ?: post.comments?.size ?: 0
                commentCounts[post.id] = count
                Log.d("PostAdapter", "Initialized comment count for post ${post.id}: $count")
            }
        }
        notifyDataSetChanged()
    }

    fun updatePostCounts(postId: Int, likes: Int? = null, commentCount: Int? = null) {
        val position = posts.indexOfFirst { it.id == postId }
        if (position != -1) {
            likes?.let {
                posts[position].likes = it
                Log.d("PostAdapter", "Updated likes for post $postId to $it")
            }
            commentCount?.let {
                commentCounts[postId] = it
                Log.d("PostAdapter", "Updated comment count for post $postId to $it")
            }
            notifyItemChanged(position)
        }
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