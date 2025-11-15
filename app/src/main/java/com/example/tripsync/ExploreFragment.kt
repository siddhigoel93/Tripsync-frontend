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
import com.example.tripsync.api.SessionManager
import com.example.tripsync.api.models.TrendingPlace
import com.example.tripsync.api.models.TravelStory
import com.example.tripsync.api.models.TravelStoryAdapter
import com.example.tripsync.api.models.WeatherResponse
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlin.getValue

class ExploreFragment : Fragment() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var city: TextView
    private lateinit var sos_btn: MaterialButton
    private val args: ExploreFragmentArgs by navArgs()
    private lateinit var storiesAdapter: TravelStoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)
        city = view.findViewById(R.id.weather_subtitle)
        sos_btn = view.findViewById(R.id.sos_btn)

        // Setup SOS button click listener
        sos_btn.setOnClickListener {
            navigateToEmergencySos()
        }

        view.findViewById<CardView>(R.id.card_emergency_sos).setOnClickListener {
            navigateToEmergencySos()
        }

        view.findViewById<CardView>(R.id.card_create_trip).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_communityFragment)
        }

        // Complete profile button
        view.findViewById<MaterialButton>(R.id.complete_profile_button).setOnClickListener {
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
            if (SessionManager.isProfileCompleted(requireContext())) {
                findNavController().navigate(R.id.action_homeFragment_to_AIItinearyPlannerFragment)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please complete your profile to create itineraries",
                    Toast.LENGTH_LONG
                ).show()
            }
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

    private fun navigateToEmergencySos() {
        if (SessionManager.isProfileCompleted(requireContext())) {
            findNavController().navigate(R.id.action_homeFragment_to_emergencySosFragment)
        } else {
            Toast.makeText(
                requireContext(),
                "Please complete your profile to access Emergency SOS",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun fetchTrendingPlacesAndPopulate(context: Context, recyclerView: RecyclerView) {
        lifecycleScope.launch {
            try {
                val service = ApiClient.createService(context, AuthService::class.java)
                val places: List<TrendingPlace> = service.getTrendingPlaces()

                if (places.isNotEmpty()) {
                    val selectedPlaces = places.shuffled().take(4)

                    val stories = selectedPlaces.map { place ->
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
                                val frag = TravelStoryDetailFragment().apply {
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
        val chatbot = view.findViewById<ImageButton>(R.id.toolbar_btn_chat)

        view.findViewById<MaterialButton>(R.id.sos_btn).bringToFront()


        // Use SessionManager instead of direct SharedPreferences access
        val isProfileCompleted = SessionManager.isProfileCompleted(requireContext())
        val savedUrl = SessionManager.getAvatarUrl(requireContext())
        updateExploreProfileImage(savedUrl)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        getUserLocation()

        customHeader.visibility = if (isProfileCompleted) View.GONE else View.VISIBLE

        if (args.showHeader) {
            val avatarUrl = SessionManager.getAvatarUrl(requireContext())
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
        customHeader.setOnTouchListener { _, _ -> false }


        chatbot.setOnClickListener {
            findNavController().navigate(R.id.AIchatFragment)
        }

        (activity as? MainActivity)?.addProfileObserver { newUrl ->
            if (isAdded && view != null) {
                newUrl?.let { updateExploreProfileImage(it) }
            }
        }
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
        if (!isAdded || view == null) return

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
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val lat = location.latitude
                        val lon = location.longitude
                        Log.d("Location", "Lat: $lat, Lon: $lon")

                        val cityName = getCityNameFromLocation(lat, lon)
                        Log.d("Location", "City: $cityName")
                        fetchWeather(cityName ?: "Delhi")
                        city.text = cityName
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