package com.example.tripsync

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.models.friend_FriendRequestBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class ConnectionAdapter(
    private val items: MutableList<TripmateConnect>
) : RecyclerView.Adapter<ConnectionAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connection_box, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.role.text = item.meta
        val url = item.avatarUrl
        if (!url.isNullOrEmpty()) {
            Glide.with(holder.avatar.context)
                .load(url)
                .placeholder(R.drawable.ic_avatar_placeholder)
                .error(R.drawable.ic_avatar_placeholder)
                .circleCrop()
                .into(holder.avatar)
        } else {
            holder.avatar.setImageResource(R.drawable.ic_avatar_placeholder)
        }

        holder.sendButton.setOnClickListener {
            holder.sendButton.isEnabled = false
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val receiverId = item.id.toIntOrNull() ?: -1
                    if (receiverId <= 0) {
                        Toast.makeText(holder.itemView.context, "Invalid user id", Toast.LENGTH_SHORT).show()
                        holder.sendButton.isEnabled = true
                        return@launch
                    }
                    val body = friend_FriendRequestBody(receiver_id = receiverId, message = "Hi")
                    Log.i("FriendReq", "Sending friend request to id=$receiverId")
                    val resp = ApiClient.getTripmateService(holder.itemView.context).sendFriendRequest(body)
                    if (resp.isSuccessful && resp.code() == 201) {
                        Toast.makeText(holder.itemView.context, "request sent", Toast.LENGTH_SHORT).show()
                        Log.i("FriendReq", "Success code=${resp.code()} body=${resp.body()}")
                    } else {
                        val errBody = try { resp.errorBody()?.string() } catch (ex: Exception) { null }
                        if (!errBody.isNullOrEmpty()) {
                            try {
                                val jo = JSONObject(errBody)
                                if (jo.has("errors")) {
                                    val errors = jo.getJSONObject("errors")
                                    if (errors.has("receiver_id")) {
                                        val arr = errors.getJSONArray("receiver_id")
                                        if (arr.length() > 0) {
                                            val msg = arr.optString(0)
                                            if (msg.contains("Friend request already sent", ignoreCase = true)) {
                                                Toast.makeText(holder.itemView.context, "request already sent", Toast.LENGTH_SHORT).show()
                                                holder.sendButton.isEnabled = true
                                                Log.i("FriendReq", "Already sent: $msg")
                                                return@launch
                                            }
                                        }
                                    }
                                }
                                if (jo.has("message")) {
                                    val message = jo.optString("message")
                                    Toast.makeText(holder.itemView.context, message, Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(holder.itemView.context, "request failed: ${resp.code()}", Toast.LENGTH_SHORT).show()
                                }
                            } catch (je: Exception) {
                                Toast.makeText(holder.itemView.context, "request failed: ${resp.code()}", Toast.LENGTH_SHORT).show()
                                Log.i("FriendReq", "Failed code=${resp.code()} body=$errBody")
                            }
                        } else {
                            Toast.makeText(holder.itemView.context, "request failed: ${resp.code()}", Toast.LENGTH_SHORT).show()
                            Log.i("FriendReq", "Failed code=${resp.code()} empty errorBody")
                        }
                        holder.sendButton.isEnabled = true
                    }
                } catch (e: Exception) {
                    Log.e("FriendReq", "Error sending request", e)
                    Toast.makeText(holder.itemView.context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    holder.sendButton.isEnabled = true
                }
            }
        }

        holder.itemView.setOnClickListener {
            it.alpha = 0.6f
            it.postDelayed({ it.alpha = 1f }, 120)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<TripmateConnect>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ImageView = view.findViewById(R.id.iv_connection_avatar)
        val name: TextView = view.findViewById(R.id.tv_connection_name)
        val role: TextView = view.findViewById(R.id.tv_connection_role)
        val sendButton: Button = view.findViewById(R.id.btn_send_request)
    }
}
