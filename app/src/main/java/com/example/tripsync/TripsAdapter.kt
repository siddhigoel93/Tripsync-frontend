package com.example.tripsync

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TripsAdapter(
    private val items: List<Trip>,
    private val tripClickListener: (Trip) -> Unit,
    private val rowClickListener: (name: String, avatarRes: Int) -> Unit
) : RecyclerView.Adapter<TripsAdapter.VH>() {

    private val expandedPositions = mutableSetOf<Int>()
    private val sampleNames = listOf("Khushi Kumar", "Mina Dimple", "Rahul Singh", "Amit Patel", "Nisha Roy")
    private val sampleMessages = listOf(
        "I will meet you near red fort",
        "I am at the red fort",
        "Hi where are you?",
        "On my way",
        "Running 10 minutes late"
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_trip_card, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val trip = items[position]
        holder.bind(trip)

        holder.itemView.setOnClickListener { tripClickListener(trip) }
        holder.editBtn?.setOnClickListener { tripClickListener(trip) }

        val isExpanded = expandedPositions.contains(position)
        holder.setExpanded(isExpanded)

        holder.chevron?.setOnClickListener {
            val currentlyExpanded = expandedPositions.contains(position)
            if (currentlyExpanded) {
                expandedPositions.remove(position)
                holder.setExpanded(false)
                holder.expandedContainer.removeAllViews()
            } else {
                expandedPositions.add(position)
                holder.setExpanded(true)
                populateExpanded(holder.expandedContainer, trip)
            }
        }

        if (isExpanded) {
            populateExpanded(holder.expandedContainer, trip)
        } else {
            holder.expandedContainer.removeAllViews()
        }
    }

    override fun getItemCount(): Int = items.size

    private fun populateExpanded(container: LinearLayout, trip: Trip) {
        container.removeAllViews()
        val ctx = container.context
        val visibleCount = minOf(3, trip.avatarRes.size)
        for (i in 0 until visibleCount) {
            val row = LayoutInflater.from(ctx).inflate(R.layout.item_chat_list_row, container, false)
            val avatar = row.findViewById<ImageView>(R.id.row_avatar)
            val name = row.findViewById<TextView>(R.id.row_name)
            val message = row.findViewById<TextView>(R.id.row_message)
            val time = row.findViewById<TextView>(R.id.row_time)

            val avatarRes = trip.avatarRes.getOrNull(i) ?: R.drawable.avatar_1
            avatar.setImageResource(avatarRes)
            name.text = sampleNames.getOrNull(i) ?: "User $i"
            message.text = sampleMessages.getOrNull(i) ?: "Hey"
            time.text = "4:00"

            row.setOnClickListener {
                rowClickListener(name.text.toString(), avatarRes)
            }

            container.addView(row)
        }
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val banner: ImageView = view.findViewById(R.id.banner)
        private val title: TextView = view.findViewById(R.id.title)
        private val subtitle: TextView = view.findViewById(R.id.subtitle)
        private val avatar1: ImageView = view.findViewById(R.id.avatar1)
        private val avatar2: ImageView = view.findViewById(R.id.avatar2)
        private val avatar3: ImageView = view.findViewById(R.id.avatar3)
        val chevron: ImageView? = view.findViewById(R.id.chevron)
        val editBtn: ImageButton? = view.findViewById(R.id.edit_btn)
        val expandedContainer: LinearLayout = view.findViewById(R.id.expanded_container)

        fun bind(trip: Trip) {
            banner.setImageResource(trip.bannerRes)
            title.text = trip.title

            val count = trip.avatarRes.size
            subtitle.text = when {
                count == 0 -> "No tripmates"
                count == 1 -> "1 Tripmate is currently active and available for chat."
                else -> "$count Tripmates are currently active and available for chat."
            }

            avatar1.visibility = View.GONE
            avatar2.visibility = View.GONE
            avatar3.visibility = View.GONE

            for (i in 0 until minOf(3, trip.avatarRes.size)) {
                val res = trip.avatarRes[i]
                when (i) {
                    0 -> {
                        avatar1.setImageResource(res)
                        avatar1.visibility = View.VISIBLE
                    }
                    1 -> {
                        avatar2.setImageResource(res)
                        avatar2.visibility = View.VISIBLE
                    }
                    2 -> {
                        avatar3.setImageResource(res)
                        avatar3.visibility = View.VISIBLE
                    }
                }
            }

            chevron?.visibility = if (trip.avatarRes.isEmpty()) View.GONE else View.VISIBLE
        }

        fun setExpanded(expanded: Boolean) {
            if (expanded) {
                expandedContainer.visibility = View.VISIBLE
                chevron?.animate()?.rotation(180f)?.setDuration(200)?.start()
            } else {
                expandedContainer.visibility = View.GONE
                chevron?.animate()?.rotation(0f)?.setDuration(200)?.start()
            }
        }
    }
}
