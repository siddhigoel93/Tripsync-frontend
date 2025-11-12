package com.example.tripsync

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
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
import androidx.annotation.RequiresApi
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.models.LogoutRequest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var progressLayout: View
    lateinit var app_bar_layout: AppBarLayout
    lateinit var bottom_app_bar_wrapper: BottomAppBar
    lateinit var bottom_nav_view: BottomNavigationView
    lateinit var fab_store: FloatingActionButton
    private val profileObservers = mutableListOf<(String?) -> Unit>()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

//        WindowInsetsControllerCompat(window, window.decorView).apply {
//            isAppearanceLightStatusBars = false
//            isAppearanceLightNavigationBars = false
//        }

        val requestFocus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)

        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = true

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_content_container)) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            view.setPadding(0, statusBarHeight, 0, 0)
            insets
        }
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = true



        progressLayout = findViewById(R.id.profileProgressLayout)
        app_bar_layout = findViewById(R.id.app_bar_layout)
        bottom_app_bar_wrapper = findViewById(R.id.bottom_app_bar)
        bottom_nav_view = findViewById(R.id.bottom_navigation_view)
        fab_store = findViewById(R.id.fab_store)

        drawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END)


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        this.navController = navHostFragment.navController

        setupDrawerMenuListeners()
        setupDrawerBackButtonHandling()

        bottom_nav_view.setupWithNavController(navController)

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
                R.id.trendingDestinationFragment,
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

        refreshDrawerProfile()

        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {
                refreshDrawerProfile()
            }

            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerStateChanged(newState: Int) {}
        })

        val menuProfile = drawerMenuView.findViewById<TextView>(R.id.menu_profile)
        val menuDelete = drawerMenuView.findViewById<ImageView>(R.id.menu_delete)
        val menuLogout = drawerMenuView.findViewById<ImageView>(R.id.menu_logout)
        val menuSOS = drawerMenuView.findViewById<ImageView>(R.id.menu_sos)

        menuProfile.setOnClickListener { handleNavigation(R.id.openProfileFragment) }
        menuLogout.setOnClickListener { logoutUser() }
        menuDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
        menuSOS.setOnClickListener { handleNavigation(R.id.emergencySosFragment) }
    }

    private fun refreshDrawerProfile() {
        val drawerMenuView = findViewById<View>(R.id.drawer_menu_include)
        val profileName = drawerMenuView.findViewById<TextView>(R.id.profile_name)
        val profileEmail = drawerMenuView.findViewById<TextView>(R.id.profile_email)
        val profileImage = drawerMenuView.findViewById<ImageView>(R.id.profile_image)

        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val fname = sharedPref.getString("fname", "User")
        val lname = sharedPref.getString("lname", "")
        val email = sharedPref.getString("currentUserEmail", "example@email.com")
        val avatarUrl = sharedPref.getString("userAvatarUrl", null)

        val fullName = "$fname $lname".trim()
        profileName.text = fname
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
        navController.navigate(destinationId)
        drawerLayout.closeDrawers()
    }

    private fun setupDrawerBackButtonHandling() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)
    }

    fun updateDrawerProfileImage(url: String?) {
        refreshDrawerProfile()
        profileObservers.forEach { it(url) }
    }

    private fun showDeleteConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to permanently delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteUserAccount()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteUserAccount() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getTokenService(this@MainActivity)
                val response = api.deleteProfile()

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        clearAllUserData()

                        Toast.makeText(
                            this@MainActivity,
                            "Account deleted successfully",
                            Toast.LENGTH_LONG
                        ).show()

                        navController.navigate(R.id.loginFragment)
                        drawerLayout.closeDrawers()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            body?.message ?: "Failed to delete account",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        404 -> "Profile not found"
                        500 -> "Server error. Please try again later."
                        else -> "Failed to delete account: ${response.code()}"
                    }
                    Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Network error: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun clearAllUserData() {
        val sharedPrefsAuth = getSharedPreferences("auth", Context.MODE_PRIVATE)
        sharedPrefsAuth.edit().clear().apply()
        val sharedPrefsApp = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPrefsApp.edit().clear().apply()
        val sharedPrefsLiked = getSharedPreferences("liked_posts", Context.MODE_PRIVATE)
        sharedPrefsLiked.edit().clear().apply()
    }

    fun addProfileObserver(observer: (String?) -> Unit) {
        profileObservers.add(observer)
    }

    fun removeProfileObserver(observer: (String?) -> Unit) {
        profileObservers.remove(observer)
    }

    private fun logoutUser() {
        lifecycleScope.launch {
            try {
                val sharedPrefsAuth = getSharedPreferences("auth", Context.MODE_PRIVATE)
                val refreshToken = sharedPrefsAuth.getString("refresh_token", null)

                if (refreshToken.isNullOrEmpty()) {
                    clearAuthData()
                    Toast.makeText(this@MainActivity, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    navController.navigate(R.id.loginFragment)
                    drawerLayout.closeDrawers()
                    return@launch
                }

                val api = ApiClient.getTokenService(this@MainActivity)
                val request = LogoutRequest(refresh = refreshToken)
                val response = api.logout(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == "success") {
                        clearAuthData()
                        Toast.makeText(this@MainActivity, body.message ?: "Logged out successfully", Toast.LENGTH_SHORT).show()
                        navController.navigate(R.id.loginFragment)
                        drawerLayout.closeDrawers()
                    } else {
                        clearAuthData()
                        Toast.makeText(this@MainActivity, "Logged out", Toast.LENGTH_SHORT).show()
                        navController.navigate(R.id.loginFragment)
                        drawerLayout.closeDrawers()
                    }
                } else {
                    clearAuthData()
                    val errorMsg = when (response.code()) {
                        400 -> "Invalid session. Logged out locally."
                        else -> "Logged out locally"
                    }
                    Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    navController.navigate(R.id.loginFragment)
                    drawerLayout.closeDrawers()
                }
            } catch (e: Exception) {
                clearAuthData()
                Toast.makeText(this@MainActivity, "Logged out locally", Toast.LENGTH_SHORT).show()
                navController.navigate(R.id.loginFragment)
                drawerLayout.closeDrawers()
            }
        }
    }

    private fun clearAuthData() {
        val sharedPrefsAuth = getSharedPreferences("auth", Context.MODE_PRIVATE)
        sharedPrefsAuth.edit().clear().apply()
        val sharedPrefsApp = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPrefsApp.edit().clear().apply()
    }
}