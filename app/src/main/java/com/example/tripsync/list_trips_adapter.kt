package com.example.tripsync

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.network.models.TripItem

class ListTripsAdapter(private var items: List<TripItem>, private val onClick: (TripItem) -> Unit) : RecyclerView.Adapter<ListTripsAdapter.VH>() {
    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_trip_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_trip, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.title.text = item.tripname ?: "Untitled"
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<TripItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
