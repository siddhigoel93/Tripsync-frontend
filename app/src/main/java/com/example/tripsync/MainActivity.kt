package com.example.tripsync

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.NavHostFragment
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {
    lateinit var progressLayout: View
    lateinit var app_bar_layout: AppBarLayout
    lateinit var bottom_app_bar_wrapper: BottomAppBar
    lateinit var bottom_nav_view: BottomNavigationView
    lateinit var fab_store: FloatingActionButton

    // All members below this line are properly initialized/used in the fix.
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }



        progressLayout = findViewById(R.id.profileProgressLayout)
        app_bar_layout = findViewById(R.id.app_bar_layout)
        bottom_app_bar_wrapper = findViewById(R.id.bottom_app_bar)
        bottom_nav_view = findViewById(R.id.bottom_navigation_view)
        fab_store = findViewById(R.id.fab_store)

        drawerLayout = findViewById(R.id.drawer_layout)


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        this.navController = navHostFragment.navController

        setupDrawerMenuListeners()
        setupDrawerBackButtonHandling()

        bottom_nav_view.setupWithNavController(navController)

//        fab_store.setOnClickListener { /* ... */ }

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
                R.id.nav_community,
                R.id.createPostFragment,
                R.id.searchFragment,
                    -> {
                    bottom_app_bar_wrapper.visibility = View.VISIBLE
                    bottom_nav_view.visibility = View.VISIBLE
                    fab_store.visibility = View.VISIBLE
                }

                else -> {
                    bottom_app_bar_wrapper.visibility = View.GONE
                    fab_store.visibility = View.GONE
                }
            }
        }
    }

    private fun setupDrawerMenuListeners() {
        val drawerMenuView = findViewById<View>(R.id.drawer_menu_include)

        val profileName = drawerMenuView.findViewById<TextView>(R.id.profile_name)
        val profileEmail = drawerMenuView.findViewById<TextView>(R.id.profile_email)
        val profileImage = drawerMenuView.findViewById<ImageView>(R.id.profile_image)

        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val fname = sharedPref.getString("fname", "User")
        val lname = sharedPref.getString("lname", "")
        val email = sharedPref.getString("userEmail", "example@email.com")
        val avatarUrl = sharedPref.getString("userAvatarUrl", null)

        profileName.text = "$fname"
        profileEmail.text = email
        if (!avatarUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .circleCrop()
                .into(profileImage)
        } else {
            profileImage.setImageResource(R.drawable.profile)
        }

        // Find menu items by their IDs from layout_drawer_menu.xml
        val menuProfile = drawerMenuView.findViewById<TextView>(R.id.menu_profile)
        val menuWeather = drawerMenuView.findViewById<TextView>(R.id.menu_weather)
        val menuAddTripmates = drawerMenuView.findViewById<TextView>(R.id.menu_add_tripmates)
        val menuFriends = drawerMenuView.findViewById<TextView>(R.id.menu_friends)
        val menuTrainInfo = drawerMenuView.findViewById<TextView>(R.id.menu_train_info)
        val menuLogout = drawerMenuView.findViewById<ImageView>(R.id.menu_logout)
        val menuSOS = drawerMenuView.findViewById<ImageView>(R.id.menu_sos)

        // Uncomment and implement your actual navigation here:
        // menuProfile.setOnClickListener { handleNavigation(R.id.profileFragment) }
        // menuWeather.setOnClickListener { handleNavigation(R.id.weatherFragment) }
        // menuLogout.setOnClickListener { logoutUser() }
        // menuSOS.setOnClickListener { handleNavigation(R.id.emergencyFragment) }
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

    private fun handleNavigation(destinationId: Int) {
        // Navigate using the NavController
        navController.navigate(destinationId)

        // Close the drawer immediately after starting navigation
        drawerLayout.closeDrawers()
    }

    private fun logoutUser() {
        // Clear stored tokens (reusing your commented-out logic from ExploreFragment)
        val sharedPrefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Navigate to the Login screen (ensure R.id.loginFragment exists in nav_graph)
        navController.navigate(R.id.loginFragment)

        drawerLayout.closeDrawers()
    }

    private fun setupDrawerBackButtonHandling() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // If the drawer is open, consume the back event and close it.
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END)
                } else {
                    // If the drawer is closed, allow the default system back behavior
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)
    }

}