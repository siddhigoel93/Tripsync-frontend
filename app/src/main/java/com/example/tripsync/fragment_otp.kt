package com.example.tripsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class fragment_otp : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_otp, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvBackToLogin = view.findViewById<TextView>(R.id.tvBackToLogin)
        tvBackToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_fragment_otp_to_fragment_signup)
        }
    }
}
