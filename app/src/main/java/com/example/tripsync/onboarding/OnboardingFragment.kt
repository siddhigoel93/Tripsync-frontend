package com.example.tripsync.onboarding

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    private val handler = Handler(Looper.getMainLooper())
    private var slideRunnable: Runnable? = null
    private val slideInterval = 1800L
    private var isUserInteracting = false // Add this flag

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

        adapter = OnboardingAdapter(requireActivity(), OnboardingSlides.slides) {
            stopAutoSlide()
        }
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 1
        viewPager.clipToPadding = true
        viewPager.clipChildren = true

        // Smoother page transformer
        viewPager.setPageTransformer { page, position ->
            page.alpha = 1 - kotlin.math.abs(position) * 0.5f
            page.scaleY = 0.85f + (1 - kotlin.math.abs(position)) * 0.15f
        }

        startAutoSlide()

        return view
    }

    private fun startAutoSlide() {
        slideRunnable = object : Runnable {
            override fun run() {
                if (!isUserInteracting && viewPager.currentItem < adapter.itemCount - 1) {
                    viewPager.setCurrentItem(viewPager.currentItem + 1, true)
                    handler.postDelayed(this, slideInterval)
                }
            }
        }
        handler.postDelayed(slideRunnable!!, slideInterval)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (!isUserInteracting) {
                    handler.removeCallbacks(slideRunnable!!)
                    if (position < adapter.itemCount - 1) {
                        handler.postDelayed(slideRunnable!!, slideInterval)
                    }
                }
            }
        })
    }

    fun stopAutoSlide() {
        isUserInteracting = true
        slideRunnable?.let { handler.removeCallbacks(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        slideRunnable?.let { handler.removeCallbacks(it) }
    }
}