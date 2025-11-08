package com.example.tripsync

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tripsync.api.models.TrendingPlace

class TrendingDestinationAdapter(
    private val destinations: List<TrendingPlace>,
    private val clickListener: (TrendingPlace) -> Unit
) : RecyclerView.Adapter<TrendingDestinationAdapter.DestinationViewHolder>() {

    inner class DestinationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val destinationImage: ImageView = itemView.findViewById(R.id.destination_image)
        val destinationTitle: TextView = itemView.findViewById(R.id.destination_title)
        val destinationDescription: TextView = itemView.findViewById(R.id.destination_description)
        val actionArrow: ImageView = itemView.findViewById(R.id.action_arrow)

        fun bind(destination: TrendingPlace) {
            destinationTitle.text = destination.name
            destinationDescription.text = destination.fun_facts.joinToString("\n") { it.title }

            Glide.with(itemView.context)
                .load(destination.main)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .circleCrop() // optional, remove if rectangular is desired
                .into(destinationImage)

            itemView.setOnClickListener { clickListener(destination) }
            actionArrow.setOnClickListener { clickListener(destination) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.trending_destination_item, parent, false)
        return DestinationViewHolder(view)
    }

    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        holder.bind(destinations[position])
    }

    override fun getItemCount(): Int = destinations.size
}
