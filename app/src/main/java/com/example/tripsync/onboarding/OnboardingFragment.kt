package com.example.tripsync.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.tripsync.LoginFragment
import com.example.tripsync.R

class OnboardingFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: OnboardingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.onboarding_fragment, container, false)
        viewPager = view.findViewById(R.id.viewPager)

        adapter = OnboardingAdapter(requireActivity(), OnboardingSlides.slides)
        viewPager.adapter = adapter

        return view
    }

}
