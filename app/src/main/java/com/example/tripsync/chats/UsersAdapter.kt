package com.example.tripsync.adapters

import android.R.attr.onClick
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.R
import com.example.tripsync.api.models.User
import com.example.tripsync.api.models.UserSearchResponse

class UsersAdapter(
    private var users: List<UserSearchResponse>,
    private val onClick: (UserSearchResponse) -> Unit
) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatar: ImageView = itemView.findViewById(R.id.user_avatar)
        val nameTv: TextView = itemView.findViewById(R.id.user_name)
        val emailTv: TextView = itemView.findViewById(R.id.user_email)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]

        val displayName = user.name ?: user.first_name ?: "Unknown User"
        holder.nameTv.text = displayName
        holder.emailTv.text = user.email
        holder.avatar.setImageResource(R.drawable.avatar_1)

        holder.itemView.setOnClickListener {
            onClick(user)
        }
    }

    override fun getItemCount(): Int = users.size

    fun updateUsers(newUsers: List<UserSearchResponse>) {
        users = newUsers
        notifyDataSetChanged()
    }
}