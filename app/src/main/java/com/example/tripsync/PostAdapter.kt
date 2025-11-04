package com.example.tripsync

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.api.models.Post

//class PostAdapter(private val posts: List<Post>) :
//    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {
//
//    inner class PostViewHolder(val binding: ItemPostBinding) :
//        RecyclerView.ViewHolder(binding.root)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
//        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return PostViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
//        val post = posts[position]
//        with(holder.binding) {
//            tvTitle.text = post.title
//            tvDescription.text = post.desc
//            tvUserEmail.text = post.user_email
//            tvLocation.text = post.loc
//
//            // Load image if available
//            if (!post.img_url.isNullOrEmpty()) {
//                Glide.with(root.context)
//                    .load(post.img_url)
//                    .into(ivPostImage)
//            } else {
//                ivPostImage.visibility = View.GONE
//            }
//        }
//    }
//
//    override fun getItemCount() = posts.size
//}
