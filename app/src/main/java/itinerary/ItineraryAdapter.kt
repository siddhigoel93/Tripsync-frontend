package com.example.tripsync.itinerary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.R

sealed class ItineraryItem {
    data class Section(val iconRes: Int, val label: String): ItineraryItem()
    data class Card(val iconRes: Int, val title: String, val subtitle: String?): ItineraryItem()
}

private object ItineraryDiff : DiffUtil.ItemCallback<ItineraryItem>() {
    override fun areItemsTheSame(oldItem: ItineraryItem, newItem: ItineraryItem): Boolean = oldItem == newItem
    override fun areContentsTheSame(oldItem: ItineraryItem, newItem: ItineraryItem): Boolean = oldItem == newItem
}

class ItineraryAdapter : ListAdapter<ItineraryItem, RecyclerView.ViewHolder>(ItineraryDiff) {

    companion object {
        private const val TYPE_SECTION = 1
        private const val TYPE_CARD = 2
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is ItineraryItem.Section -> TYPE_SECTION
        is ItineraryItem.Card -> TYPE_CARD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SECTION -> SectionVH(inf.inflate(R.layout.item_itinerary_section, parent, false))
            else -> CardVH(inf.inflate(R.layout.item_itinerary_card, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ItineraryItem.Section -> (holder as SectionVH).bind(item)
            is ItineraryItem.Card -> (holder as CardVH).bind(item)
        }
    }

    class SectionVH(v: View) : RecyclerView.ViewHolder(v) {
        private val icon: ImageView = v.findViewById(R.id.ivSectionDot)
        private val title: TextView = v.findViewById(R.id.tvSectionTitle)
        fun bind(item: ItineraryItem.Section) {
            icon.setImageResource(item.iconRes)
            title.text = item.label
        }
    }

    class CardVH(v: View) : RecyclerView.ViewHolder(v) {
        private val icon: ImageView = v.findViewById(R.id.ivIcon)
        private val title: TextView = v.findViewById(R.id.tvTitle)
        private val subtitle: TextView = v.findViewById(R.id.tvSubtitle)
        fun bind(item: ItineraryItem.Card) {
            icon.setImageResource(item.iconRes)
            title.text = item.title
            if (item.subtitle.isNullOrBlank()) {
                subtitle.visibility = View.GONE
            } else {
                subtitle.visibility = View.VISIBLE
                subtitle.text = item.subtitle
            }
        }
    }
}
