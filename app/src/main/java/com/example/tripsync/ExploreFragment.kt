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
//        val button = view.findViewById<MaterialButton>(R.id.logoutButton)
//
//        button.setOnClickListener {
//            logoutUser()
//        }

//        val emergency = view.findViewById<CardView>(R.id.card_sos_emergency)
//        emergency.setOnClickListener { findNavController().navigate(R.id.action_homeFragment_to_emergencyFragment) }
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
//
//    if (appBarLayout != null) {
//
//        val elevationInPixels = 4f * resources.displayMetrics.density
//
//        ViewCompat.setElevation(appBarLayout, elevationInPixels)
//        appBarLayout.translationZ = elevationInPixels
//
//        appBarLayout.bringToFront()
//    }

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