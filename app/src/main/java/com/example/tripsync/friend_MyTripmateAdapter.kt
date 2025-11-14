package com.example.tripsync

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tripsync.api.models.friend_MyTripmateResponseItem

class friend_MyTripmateAdapter(
    private val items: MutableList<friend_MyTripmateResponseItem>
) : RecyclerView.Adapter<friend_MyTripmateAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tripmate_box, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.name.text = item.profileData?.fullName ?: "Unknown"
        holder.meta.text = item.profileData?.preference ?: "—"
        val url = item.profileData?.profilePic
        if (!url.isNullOrEmpty()) {
            Glide.with(holder.image.context)
                .load(url)
                .placeholder(R.drawable.ic_trip_placeholder)
                .error(R.drawable.ic_trip_placeholder)
                .circleCrop()
                .into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.ic_trip_placeholder)
        }
        holder.chatAction.setOnClickListener { }
    }

    override fun getItemCount() = items.size

    fun update(newList: List<friend_MyTripmateResponseItem>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.iv_tripmate_image)
        val name: TextView = view.findViewById(R.id.tv_tripmate_name)
        val meta: TextView = view.findViewById(R.id.tv_tripmate_meta)
        val chatAction: ImageView = view.findViewById(R.id.iv_chat_action)
    }
}
