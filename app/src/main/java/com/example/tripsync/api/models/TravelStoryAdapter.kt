package com.example.tripsync.api.models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.*
import com.bumptech.glide.Glide
import com.example.tripsync.R

class TravelStoryAdapter(
private val stories: List<Place>,
private val onStoryClick: (Place) -> Unit
) : RecyclerView.Adapter<TravelStoryAdapter.StoryViewHolder>() {

    inner class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.story_image)
        private val title: TextView = itemView.findViewById(R.id.story_title)
        private val desc: TextView = itemView.findViewById(R.id.story_desc)

        fun bind(story: Place) {
            // Load main image
            Glide.with(itemView.context)
                .load(story.main)
                .placeholder(R.drawable.placeholder_image)
                .into(image)

            // Title
            title.text = story.name

            // Random fun fact description
            desc.text = if (story.fun_facts.isNotEmpty()) story.fun_facts.random().desc else ""

            itemView.setOnClickListener {
                onStoryClick(story)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_travel_story, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(stories[position])
    }

    override fun getItemCount(): Int = stories.size
}
