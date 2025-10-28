package com.example.tripsync

import android.graphics.Color
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

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
//                R.id.personalDetailsFragment,
                R.id.emergencyFragment,
//                R.id.preferencesFragment,
//                R.id.verificationFragment
             -> {
                    progressLayout.visibility = View.VISIBLE
                    app_bar_layout.visibility = View.VISIBLE
                    updateProgress(destination.id)
                }

                else -> {
                    progressLayout.visibility = View.GONE
                }
            }
        }
    }

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

        // Identify which index is current
        val currentIndex = when (fragmentId) {
//            R.id.personalDetailsFragment -> 0
            R.id.emergencyFragment -> 1
//            R.id.preferencesFragment -> 2
//            R.id.verificationFragment -> 3
            else -> -1
        }

        // Reset all
        circles.forEach { circle ->
            circle.setBackgroundResource(R.drawable.incompleted_circle)
            circle.foreground = null
        }
        texts.forEach { it.setTextColor(Color.parseColor("#999999")) } // gray text
        views.forEach { it.setBackgroundColor(Color.parseColor("#CCCCCC")) }

        if (currentIndex >= 0) {
            // Set completed ones
            for (i in 0 until currentIndex) {
                circles[i].setBackgroundResource(R.drawable.complete_circle)
                circles[i].text = "✓"
                circles[i].setTextColor(Color.WHITE)

                if (i < views.size) {
                    views[i].setBackgroundColor(Color.parseColor("#00C896")) // green line
                }
            }


            circles[currentIndex].setBackgroundResource(R.drawable.complete_circle)
            circles[currentIndex].text = (currentIndex + 1).toString()
            circles[currentIndex].setTextColor(Color.WHITE)

            texts[currentIndex].setTextColor(Color.parseColor("#00C896"))
        }
    }
}
