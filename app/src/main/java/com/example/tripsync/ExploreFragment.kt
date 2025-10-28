package com.example.tripsync

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton

class ExploreFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)
//        val button = view.findViewById<MaterialButton>(R.id.logoutButton)
//
//        button.setOnClickListener {
//            logoutUser()
//        }

        return view
    }

    private fun logoutUser() {
        // Clear stored tokens
        val sharedPrefs = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()

        findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
    }
}