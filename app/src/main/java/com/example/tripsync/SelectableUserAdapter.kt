package com.example.tripsync.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tripsync.R
import com.example.tripsync.api.models.UserSearchResponse
import com.google.android.material.imageview.ShapeableImageView

class SelectableUsersAdapter(
    private var users: List<UserSearchResponse>,
    private val selectedUsers: MutableSet<UserSearchResponse>,
    private val onSelectionChanged: (UserSearchResponse, Boolean) -> Unit
) : RecyclerView.Adapter<SelectableUsersAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ShapeableImageView = view.findViewById(R.id.user_avatar)
        val name: TextView = view.findViewById(R.id.user_name)
        val email: TextView = view.findViewById(R.id.user_email)
        val checkbox: CheckBox = view.findViewById(R.id.user_checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_participant, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]

        // Set name
        val displayName = when {
            !user.fname.isNullOrEmpty() && !user.lname.isNullOrEmpty() ->
                "${user.fname} ${user.lname}"
            !user.fname.isNullOrEmpty() -> user.fname
            !user.lname.isNullOrEmpty() -> user.lname
            else -> "Unknown User"
        }
        holder.name.text = displayName

        // Set email
        holder.email.text = user.email ?: "No email"

        // Load avatar
        if (!user.profile_pic_url.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(user.profile_pic_url)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(holder.avatar)
        } else {
            holder.avatar.setImageResource(R.drawable.placeholder_image)
        }

        // Remove previous listeners to avoid multiple triggers
        holder.checkbox.setOnCheckedChangeListener(null)

        // Set checkbox state
        holder.checkbox.isChecked = selectedUsers.contains(user)

        // Handle item click
        holder.itemView.setOnClickListener {
            val newState = !holder.checkbox.isChecked
            holder.checkbox.isChecked = newState
            onSelectionChanged(user, newState)
        }

        // Handle checkbox click
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            onSelectionChanged(user, isChecked)
        }
    }

    override fun getItemCount() = users.size

    fun updateUsers(newUsers: List<UserSearchResponse>) {
        users = newUsers
        notifyDataSetChanged()
    }
}