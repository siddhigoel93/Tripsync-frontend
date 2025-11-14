package com.example.tripsync

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TripmateAdapter(
    private val items: List<TripmateItem>
) : RecyclerView.Adapter<TripmateAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tripmate_box, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.meta.text = item.meta
        holder.image.setImageResource(item.imageDrawable)
        holder.itemView.setOnClickListener {
            it.alpha = 0.6f
            it.postDelayed({ it.alpha = 1f }, 120)
        }
    }

    override fun getItemCount(): Int = items.size

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.iv_tripmate_image)
        val name: TextView = view.findViewById(R.id.tv_tripmate_name)
        val meta: TextView = view.findViewById(R.id.tv_tripmate_meta)
    }
}
