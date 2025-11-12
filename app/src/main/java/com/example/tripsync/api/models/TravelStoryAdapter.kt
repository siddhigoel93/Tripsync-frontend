package com.example.tripsync.api.models

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tripsync.R

class TravelStoryAdapter(
    private val stories: List<TravelStory>,
    private val onStoryClick: (TravelStory) -> Unit
) : RecyclerView.Adapter<TravelStoryAdapter.StoryViewHolder>() {

    inner class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val storyImage: ImageView = itemView.findViewById(R.id.story_image)
        private val cityName: TextView = itemView.findViewById(R.id.story_city_name)

        fun bind(story: TravelStory) {
            Log.d("TravelStoryAdapter", "Binding story: ${story.cityName} imageUrl=${story.imageUrl}")

            Glide.with(itemView.context)
                .load(story.imageUrl)
                .centerCrop()
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(storyImage)

            cityName.text = story.cityName

            itemView.setOnClickListener { onStoryClick(story) }
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
