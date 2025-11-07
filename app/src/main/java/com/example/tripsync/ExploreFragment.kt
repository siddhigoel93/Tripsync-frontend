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
import android.widget.ImageView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.GravityCompat // ⬅️ NEW IMPORT for opening the drawer

class ExploreFragment : Fragment() {

    private val args: ExploreFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)

        val complete_profile_button = view.findViewById<MaterialButton>(R.id.complete_profile_button)

        complete_profile_button.setOnClickListener {
            // NOTE: Assuming action_homeFragment_to_fragment_personal_details points to the correct destination
            findNavController().navigate(R.id.action_homeFragment_to_fragment_personal_details)
        }

        // ❌ Removed: val drawerLayout = view.findViewById<DrawerLayout>(R.id.drawer_layout)
        // ❌ Removed: val navView = view.findViewById<NavigationView>(R.id.nav_view)

        // 1. Find the profile icon in the fragment's toolbar
        val profileButton = view.findViewById<ImageView>(R.id.menu_icon)

        // 2. Set the click listener to open the drawer
        profileButton.setOnClickListener {
            // ⬅️ FIX: Access the DrawerLayout from the hosting Activity's hierarchy
            val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)

            // ⬅️ FIX: Use GravityCompat.START to open the drawer from the left
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START)
            } else {
                Toast.makeText(requireContext(), "DrawerLayout not found in Activity", Toast.LENGTH_SHORT).show()
            }
        }

        // ... (Commented-out code remains commented out) ...
        return view
    }
//
//    private fun logoutUser() {
//        // Clear stored tokens
//        val sharedPrefs = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
//        sharedPrefs.edit().clear().apply()
//
//        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
//
//        findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
//    }

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
    }
}