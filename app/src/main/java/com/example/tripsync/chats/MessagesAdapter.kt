package com.example.tripsync

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.api.models.Message
import java.text.SimpleDateFormat
import java.util.*

class MessagesAdapter(
    private var messages: MutableList<Message>,
    private val currentUserId: Int,
    private val isGroup: Boolean,
    private val onEditMessage: (Message, String) -> Unit,
    private val onDeleteMessage: (Message) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    class SentMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.message_text)
        val messageTime: TextView = view.findViewById(R.id.message_time)
        val editedIndicator: TextView = view.findViewById(R.id.edited_indicator)
    }

    class ReceivedMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val senderName: TextView = view.findViewById(R.id.sender_name)
        val messageText: TextView = view.findViewById(R.id.message_text)
        val messageTime: TextView = view.findViewById(R.id.message_time)
        val editedIndicator: TextView = view.findViewById(R.id.edited_indicator)
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

                // Show edited indicator
                if (message.is_edited == true) {
                    holder.editedIndicator.visibility = View.VISIBLE
                    val editedTime = message.edited_at?.let { formatTime(it) } ?: ""
                    holder.editedIndicator.text = "Edited $editedTime"
                } else {
                    holder.editedIndicator.visibility = View.GONE
                }

                // Long press to show options (only for sent messages)
                holder.itemView.setOnLongClickListener {
                    showMessageOptions(holder.itemView.context, message)
                    true
                }

                Log.d("MessagesAdapter", "Binding SENT message: ${message.content}")
            }
            is ReceivedMessageViewHolder -> {
                holder.messageText.text = message.content
                holder.messageTime.text = formatTime(message.timestamp)

                // Show edited indicator
                if (message.is_edited == true) {
                    holder.editedIndicator.visibility = View.VISIBLE
                    val editedTime = message.edited_at?.let { formatTime(it) } ?: ""
                    holder.editedIndicator.text = "Edited $editedTime"
                } else {
                    holder.editedIndicator.visibility = View.GONE
                }

                // Show sender name only in group chats
                if (isGroup) {
                    holder.senderName.visibility = View.VISIBLE
                    holder.senderName.text = message.sender.name?.split(" ")?.firstOrNull()
                        ?: message.sender.email?.split("@")?.firstOrNull()
                                ?: "Unknown"
                } else {
                    holder.senderName.visibility = View.GONE
                }

                Log.d("MessagesAdapter", "Binding RECEIVED message: ${message.content}")
            }
        }
    }

    private fun showMessageOptions(context: android.content.Context, message: Message) {
        val options = arrayOf("Edit", "Delete")

        AlertDialog.Builder(context)
            .setTitle("Message Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditDialog(context, message)
                    1 -> showDeleteConfirmation(context, message)
                }
            }
            .show()
    }

    private fun showEditDialog(context: android.content.Context, message: Message) {
        val input = EditText(context)
        input.setText(message.content)
        input.setSelection(message.content.length)

        AlertDialog.Builder(context)
            .setTitle("Edit Message")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newContent = input.text.toString().trim()
                if (newContent.isNotEmpty() && newContent != message.content) {
                    onEditMessage(message, newContent)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmation(context: android.content.Context, message: Message) {
        AlertDialog.Builder(context)
            .setTitle("Delete Message")
            .setMessage("Are you sure you want to delete this message?")
            .setPositiveButton("Delete") { _, _ ->
                onDeleteMessage(message)
            }
            .setNegativeButton("Cancel", null)
            .show()
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
                if (i < newMessages.size &&
                    (messages[i].id != newMessages[i].id ||
                            messages[i].content != newMessages[i].content ||
                            messages[i].is_edited != newMessages[i].is_edited)) {
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