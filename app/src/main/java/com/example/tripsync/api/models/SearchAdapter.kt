package com.example.tripsync.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.R
import com.example.tripsync.api.models.UserS

class SearchAdapter(
    private val users: List<UserS>,
    private val onClick: (UserS) -> Unit
) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.friend_username_text)
        val emailText: TextView = view.findViewById(R.id.friend_details_text)
        val actionIcon: View = view.findViewById(R.id.action_icon)




        init {
            // Click on entire item
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onClick(users[position])
                }
            }

            // Optional: click on action icon separately
            actionIcon?.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onClick(users[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.nameText.text = user.fullName
        holder.actionIcon.setOnClickListener {
            onClick(user)
        }
    }

    override fun getItemCount(): Int = users.size
}
