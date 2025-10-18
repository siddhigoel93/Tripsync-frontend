package com.example.tripsync.onboarding

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.tripsync.R

class OnboardingFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: OnboardingAdapter
    private val handler = Handler(Looper.getMainLooper())
    private var slideRunnable: Runnable? = null
    private val slideInterval = 2000L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.onboarding_fragment, container, false)
        viewPager = view.findViewById(R.id.viewPager)

        adapter = OnboardingAdapter(requireActivity(), OnboardingSlides.slides)
        viewPager.adapter = adapter

        startAutoSlide()

        return view
    }

    private fun startAutoSlide() {
        slideRunnable = object : Runnable {
            override fun run() {
                if (viewPager.currentItem < adapter.itemCount - 1) {
                    viewPager.setCurrentItem(viewPager.currentItem + 1, true)
                    handler.postDelayed(this, slideInterval)
                } else {

                    handler.removeCallbacks(this)
                }
            }
        }
        handler.postDelayed(slideRunnable!!, slideInterval)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                handler.removeCallbacks(slideRunnable!!)
                if (position < adapter.itemCount - 1) {
                    handler.postDelayed(slideRunnable!!, slideInterval)
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        slideRunnable?.let { handler.removeCallbacks(it) }
    }
}
