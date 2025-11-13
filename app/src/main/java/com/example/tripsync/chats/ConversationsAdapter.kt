package com.example.tripsync

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.R
import com.example.tripsync.api.models.Conversation
import java.text.SimpleDateFormat
import java.util.*

class ConversationsAdapter(
    private var conversations: List<Conversation>,
    private val onClick: (Conversation) -> Unit
) : RecyclerView.Adapter<ConversationsAdapter.ConversationViewHolder>() {

    inner class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatar: ImageView = itemView.findViewById(R.id.row_avatar)
        val nameTv: TextView = itemView.findViewById(R.id.row_name)
        val lastMsgTv: TextView = itemView.findViewById(R.id.row_message)
        val timeTv: TextView = itemView.findViewById(R.id.row_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_list_row, parent, false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val conversation = conversations[position]

        // Set conversation name
        holder.nameTv.text = conversation.name

        // Set last message
        holder.lastMsgTv.text = conversation.last_message?.content ?: "No messages yet"

        // Set timestamp (HH:mm)
        holder.timeTv.text = conversation.last_message?.timestamp?.let { formatTime(it) } ?: ""

        // TODO: Set avatar if you have avatar URLs
        holder.avatar.setImageResource(R.drawable.avatar_1)

        holder.itemView.setOnClickListener { onClick(conversation) }
    }

    override fun getItemCount(): Int = conversations.size

    fun updateList(newList: List<Conversation>) {
        conversations = newList
        notifyDataSetChanged()
    }

    // Optional: format ISO timestamp to HH:mm
    private fun formatTime(isoTime: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(isoTime)
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            formatter.format(date ?: Date())
        } catch (e: Exception) {
            ""
        }
    }
}
