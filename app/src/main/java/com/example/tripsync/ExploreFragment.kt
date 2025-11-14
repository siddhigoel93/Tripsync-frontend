package com.example.tripsync

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.AuthService
import com.example.tripsync.api.models.TrendingPlace
import com.example.tripsync.api.models.TravelStory
import com.example.tripsync.api.models.TravelStoryAdapter
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class ExploreFragment : Fragment() {

    private val args: ExploreFragmentArgs by navArgs()
    private lateinit var storiesAdapter: TravelStoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)

        view.findViewById<MaterialButton>(R.id.complete_profile_button)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_fragment_personal_details)
        }

        view.findViewById<ImageView>(R.id.menu_icon).setOnClickListener {
            val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.END)
            } else {
                Toast.makeText(requireContext(), "DrawerLayout not found", Toast.LENGTH_SHORT).show()
            }
        }

        val chatButton = view.findViewById<ImageButton>(R.id.toolbar_btn_chat)
        chatButton.setOnClickListener {
            findNavController().navigate(R.id.chatFragment)
        }

        view.findViewById<CardView>(R.id.card_ai_planner).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_AIItinearyPlannerFragment)
        }

        view.findViewById<CardView>(R.id.card_add_tripmates).setOnClickListener {
            try {
                findNavController().navigate(R.id.action_homeFragment_to_connectionsTripmatesFragment)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Unable to open Connections", Toast.LENGTH_SHORT).show()
            }
        }

        val storiesRv = view.findViewById<RecyclerView>(R.id.travel_stories_recycler_view)
        storiesRv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        storiesRv.overScrollMode = View.OVER_SCROLL_NEVER

        val placeholderList = listOf(
            TravelStory(cityName = "Loading...", imageUrl = "https://via.placeholder.com/300"),
            TravelStory(cityName = "Loading...", imageUrl = "https://via.placeholder.com/300"),
            TravelStory(cityName = "Loading...", imageUrl = "https://via.placeholder.com/300"),
            TravelStory(cityName = "Loading...", imageUrl = "https://via.placeholder.com/300")
        )

        storiesAdapter = TravelStoryAdapter(placeholderList) { story ->
            Toast.makeText(requireContext(), "Clicked ${story.cityName}", Toast.LENGTH_SHORT).show()
        }

        storiesRv.adapter = storiesAdapter

        fetchTrendingPlacesAndPopulate(requireContext(), storiesRv)

        return view
    }

    private fun fetchTrendingPlacesAndPopulate(context: Context, recyclerView: RecyclerView) {
        lifecycleScope.launch {
            try {
                val service = ApiClient.createService(context, AuthService::class.java)
                val places: List<TrendingPlace> = service.getTrendingPlaces()

                if (places.isNotEmpty()) {
                    val stories = places.map { place ->
                        TravelStory(id = place.id, cityName = place.name, imageUrl = place.main)
                    }

                    val adapter = TravelStoryAdapter(stories) { story ->
                        val bundle = Bundle().apply {
                            putInt("place_id", story.id ?: -1)
                        }
                        try {
                            findNavController().navigate(R.id.travelStoryDetailFragment, bundle)
                        } catch (_: Exception) {
                            try {
                                val frag = com.example.tripsync.TravelStoryDetailFragment().apply {
                                    arguments = Bundle().apply {
                                        putInt("place_id", story.id ?: -1)
                                    }
                                }
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.nav_host_fragment, frag)
                                    .addToBackStack(null)
                                    .commitAllowingStateLoss()
                            } catch (e: Exception) {
                                Toast.makeText(requireContext(), "Open detail for ${story.cityName}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    recyclerView.adapter = adapter
                    storiesAdapter = adapter
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Failed to fetch trending places: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appBarLayout = view.findViewById<AppBarLayout>(R.id.app_bar_layout)
        val customHeader = view.findViewById<ConstraintLayout>(R.id.header)

        val sharedPrefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isProfileCompleted = sharedPrefs.getBoolean("profile_completed", false)
        customHeader.visibility = if (isProfileCompleted) View.GONE else View.VISIBLE

        if (args.showHeader) {
            val avatarUrl = sharedPrefs.getString("userAvatarUrl", null)
            val profileImageView = view.findViewById<ImageView>(R.id.menu_icon)
            if (!avatarUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .circleCrop()
                    .into(profileImageView)
            } else {
                profileImageView.setImageResource(R.drawable.placeholder_image)
            }
            customHeader.visibility = View.GONE
        }

        val elevationInPixels = 4f * resources.displayMetrics.density
        appBarLayout.elevation = elevationInPixels
        appBarLayout.translationZ = elevationInPixels
        appBarLayout.bringToFront()
    }

    fun updateExploreProfileImage(url: String?) {
        val exploreProfileImage = requireView().findViewById<ImageView>(R.id.menu_icon)
        if (!url.isNullOrEmpty()) {
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .circleCrop()
                .into(exploreProfileImage)
        } else {
            exploreProfileImage.setImageResource(R.drawable.profile)
        }
    }
}
