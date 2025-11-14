package com.example.tripsync.home

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.SessionManager
import com.example.tripsync.api.models.GetProfileResponse
import kotlinx.coroutines.launch

class OpenProfileFragment : Fragment() {

    private lateinit var nameField: TextView
    private lateinit var emailField: TextView
    private lateinit var contactField: TextView
    private lateinit var bioField: TextView
    private lateinit var genderM: TextView
    private lateinit var genderF: TextView
    private lateinit var bloodGroupField: TextView
    private lateinit var allergiesField: TextView
    private lateinit var emergencyName: TextView
    private lateinit var emergencyNumber: TextView
    private lateinit var emergencyRelation: Spinner
    private lateinit var avatarImage: ImageView
    private lateinit var coverPhoto: ImageView
    private lateinit var editIcon: ImageView

    private lateinit var cardAdventure: View
    private lateinit var cardRelaxation: View
    private lateinit var cardSpiritual: View
    private lateinit var cardHistoric: View
    private lateinit var cardExplore: View
    private lateinit var cardNature: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_open_profile, container, false)
        initViews(view)
        setupSpinner()
        setupBackButton(view)
        setupEditButton()
        setupLogoutButton(view)
        setupDeleteProfileButton(view)
        fetchProfile()
        return view
    }

    private fun initViews(view: View) {
        nameField = view.findViewById(R.id.name)
        emailField = view.findViewById(R.id.email)
        contactField = view.findViewById(R.id.contact)
        bioField = view.findViewById(R.id.bio_content)
        genderM = view.findViewById(R.id.genderM)
        genderF = view.findViewById(R.id.genderF)
        bloodGroupField = view.findViewById(R.id.blood_group_value)
        allergiesField = view.findViewById(R.id.allergies_value)
        emergencyName = view.findViewById(R.id.contact_name_input)
        emergencyNumber = view.findViewById(R.id.contact_phone_input)
        emergencyRelation = view.findViewById(R.id.relationship_spinner)
        avatarImage = view.findViewById(R.id.avatar)
        coverPhoto = view.findViewById(R.id.cover_photo)
        editIcon = view.findViewById(R.id.edit_icon)

        cardAdventure = view.findViewById(R.id.cardAdventure)
        cardRelaxation = view.findViewById(R.id.cardRelaxation)
        cardSpiritual = view.findViewById(R.id.cardSpiritual)
        cardHistoric = view.findViewById(R.id.cardHistoric)
        cardExplore = view.findViewById(R.id.cardExplore)
        cardNature = view.findViewById(R.id.cardNature)
    }

    private fun setupDeleteProfileButton(view: View) {
        val deleteButton = view.findViewById<LinearLayout>(R.id.delete_account)
        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to permanently delete your profile and account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteUserAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteUserAccount() {
        lifecycleScope.launch {
            try {
                // Show loading state
                Toast.makeText(requireContext(), "Deleting account...", Toast.LENGTH_SHORT).show()

                val response = ApiClient.getTokenService(requireContext()).deleteProfile()

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body?.success == true) {
                        Log.d("OpenProfile", "Account deleted successfully")

                        // Clear ALL data including auth tokens
                        SessionManager.clearAllData(requireContext())

                        Toast.makeText(
                            requireContext(),
                            "Account deleted successfully",
                            Toast.LENGTH_LONG
                        ).show()

                        // Navigate to Login and clear backstack
                        findNavController().navigate(R.id.action_openProfileFragment_to_loginFragment)
                    } else {
                        val errorMessage = body?.message ?: "Unable to delete account"
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                        Log.e("OpenProfile", "Delete failed: $errorMessage")
                    }
                } else {
                    handleDeleteError(response.code(), response.errorBody()?.string())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("OpenProfile", "Delete exception: ${e.message}")
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message ?: "Network error"}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun handleDeleteError(code: Int, errorBody: String?) {
        val message = when (code) {
            404 -> "Profile not found"
            500 -> "Server error. Please try again later."
            else -> "Failed to delete account (Code: $code)"
        }

        Log.e("OpenProfile", "Delete error $code: $errorBody")
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.relationship_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        emergencyRelation.adapter = adapter
        emergencyRelation.isEnabled = false
    }

    private fun setupEditButton() {
        editIcon.setOnClickListener {
            findNavController().navigate(R.id.action_openProfileFragment_to_editProfileFragment)
        }
    }

    private fun setupBackButton(view: View) {
        val backButton = view.findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupLogoutButton(view: View) {
        val logoutButton = view.findViewById<LinearLayout>(R.id.logout_button)
        logoutButton.setOnClickListener {
            performLogout()
        }
    }

    private fun performLogout() {
        // Use SessionManager to properly clear only auth data (keeps profile data)
        SessionManager.logout(requireContext())

        // Navigate to login
        findNavController().navigate(R.id.action_openProfileFragment_to_loginFragment)
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

    private fun fetchProfile() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.getTokenService(requireContext()).getProfile()
                if (response.isSuccessful) {
                    val profile = response.body()?.data?.profile
                    if (profile != null) {
                        // Get the user's actual email from SessionManager
                        val userEmail = SessionManager.getEmail(requireContext())

                        // Save CORRECT profile data to SessionManager
                        SessionManager.saveUserProfile(
                            requireContext(),
                            firstName = profile.fname,
                            lastName = profile.lname,
                            email = userEmail, // User's email from SessionManager
                            phone = profile.phone_number, // User's phone
                            avatarUrl = profile.profile_pic_url,
                            bio = profile.bio,
                            gender = profile.gender,
                            preference = profile.prefrence,
                            bloodGroup = profile.bgroup,
                            allergies = profile.allergies,
                            medical = profile.medical,
                            emergencyNumber = profile.enumber,
                            emergencyName = profile.ename,
                            emergencyRelation = profile.erelation,
                        )

                        // Update profile completion status
                        SessionManager.checkAndUpdateProfileStatus(requireContext())

                        Log.d("OpenProfile", "User Email: $userEmail, Phone: ${profile.phone_number}")
                        Log.d("OpenProfile", "Emergency: ${profile.ename}, ${profile.enumber}")

                        bindProfile(profile, userEmail)
                    }
                } else {
                    Toast.makeText(requireContext(), "Error loading profile: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindProfile(profile: GetProfileResponse.ProfileData, userEmail: String?) {
        // Display USER data (not emergency contact data!)
        nameField.text = "${profile.fname ?: ""} ${profile.lname ?: ""}".trim()
        emailField.text = userEmail ?: "No email" // User's email
        contactField.text = profile.phone_number ?: "No phone" // User's phone
        bioField.text = profile.bio ?: "No bio available"
        bloodGroupField.text = profile.bgroup ?: "Not specified"
        allergiesField.text = profile.allergies ?: "None"

        // Display EMERGENCY CONTACT data
        emergencyName.text = profile.ename ?: "Not set"
        emergencyNumber.text = profile.enumber ?: "Not set"

        // Set relationship spinner
        val relations = resources.getStringArray(R.array.relationship_options)
        val index = relations.indexOfFirst { it.equals(profile.erelation, ignoreCase = true) }
        if (index >= 0) {
            emergencyRelation.setSelection(index)
        }

        // Set gender
        when (profile.gender?.lowercase()) {
            "male" -> {
                genderM.alpha = 1f
                genderF.alpha = 0.3f
            }
            "female" -> {
                genderF.alpha = 1f
                genderM.alpha = 0.3f
            }
            else -> {
                genderM.alpha = 0.5f
                genderF.alpha = 0.5f
            }
        }

        // Highlight ONLY the selected interest
        highlightInterest(profile.prefrence)

        // Load profile picture
        Glide.with(this)
            .load(profile.profile_pic_url ?: R.drawable.placeholder_image)
            .circleCrop()
            .into(avatarImage)

        // Load cover photo
        Glide.with(this)
            .load(R.drawable.cover_image)
            .centerCrop()
            .into(coverPhoto)
    }

    private fun highlightInterest(preference: String?) {
        val allCards = listOf(cardAdventure, cardRelaxation, cardSpiritual, cardHistoric, cardExplore, cardNature)

        // Reset ALL cards to transparent (not selected)
        allCards.forEach { card ->
            card.alpha = 0.3f
        }

        // Highlight ONLY the selected preference
        val selectedCard = when (preference?.lowercase()?.trim()) {
            "adventure" -> cardAdventure
            "relaxation" -> cardRelaxation
            "spiritual" -> cardSpiritual
            "historic" -> cardHistoric
            "explore" -> cardExplore
            "nature" -> cardNature
            else -> null
        }

        // Make ONLY selected card fully visible
        selectedCard?.alpha = 1f

        Log.d("OpenProfile", "Highlighting preference: $preference")
    }

    override fun onResume() {
        super.onResume()
        fetchProfile()
    }
}