package com.example.tripsync.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.tripsync.R

class OnboardingFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: OnboardingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPrefs = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
        val accessToken = sharedPrefs.getString("access_token", null)

        if (accessToken != null) {
            findNavController().navigate(R.id.action_onboardingFragment_to_homeFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.onboarding_fragment, container, false)
        viewPager = view.findViewById(R.id.viewPager)

        adapter = OnboardingAdapter(requireActivity(), OnboardingSlides.slides)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 1
        viewPager.clipToPadding = true
        viewPager.clipChildren = true

        viewPager.isUserInputEnabled = false

        viewPager.setPageTransformer { page, position ->
            page.alpha = 1 - kotlin.math.abs(position) * 0.5f
            page.scaleY = 0.85f + (1 - kotlin.math.abs(position)) * 0.15f
        }

        return view
    }
}