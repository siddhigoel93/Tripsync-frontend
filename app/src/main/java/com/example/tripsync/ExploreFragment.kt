package com.example.tripsync

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomnavigation.BottomNavigationView   // ADDED

class ExploreFragment : Fragment() {

    private val args: ExploreFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)

        val complete_profile_button = view.findViewById<MaterialButton>(R.id.complete_profile_button)

        complete_profile_button.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_fragment_personal_details)
        }

        val cardAIPlanner = view.findViewById<CardView>(R.id.card_ai_planner)
        cardAIPlanner.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_AIItinearyPlannerFragment)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appBarLayout = view.findViewById<AppBarLayout>(R.id.app_bar_layout)
        val customHeader = view.findViewById<ConstraintLayout>(R.id.header)

        val sharedPrefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isProfileCompleted = sharedPrefs.getBoolean("profile_completed", false)

        if(isProfileCompleted){
            customHeader.visibility= View.GONE
        }
        if (args.showHeader) {

            customHeader.visibility = View.GONE
            val elevationInPixels = 4f * resources.displayMetrics.density
            appBarLayout.elevation = elevationInPixels
            appBarLayout.translationZ = elevationInPixels
            appBarLayout.bringToFront()

        } else {

            customHeader.visibility = View.VISIBLE
            val elevationInPixels = 4f * resources.displayMetrics.density
            appBarLayout.elevation = elevationInPixels
            appBarLayout.translationZ = elevationInPixels
            appBarLayout.bringToFront()
        }
        val bottomNav = view.findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_search -> {
                    val navController = findNavController()
                    val current = navController.currentDestination?.id
                    if (current != R.id.chatFragment) {
                        navController.navigate(R.id.chatFragment)
                    }
                    true
                }
                else -> false
            }
        }
        // END ADDED
    }
}
