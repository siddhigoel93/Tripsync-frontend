package com.example.tripsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.tripsync.R
import com.example.tripsync.api.models.FunFacts
import com.example.tripsync.api.models.Place
import kotlin.getValue

class StoryDetailFragment : Fragment() {
    private var place: Place? = null


    private val args: StoryDetailFragmentArgs by navArgs() // receives FunFacts object

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        place = arguments?.getSerializable("place") as? Place
        // Use the story item layout
        return inflater.inflate(R.layout.item_travel_story, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val storyImage = view.findViewById<ImageView>(R.id.story_image)
        val storyTitle = view.findViewById<TextView>(R.id.story_title)
        val storyDesc = view.findViewById<TextView>(R.id.story_desc)

        // Load fun fact image
        place?.let { p ->
            storyTitle.text = p.name
            storyDesc.text = if (p.fun_facts.isNotEmpty()) p.fun_facts.random().desc else ""
            Glide.with(this)
                .load(p.main)
                .placeholder(R.drawable.placeholder_image)
                .into(storyImage)
        }
    }
}
