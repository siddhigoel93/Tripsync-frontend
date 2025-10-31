package com.example.tripsync

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.appbar.AppBarLayout

class MainActivity : AppCompatActivity() {
    lateinit var progressLayout: View
    lateinit var app_bar_layout: AppBarLayout
    lateinit var gradient: GradientDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        progressLayout = findViewById(R.id.profileProgressLayout)
        app_bar_layout = findViewById(R.id.app_bar_layout)

        gradient = GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT , intArrayOf(Color.parseColor("#00B487"),
            Color.parseColor("#00AEEF")))

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.fragment_personal_details,
                R.id.emergencyFragment,
                R.id.preferencesFragment,
//                R.id.verificationFragment
             -> {
                    progressLayout.visibility = View.VISIBLE
                    app_bar_layout.visibility = View.VISIBLE
                    updateProgress(destination.id)
                }

                else -> {
                    progressLayout.visibility = View.GONE
                    app_bar_layout.visibility = View.GONE
                }
            }
        }
    }

//    private fun updateProgress(fragmentId: Int) {
//        val circles = listOf(
//            findViewById<TextView>(R.id.circle1),
//            findViewById<TextView>(R.id.circle2),
//            findViewById<TextView>(R.id.circle3),
//            findViewById<TextView>(R.id.circle4)
//        )
//        val texts = listOf(
//            findViewById<TextView>(R.id.text1),
//            findViewById<TextView>(R.id.text2),
//            findViewById<TextView>(R.id.text3),
//            findViewById<TextView>(R.id.text4)
//        )
//        val views = listOf(
//            findViewById<View>(R.id.view1),
//            findViewById<View>(R.id.view2),
//            findViewById<View>(R.id.view3),
//        )
//
//        val currentIndex = when (fragmentId) {
//      R.id.fragment_personal_details -> 0
//            R.id.emergencyFragment -> 1
//            R.id.preferencesFragment -> 2
////        R.id.verificationFragment -> 3
//            else -> -1
//        }
//
//        circles.forEachIndexed { index , circle ->
//            circle.setBackgroundResource(R.drawable.incompleted_circle)
//            texts[index].setTextColor(Color.parseColor("#000000"))
//            circle.text = (index + 1).toString()
//            circle.scaleX = 1f
//            circle.scaleY = 1f

//        texts.forEach { it.setTextColor(Color.parseColor("#000000")) }
//        views.forEach { it.setBackgroundColor(Color.parseColor("#CCCCCC")) }

//        if (currentIndex >= 0) {
//            // Completed ones
//            for (i in 0 until currentIndex) {
//                val circle = circles[i]
//                circle.setBackgroundResource(R.drawable.complete_circle)
//                circle.text = "✓"
//                circle.setTextColor(Color.WHITE)
//
//                circle.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150)
//                    .withEndAction { circle.animate().scaleX(1f).scaleY(1f).setDuration(1000) }
//
//                if (i < views.size) {
//                    val view = views[i]
//                    view.animate().alpha(0f).setDuration(0).withEndAction {
//                        view.background = gradient
//                        view.animate().alpha(1f).setDuration(200).start()
//                    }
//                }
//            }
//        if (index < currentIndex) {
//            circle.setBackgroundResource(R.drawable.complete_circle)
//            circle.text = "✓"
//            circle.setTextColor(Color.WHITE)
//        } else if (index == currentIndex) {
//
//            circle.setBackgroundResource(R.drawable.complete_circle)
//            circle.setTextColor(Color.WHITE)
//            texts[index].setTextColor(Color.parseColor("#00C896"))
//
//
//        } else {
//            circle.setTextColor(Color.parseColor("#999999"))
//
//        }
//            views.forEach { it.setBackgroundColor(Color.parseColor("#CCCCCC")) }

//            val currentCircle = circles[currentIndex]
//            currentCircle.setBackgroundResource(R.drawable.complete_circle)
//            currentCircle.text = (currentIndex + 1).toString()
//            currentCircle.setTextColor(Color.WHITE)
//
//            texts[currentIndex].setTextColor(Color.parseColor("#00C896"))
//
//            currentCircle.animate()
//                .scaleX(1.15f)
//                .scaleY(1.15f)
//                .setDuration(200)
//                .withEndAction {
//                    currentCircle.animate().scaleX(1f).scaleY(1f).setDuration(150)
//                }

    private fun updateProgress(fragmentId: Int) {
        val circles = listOf(
            findViewById<TextView>(R.id.circle1),
            findViewById<TextView>(R.id.circle2),
            findViewById<TextView>(R.id.circle3),
            findViewById<TextView>(R.id.circle4)
        )
        val texts = listOf(
            findViewById<TextView>(R.id.text1),
            findViewById<TextView>(R.id.text2),
            findViewById<TextView>(R.id.text3),
            findViewById<TextView>(R.id.text4)
        )
        val views = listOf(
            findViewById<View>(R.id.view1),
            findViewById<View>(R.id.view2),
            findViewById<View>(R.id.view3),
        )

        val currentIndex = when (fragmentId) {
            R.id.fragment_personal_details -> 0
            R.id.emergencyFragment -> 1
            R.id.preferencesFragment -> 2
//        R.id.verificationFragment -> 3
            else -> -1
        }

        views.forEach {
            it.setBackgroundColor(Color.parseColor("#CCCCCC"))
        }

        circles.forEachIndexed { index, circle ->
            circle.setBackgroundResource(R.drawable.incompleted_circle)
            circle.setTextColor(Color.parseColor("#999999"))
            texts[index].setTextColor(Color.parseColor("#000000"))
            circle.text = (index + 1).toString()
            circle.scaleX = 1f
            circle.scaleY = 1f

            if (currentIndex >= 0) {
                if (index < currentIndex) {
                    circle.setBackgroundResource(R.drawable.complete_circle)
                    circle.text = "✓"
                    circle.setTextColor(Color.WHITE)
                } else if (index == currentIndex) {
                    circle.setBackgroundResource(R.drawable.complete_circle)
                    circle.text = (index + 1).toString()
                    circle.setTextColor(Color.WHITE)
                    texts[index].setTextColor(Color.parseColor("#00C896"))

                    circle.animate().scaleX(1.15f).scaleY(1.15f).setDuration(200).start()
                }
            }
        }

        if (currentIndex > 0) {
            for (i in 0 until currentIndex) {
                if (i < views.size) {
                    val view = views[i]
                    view.background = gradient
                    view.animate().alpha(1f).setDuration(200).start()
                }
            }
        }
    }



}
