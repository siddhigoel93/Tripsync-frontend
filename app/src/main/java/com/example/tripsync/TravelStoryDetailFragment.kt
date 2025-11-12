package com.example.tripsync

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.AuthService
import com.example.tripsync.api.models.TrendingPlace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TravelStoryDetailFragment : Fragment() {

    private var placeId: Int = -1
    private lateinit var viewPager: ViewPager2
    private lateinit var backButton: ImageButton
    private lateinit var progressContainer: LinearLayout
    private val inactiveColor = 0x44FFFFFF
    private val activeColor = 0xFF63E6C2.toInt()

    companion object {
        private const val ARG_PLACE_ID = "place_id"
        fun newInstance(placeId: Int): TravelStoryDetailFragment {
            val f = TravelStoryDetailFragment()
            val b = Bundle()
            b.putInt(ARG_PLACE_ID, placeId)
            f.arguments = b
            return f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        placeId = arguments?.getInt(ARG_PLACE_ID) ?: -1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_travel_story_detail, container, false)
        viewPager = v.findViewById(R.id.slide_view_pager)
        backButton = v.findViewById(R.id.back_button)
        progressContainer = v.findViewById(R.id.progress_container)
        backButton.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (placeId != -1) fetchPlaceAndPopulate(placeId)
    }

    private fun fetchPlaceAndPopulate(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = ApiClient.createService(requireContext(), AuthService::class.java)
                val places = api.getTrendingPlaces()
                val place = places.firstOrNull { it.id == id } ?: places.firstOrNull()
                withContext(Dispatchers.Main) {
                    place?.let { populatePager(it) } ?: run {
                        android.widget.Toast.makeText(requireContext(), "Place not found", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(requireContext(), "Error loading place: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun populatePager(place: TrendingPlace) {
        val adapter = FunFactPagerAdapter(place)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 1
        setupProgressIndicators(adapter.itemCount)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateProgress(position)
            }
        })
        if (adapter.itemCount > 0) updateProgress(0)
    }

    private fun setupProgressIndicators(count: Int) {
        progressContainer.removeAllViews()
        if (count <= 0) return
        val gapDp = 6
        val gapPx = dpToPx(gapDp)
        for (i in 0 until count) {
            val v = View(requireContext())
            val bg = GradientDrawable().apply {
                cornerRadius = dpToPx(6).toFloat()
                setColor(inactiveColor.toInt())
            }
            v.background = bg
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            params.marginStart = if (i == 0) 0 else gapPx / 2
            params.marginEnd = if (i == count - 1) 0 else gapPx / 2
            v.layoutParams = params
            progressContainer.addView(v)
        }
    }

    private fun updateProgress(activeIndex: Int) {
        val childCount = progressContainer.childCount
        for (i in 0 until childCount) {
            val child = progressContainer.getChildAt(i)
            val drawable = child.background as? GradientDrawable
            if (i == activeIndex) {
                drawable?.setColor(activeColor)
                child.updateLayoutParams<LinearLayout.LayoutParams> {
                    height = dpToPx(12)
                }
            } else {
                drawable?.setColor(inactiveColor.toInt())
                child.updateLayoutParams<LinearLayout.LayoutParams> {
                    height = dpToPx(8)
                }
            }
            child.invalidate()
        }
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).toInt()
    }
}
