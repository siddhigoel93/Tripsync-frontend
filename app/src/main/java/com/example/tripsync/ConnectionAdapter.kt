package com.example.tripsync

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ConnectionAdapter(
    private val items: List<ConnectionItem>
) : RecyclerView.Adapter<ConnectionAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connection_box, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.role.text = item.meta
        holder.avatar.setImageResource(item.avatarDrawable)
        holder.itemView.setOnClickListener {
            it.alpha = 0.6f
            it.postDelayed({ it.alpha = 1f }, 120)
        }
    }

    override fun getItemCount(): Int = items.size

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ImageView = view.findViewById(R.id.iv_connection_avatar)
        val name: TextView = view.findViewById(R.id.tv_connection_name)
        val role: TextView = view.findViewById(R.id.tv_connection_role)
    }
}
