package com.example.tripsync

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.models.WeatherResponse
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExploreFragment : Fragment() {

    private val args: ExploreFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)


        // Complete Profile Button
        view.findViewById<MaterialButton>(R.id.complete_profile_button).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_fragment_personal_details)
        }

        // Drawer Menu Button
        view.findViewById<ImageView>(R.id.menu_icon).setOnClickListener {
            val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.END)
            } else {
                Toast.makeText(requireContext(), "DrawerLayout not found in Activity", Toast.LENGTH_SHORT).show()
            }
        }

        val chatButton = view.findViewById<ImageButton>(R.id.toolbar_btn_chat)

        // 2. Set the Click Listener and Navigate
        chatButton.setOnClickListener {
            // 3. Navigate to ChatFragment using its ID from your nav_graph.xml
            findNavController().navigate(R.id.chatFragment)
            // OR use Safe Args if you have created an action for it:
            // findNavController().navigate(ExploreFragmentDirections.actionExploreFragmentToChatFragment())
        }

        // AI Planner Card
        view.findViewById<CardView>(R.id.card_ai_planner).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_AIItinearyPlannerFragment)
        }

        // Bottom Navigation Listener
//        view.findViewById<BottomNavigationView>(R.id.bottom_navigation_view).setOnItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.nav_search -> {
//                    val navController = findNavController()
//                    if (navController.currentDestination?.id != R.id.chatFragment) {
//                        navController.navigate(R.id.chatFragment)
//                    }
//                    true
//                }
//                else -> false
//            }
//        }

        return view
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
        } else {
            fetchWeather("Delhi")
        }

        // AppBar elevation
        val elevationInPixels = 4f * resources.displayMetrics.density
        appBarLayout.elevation = elevationInPixels
        appBarLayout.translationZ = elevationInPixels
        appBarLayout.bringToFront()

        (activity as? MainActivity)?.addProfileObserver { newUrl ->
            newUrl?.let { updateExploreProfileImage(it) }}
    }

    private fun fetchWeather(location: String) {
        lifecycleScope.launch {
                try {
                    if (!isAdded) return@launch

                    val api = ApiClient.getAuthService(requireContext())
                    val weatherResponse: WeatherResponse = withContext(Dispatchers.IO) {
                        api.getWeather(location)

                    }

                    if (!isAdded || view == null) return@launch
                    val weather = weatherResponse.data
                    view?.findViewById<TextView>(R.id.temp_yesterday)?.text = "${weather.wind} km/h"
                    view?.findViewById<TextView>(R.id.temp_today)?.text = "${weather.temperature}°"
                    view?.findViewById<TextView>(R.id.temp_tomorrow)?.text =
                        "${weather.chance_of_rain}%"

                } catch (e: Exception) {
                    if (isAdded && context != null) {
                        Toast.makeText(
                            requireContext(),
                            "Error: ${e.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

    }

    fun updateExploreProfileImage(url: String?) {
        val exploreProfileImage = requireView().findViewById<ImageView>(R.id.profileImageView)
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
