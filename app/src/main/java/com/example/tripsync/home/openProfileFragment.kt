package com.example.tripsync.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
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
        // Use SessionManager to properly clear only auth data
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
                        // Save profile to SessionManager
                        SessionManager.saveUserProfile(
                            requireContext(),
                            firstName = profile.fname,
                            lastName = profile.lname,
                            email = SessionManager.getEmail(requireContext()),
                            phone = profile.phone_number,
                            avatarUrl = profile.profile_pic_url,
                            bio = profile.bio,
                            gender = profile.gender,
                            preference = profile.prefrence,
                            bloodGroup = profile.bgroup,
                            allergies = profile.allergies
                        )

                        bindProfile(profile)
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

    private fun bindProfile(profile: GetProfileResponse.ProfileData) {
        val email = SessionManager.getEmail(requireContext())

        nameField.text = "${profile.fname ?: ""} ${profile.lname ?: ""}".trim()
        emailField.text = email ?: ""
        contactField.text = profile.phone_number ?: ""
        bioField.text = profile.bio ?: "No bio available"
        bloodGroupField.text = profile.bgroup ?: "Not specified"
        allergiesField.text = profile.allergies ?: "None"
        emergencyName.text = profile.ename ?: ""
        emergencyNumber.text = profile.enumber ?: ""

        // Set relationship spinner
        val relations = resources.getStringArray(R.array.relationship_options)
        val index = relations.indexOfFirst { it.equals(profile.erelation, true) }
        if (index >= 0) emergencyRelation.setSelection(index)

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

        // Highlight selected interest
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
        val all = listOf(cardAdventure, cardRelaxation, cardSpiritual, cardHistoric)

        // Reset all to default state
        all.forEach { card ->
            card.alpha = 0.3f
        }

        // Highlight selected preference
        when (preference?.lowercase()) {
            "adventure" -> cardAdventure.alpha = 1f
            "relaxation" -> cardRelaxation.alpha = 1f
            "spiritual" -> cardSpiritual.alpha = 1f
            "historic" -> cardHistoric.alpha = 1f
            else -> {
                // If no preference, show all at half opacity
                all.forEach { it.alpha = 0.5f }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchProfile()
    }
}