package com.example.tripsync

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.appbar.AppBarLayout
import android.view.inputmethod.InputMethodManager

class MainActivity : AppCompatActivity() {
    lateinit var progressLayout: View
    lateinit var app_bar_layout: AppBarLayout
    lateinit var gradient: GradientDrawable
    lateinit var bottom_nav : AppBarLayout

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
        bottom_nav = findViewById(R.id.bottom_app_bar)


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.fragment_personal_details,
                R.id.emergencyFragment,
                R.id.preferencesFragment,
                R.id.contactVerifyFragment
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

            when (destination.id) {
                R.id.homeFragment,
                R.id.communityFragment,
                R.id.createPostFragment,
                    -> {
                    bottom_nav.visibility = View.VISIBLE
                }

                else -> {
                    bottom_nav.visibility = View.GONE
                }
            }
        }
    }

        override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
            if (ev.action == MotionEvent.ACTION_DOWN) {
                val v = currentFocus
                if (v is EditText) {
                    val outRect = Rect()
                    v.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                        v.clearFocus()
                        val imm =
                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(v.windowToken, 0)
                    }
                }
            }
            return super.dispatchTouchEvent(ev)
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

            views.forEach {
                it.setBackgroundResource(R.drawable.rounded_progress_line)
                it.scaleX = 1f
            }


            val currentIndex = when (fragmentId) {
                R.id.fragment_personal_details -> 0
                R.id.emergencyFragment -> 1
                R.id.preferencesFragment -> 2
                R.id.contactVerifyFragment -> 3
                else -> -1
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
                        circle.setBackgroundResource(R.drawable.complete_circle_tick)
                        circle.text = ""
                    } else if (index == currentIndex) {
                        circle.setBackgroundResource(R.drawable.complete_circle)
                        circle.text = (index + 1).toString()
                        circle.setTextColor(Color.WHITE)
                        texts[index].setTextColor(Color.parseColor("#00C896"))

                    }
                }
            }
            if (currentIndex > 0) {
                for (i in 0 until currentIndex) {
                    views[i].post {
                        val gradientDrawable = GradientDrawable(
                            GradientDrawable.Orientation.LEFT_RIGHT,
                            intArrayOf(
                                Color.parseColor("#00B487"),
                                Color.parseColor("#00AEEF")
                            )
                        )
                        gradientDrawable.cornerRadius = 3f * resources.displayMetrics.density
                        views[i].background = gradientDrawable
                    }
                }
            }

        }



}
