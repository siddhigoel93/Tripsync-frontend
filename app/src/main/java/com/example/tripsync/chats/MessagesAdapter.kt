package com.example.tripsync

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.R
import com.example.tripsync.api.models.Message
import java.text.SimpleDateFormat
import java.util.*

class MessagesAdapter(
    private val messages: MutableList<Message>,
    private val selfId: Int,
    private val onMessageLongClick: (Message) -> Unit = {} // NEW: Long click callback
) : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val containerLeft: LinearLayout = itemView.findViewById(R.id.container_left)
        val bubbleLeft: TextView = itemView.findViewById(R.id.bubble_left)
        val timeLeft: TextView = itemView.findViewById(R.id.time_left)

        val containerRight: LinearLayout = itemView.findViewById(R.id.container_right)
        val bubbleRight: TextView = itemView.findViewById(R.id.bubble_right)
        val timeRight: TextView = itemView.findViewById(R.id.time_right)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        val senderId = message.sender?.id
        val isSelf = senderId == selfId
        val formattedTime = message.timestamp?.let { formatTime(it) } ?: ""

        if (senderId == null) {
            holder.containerLeft.visibility = View.VISIBLE
            holder.bubbleLeft.text = message.content ?: "(Unknown sender)"
            holder.timeLeft.text = formattedTime
            holder.containerRight.visibility = View.GONE
            return
        }
        if (isSelf) {
            holder.containerRight.visibility = View.VISIBLE
            holder.bubbleRight.text = message.content
            holder.timeRight.text = formattedTime

            holder.containerLeft.visibility = View.GONE

            holder.containerRight.setOnLongClickListener {
                onMessageLongClick(message)
                true
            }
        } else {
            holder.containerLeft.visibility = View.VISIBLE
            holder.bubbleLeft.text = message.content
            holder.timeLeft.text = formattedTime

            holder.containerRight.visibility = View.GONE

            holder.containerLeft.setOnLongClickListener {
                onMessageLongClick(message)
                true
            }
        }
    }

    override fun getItemCount(): Int = messages.size

    fun updateMessages(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

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