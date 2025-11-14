package com.example.tripsync

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tripsync.R
import com.example.tripsync.api.models.Message
import com.google.android.material.imageview.ShapeableImageView
import java.text.SimpleDateFormat
import java.util.*

class MessagesAdapter(
    private var messages: MutableList<Message>,
    private val currentUserId: Int,
    private val isGroup: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    class SentMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.message_text)
        val messageTime: TextView = view.findViewById(R.id.message_time)
    }

    class ReceivedMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val senderAvatar: ShapeableImageView = view.findViewById(R.id.sender_avatar)
        val senderName: TextView = view.findViewById(R.id.sender_name)
        val messageText: TextView = view.findViewById(R.id.message_text)
        val messageTime: TextView = view.findViewById(R.id.message_time)
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        val isSentByMe = message.sender.id == currentUserId

        Log.d("MessagesAdapter", "Message ${message.id}: sender=${message.sender.id}, currentUser=$currentUserId, isSent=$isSentByMe")

        return if (isSentByMe) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_sent, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_received, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        when (holder) {
            is SentMessageViewHolder -> {
                holder.messageText.text = message.content
                holder.messageTime.text = formatTime(message.timestamp)
                Log.d("MessagesAdapter", "Binding SENT message: ${message.content}")
            }
            is ReceivedMessageViewHolder -> {
                holder.messageText.text = message.content
                holder.messageTime.text = formatTime(message.timestamp)

                // Show sender name only in group chats
                if (isGroup) {
                    holder.senderName.visibility = View.VISIBLE
                    holder.senderName.text = message.sender.name?.split(" ")?.firstOrNull()
                        ?: message.sender.email?.split("@")?.firstOrNull()
                                ?: "Unknown"
                } else {
                    holder.senderName.visibility = View.GONE
                }

                // Load avatar
//                val avatarUrl = message.sender.avatar
//                if (!avatarUrl.isNullOrEmpty()) {
//                    Glide.with(holder.itemView.context)
//                        .load(avatarUrl)
//                        .placeholder(R.drawable.placeholder_image)
//                        .error(R.drawable.placeholder_image)
//                        .into(holder.senderAvatar)
//                } else {
//                    holder.senderAvatar.setImageResource(R.drawable.placeholder_image)
//                }

                Log.d("MessagesAdapter", "Binding RECEIVED message: ${message.content}")
            }
        }
    }

    override fun getItemCount() = messages.size

    fun updateMessages(newMessages: List<Message>) {
        val oldSize = messages.size
        val newSize = newMessages.size

        Log.d("MessagesAdapter", "Updating messages: oldSize=$oldSize, newSize=$newSize")

        // If we have new messages, only notify about the new ones
        if (newSize > oldSize) {
            messages.clear()
            messages.addAll(newMessages)
            notifyItemRangeInserted(oldSize, newSize - oldSize)
        } else if (newSize < oldSize) {
            // Messages were deleted, full refresh
            messages.clear()
            messages.addAll(newMessages)
            notifyDataSetChanged()
        } else {
            // Same size, check if content changed
            var hasChanges = false
            for (i in messages.indices) {
                if (i < newMessages.size && messages[i].id != newMessages[i].id) {
                    hasChanges = true
                    break
                }
            }
            if (hasChanges) {
                messages.clear()
                messages.addAll(newMessages)
                notifyDataSetChanged()
            }
        }
    }

    private fun formatTime(timestamp: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(timestamp) ?: return ""

            val now = Calendar.getInstance()
            val messageTime = Calendar.getInstance().apply { time = date }

            if (isSameDay(now, messageTime)) {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            } else {
                SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(date)
            }
        } catch (e: Exception) {
            Log.e("MessagesAdapter", "Error formatting timestamp: $timestamp", e)
            ""
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}