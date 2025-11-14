package com.example.tripsync.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tripsync.R
import com.example.tripsync.api.models.Conversation
import com.google.android.material.imageview.ShapeableImageView
import java.text.SimpleDateFormat
import java.util.*

class ConversationsAdapter(
    private var conversations: MutableList<Conversation>,
    private val onConversationClick: (Conversation) -> Unit
) : RecyclerView.Adapter<ConversationsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ShapeableImageView = view.findViewById(R.id.conversation_avatar)
        val name: TextView = view.findViewById(R.id.conversation_name)
        val lastMessage: TextView = view.findViewById(R.id.last_message)
        val timestamp: TextView = view.findViewById(R.id.timestamp)
        val unreadBadge: TextView = view.findViewById(R.id.unread_badge)
//        val groupIndicatorCard: View = view.findViewById(R.id.group_indicator_card)
        val groupIndicator: ImageView = view.findViewById(R.id.group_indicator)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val conversation = conversations[position]
        val context = holder.itemView.context

        // Get current user ID
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getString("self_id", "-1")?.toIntOrNull() ?: -1

        // Determine display name and avatar
        if (conversation.is_group == true) {
            // Group chat
            holder.name.text = conversation.name ?: "Unnamed Group"
            holder.groupIndicator.visibility = View.VISIBLE

            // Show group avatar or default
//            if (!conversation.group_avatar.isNullOrEmpty()) {
//                Glide.with(context)
//                    .load(conversation.group_avatar)
//                    .placeholder(R.drawable.tripmates)
//                    .error(R.drawable.tripmates)
//                    .into(holder.avatar)
//            } else {
//                holder.avatar.setImageResource(R.drawable.tripmates)
//            }
        } else {
            // 1-on-1 chat - show other participant
            holder.groupIndicator.visibility = View.GONE
            val otherParticipant = conversation.participants.firstOrNull { it.id != currentUserId }

            if (otherParticipant != null) {
                holder.name.text = otherParticipant.name ?: otherParticipant.email

//                if (!otherParticipant.avatar.isNullOrEmpty()) {
//                    Glide.with(context)
//                        .load(otherParticipant.avatar)
//                        .placeholder(R.drawable.placeholder_image)
//                        .error(R.drawable.placeholder_image)
//                        .into(holder.avatar)
//                } else {
//                    holder.avatar.setImageResource(R.drawable.placeholder_image)
//                }
            } else {
                holder.name.text = conversation.name ?: "Unknown"
                holder.avatar.setImageResource(R.drawable.placeholder_image)
            }
        }

        // Last message
        if (conversation.last_message != null) {
            val lastMsg = conversation.last_message
            val senderName = if (lastMsg.sender.id == currentUserId) {
                "You"
            } else if (conversation.is_group == true) {
                lastMsg.sender.name?.split(" ")?.firstOrNull() ?: "Someone"
            } else {
                ""
            }

            val prefix = if (senderName.isNotEmpty()) "$senderName: " else ""
            holder.lastMessage.text = "$prefix${lastMsg.content}"
            holder.lastMessage.setTextColor(context.getColor(android.R.color.darker_gray))
        } else {
            holder.lastMessage.text = "No messages yet"
            holder.lastMessage.setTextColor(context.getColor(android.R.color.darker_gray))
        }

        // Timestamp
        holder.timestamp.text = formatTimestamp(conversation.updated_at)

        // Unread badge
        val unreadCount = conversation.unread_count ?: 0
        if (unreadCount > 0) {
            holder.unreadBadge.visibility = View.VISIBLE
            holder.unreadBadge.text = if (unreadCount > 99) "99+" else unreadCount.toString()
        } else {
            holder.unreadBadge.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            onConversationClick(conversation)
        }
    }

    override fun getItemCount() = conversations.size

    fun updateList(newConversations: List<Conversation>) {
        conversations.clear()
        conversations.addAll(newConversations)
        notifyDataSetChanged()
    }

    private fun formatTimestamp(timestamp: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(timestamp) ?: return ""

            val now = Calendar.getInstance()
            val messageTime = Calendar.getInstance().apply { time = date }

            when {
                isSameDay(now, messageTime) -> {
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
                }
                isYesterday(now, messageTime) -> "Yesterday"
                isSameWeek(now, messageTime) -> {
                    SimpleDateFormat("EEE", Locale.getDefault()).format(date)
                }
                else -> {
                    SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date)
                }
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(cal1: Calendar, cal2: Calendar): Boolean {
        val yesterday = cal1.clone() as Calendar
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        return isSameDay(yesterday, cal2)
    }

    private fun isSameWeek(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
    }
}