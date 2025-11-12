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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class ExploreFragment : Fragment() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var city : TextView
    lateinit var sos_btn : MaterialButton
    private val args: ExploreFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)
        city = view.findViewById(R.id.weather_subtitle)
        sos_btn = view.findViewById(R.id.sos_btn)


        sos_btn.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_emergencySosFragment)
        }

        view.findViewById<MaterialButton>(R.id.complete_profile_button).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_fragment_personal_details)
        }

        view.findViewById<ImageView>(R.id.menu_icon).setOnClickListener {
            val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.END)
            } else {
                Toast.makeText(requireContext(), "DrawerLayout not found in Activity", Toast.LENGTH_SHORT).show()
            }
        }

        val chatButton = view.findViewById<ImageButton>(R.id.toolbar_btn_chat)

        chatButton.setOnClickListener {
            findNavController().navigate(R.id.chatFragment)
        }

        view.findViewById<CardView>(R.id.card_ai_planner).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_AIItinearyPlannerFragment)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appBarLayout = view.findViewById<AppBarLayout>(R.id.app_bar_layout)
        val customHeader = view.findViewById<ConstraintLayout>(R.id.header)
        val chatbot = view.findViewById<ImageButton>(R.id.toolbar_btn_chat)

        val sharedPrefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isProfileCompleted = sharedPrefs.getBoolean("profile_completed", false)
        val savedUrl = sharedPrefs.getString("userAvatarUrl", null)
        updateExploreProfileImage(savedUrl)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        getUserLocation()

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

        chatbot.bringToFront()

        chatbot.setOnClickListener {
            findNavController().navigate(R.id.AIchatFragment)
        }

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
                    view?.findViewById<TextView>(R.id.temp_today)?.text = "${weather.chance_of_rain}%"
                    view?.findViewById<TextView>(R.id.temp_tomorrow)?.text = "${weather.temperature}°"


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
    private fun getUserLocation() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission granted, get location
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val lat = location.latitude
                        val lon = location.longitude
                        Log.d("Location", "Lat: $lat, Lon: $lon")

                        val cityName = getCityNameFromLocation(lat, lon)
                        Log.d("Location", "City: $cityName")
                        fetchWeather(cityName ?: "Delhi")
                        city.text=cityName
                    } else {
                        fetchWeather("Delhi")
                    }
                }
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getUserLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
                fetchWeather("Delhi")
            }
        }
    private fun getCityNameFromLocation(lat: Double, lon: Double): String? {
        return try {
            val geocoder = android.location.Geocoder(requireContext(), java.util.Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            addresses?.firstOrNull()?.locality
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }



}
