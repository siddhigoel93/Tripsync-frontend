package com.example.tripsync

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.accept_TripmateRespondService
import com.example.tripsync.api.models.recieved_FriendRequestItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class recieved_FriendRequestAdapter(
    private val items: MutableList<recieved_FriendRequestItem>
) : RecyclerView.Adapter<recieved_FriendRequestAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recieved_item_friend_request, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val s = item.senderInfo
        holder.name.text = s?.fullName ?: s?.email ?: "Unknown"
        holder.meta.text = s?.phoneNumber ?: ""
        val url = s?.profilePic
        if (!url.isNullOrEmpty()) {
            Glide.with(holder.avatar.context).load(url).circleCrop()
                .placeholder(R.drawable.ic_avatar_placeholder)
                .error(R.drawable.ic_avatar_placeholder)
                .into(holder.avatar)
        } else holder.avatar.setImageResource(R.drawable.ic_avatar_placeholder)

        holder.acceptBtn.isEnabled = true
        holder.acceptBtn.setOnClickListener {
            holder.acceptBtn.isEnabled = false
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val service = ApiClient.createService(holder.itemView.context, accept_TripmateRespondService::class.java)
                    val requestId = item.id
                    val resp = withContext(Dispatchers.IO) { service.respond(requestId, com.example.tripsync.api.accept_RespondBody("accept")) }
                    if (resp.isSuccessful) {
                        val pos = holder.bindingAdapterPosition
                        if (pos >= 0 && pos < items.size) {
                            items.removeAt(pos)
                            notifyItemRemoved(pos)
                        }
                        Toast.makeText(holder.itemView.context, "Request accepted", Toast.LENGTH_SHORT).show()
                    } else {
                        holder.acceptBtn.isEnabled = true
                        Toast.makeText(holder.itemView.context, "failed to accept", Toast.LENGTH_SHORT).show()
                        Log.i("recieved_Adapter", "accept failed code=${resp.code()}")
                    }
                } catch (e: Exception) {
                    holder.acceptBtn.isEnabled = true
                    Toast.makeText(holder.itemView.context, "failed to accept", Toast.LENGTH_SHORT).show()
                    Log.e("recieved_Adapter", "Error accepting request", e)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun update(list: List<recieved_FriendRequestItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val avatar = v.findViewById<android.widget.ImageView>(R.id.iv_connection_avatar)
        val name = v.findViewById<android.widget.TextView>(R.id.tv_connection_name)
        val meta = v.findViewById<android.widget.TextView>(R.id.tv_connection_role)
        val acceptBtn = v.findViewById<AppCompatButton>(R.id.btn_accept_request)
    }
}
