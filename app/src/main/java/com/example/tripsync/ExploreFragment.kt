package com.example.tripsync

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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

        val completeProfileButton = view.findViewById<MaterialButton>(R.id.complete_profile_button)
        completeProfileButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_fragment_personal_details)
        }

        val profileButton = view.findViewById<ImageView>(R.id.menu_icon)
        profileButton.setOnClickListener {
            val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START)
            } else {
                Toast.makeText(requireContext(), "DrawerLayout not found in Activity", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appBarLayout = view.findViewById<AppBarLayout>(R.id.app_bar_layout)
        val customHeader = view.findViewById<ConstraintLayout>(R.id.header)

        val sharedPrefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isProfileCompleted = sharedPrefs.getBoolean("profile_completed", false)

        customHeader.visibility = if (isProfileCompleted ) View.GONE else View.VISIBLE

        val sp = requireContext().getSharedPreferences("user", Context.MODE_PRIVATE)
        val avatarUrl = sp.getString("userAvatarUrl", null)
        val profileImageView = view.findViewById<ImageView>(R.id.profile_avatar)
        if (!avatarUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(profileImageView)
        } else {
            profileImageView?.setImageResource(R.drawable.placeholder_image)
        }

        val elevationInPixels = 4f * resources.displayMetrics.density
        appBarLayout.elevation = elevationInPixels
        appBarLayout.translationZ = elevationInPixels
        appBarLayout.bringToFront()

        fetchWeather("Delhi") // Example location
    }

    private fun fetchWeather(location: String) {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getAuthService(requireContext())
                val weatherResponse = withContext(Dispatchers.IO) {
                    api.getWeather(location)
                }

                val weather = weatherResponse.data
                view?.findViewById<TextView>(R.id.temp_yesterday)?.text = "${weather.wind} km/h"
                view?.findViewById<TextView>(R.id.temp_today)?.text = "${weather.temperature}°"
                view?.findViewById<TextView>(R.id.temp_tomorrow)?.text = "${weather.chance_of_rain}%"

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
