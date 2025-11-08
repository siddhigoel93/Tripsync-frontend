package com.example.tripsync

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.models.GetProfileResponse
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class OpenProfileFragment : Fragment() {

    // Views
    private lateinit var nameField: EditText
    private lateinit var emailField: EditText
    private lateinit var contactField: EditText
    private lateinit var bioField: EditText
    private lateinit var genderM: TextView
    private lateinit var genderF: TextView
    private lateinit var bloodGroupField: EditText
    private lateinit var allergiesField: EditText
    private lateinit var emergencyName: EditText
    private lateinit var emergencyNumber: EditText
    private lateinit var emergencyRelation: Spinner
    private lateinit var avatarImage: ImageView
    private lateinit var coverPhoto: ImageView
    private lateinit var editIcon: ImageView

    // Interests
    private lateinit var cardAdventure: View
    private lateinit var cardRelaxation: View
    private lateinit var cardSpiritual: View
    private lateinit var cardHistoric: View
    private var selectedInterest: String? = null

    private var isEditing = false
    private var imageUri: Uri? = null
    private var selectedGender: String? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_open_profile, container, false)
        initViews(view)
        setupSpinner()
        toggleEditable(false)
        setupInterestClicks()
        setupEditButton()
        setupGenderClicks()
        fetchProfile()
        setupAvatarClick()
        setupBackButton(view)
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

        // Interest cards
        cardAdventure = view.findViewById(R.id.cardAdventure)
        cardRelaxation = view.findViewById(R.id.cardRelaxation)
        cardSpiritual = view.findViewById(R.id.cardSpiritual)
        cardHistoric = view.findViewById(R.id.cardHistoric)
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.relationship_options,
            R.layout.spinner_item
        )
        adapter.setDropDownViewResource(R.layout.spinner_item)
        emergencyRelation.adapter = adapter
    }

    private fun setupInterestClicks() {
        val clickListener = View.OnClickListener { view ->
            listOf(cardAdventure, cardRelaxation, cardSpiritual, cardHistoric).forEach {
                it.alpha = 0.5f
            }
            view.alpha = 1f
            selectedInterest = when (view.id) {
                R.id.cardAdventure -> "Adventure"
                R.id.cardRelaxation -> "Relaxation"
                R.id.cardSpiritual -> "Spiritual"
                R.id.cardHistoric -> "Historic"
                else -> null
            }
        }
        cardAdventure.setOnClickListener(clickListener)
        cardRelaxation.setOnClickListener(clickListener)
        cardSpiritual.setOnClickListener(clickListener)
        cardHistoric.setOnClickListener(clickListener)
    }
    private fun setupGenderClicks() {
        // Assume you have gender_selector (default) and gender_selected (active) drawables
        val normalBg = ContextCompat.getDrawable(requireContext(), R.drawable.gender_selector)
        val selectedBg = ContextCompat.getDrawable(requireContext(), R.drawable.gender_selected)

        genderM.setOnClickListener {
            if (isEditing) {
                selectedGender = "Male"
                genderM.background = selectedBg
                genderF.background = normalBg
            }
        }
        genderF.setOnClickListener {
            if (isEditing) {
                selectedGender = "Female"
                genderF.background = selectedBg
                genderM.background = normalBg
            }
        }
    }

    private fun setupEditButton() {
        editIcon.setOnClickListener {
            isEditing = !isEditing
            toggleEditable(isEditing)

            if (isEditing) {
                editIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.green_gradient_end))
            } else {
                editIcon.clearColorFilter()
                updateProfile() // PATCH API call
            }
        }
    }

    private fun setupAvatarClick() {
        avatarImage.setOnClickListener {
            if (isEditing) {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, PICK_IMAGE_REQUEST)
            }
        }
    }
    private fun setupBackButton(view: View) {
        val backButton = view.findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun toggleEditable(enable: Boolean) {
        val editFields = listOf(
            nameField, emailField, contactField, bioField,
            bloodGroupField, allergiesField, emergencyName, emergencyNumber
        )
        editFields.forEach { it.isEnabled = enable }
        emergencyRelation.isEnabled = enable
        cardAdventure.isClickable = enable
        cardRelaxation.isClickable = enable
        cardSpiritual.isClickable = enable
        cardHistoric.isClickable = enable
    }

    // ------------------- API CALLS --------------------

    private fun fetchProfile() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.getTokenService(requireContext()).getProfile()
                if (response.isSuccessful) {
                    val body = response.body()
                    val profile = body?.data?.profile
                    if (body?.success == true && profile != null) bindProfile(profile)
                    else Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
                } else Toast.makeText(requireContext(), "Error ${response.code()}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProfile() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getTokenService(requireContext())

                val fname = nameField.text.toString().split(" ").firstOrNull().orEmpty()
                val lname = nameField.text.toString().split(" ").getOrNull(1).orEmpty()
                val bio = bioField.text.toString()
                val bgroup = bloodGroupField.text.toString()
                val allergies = allergiesField.text.toString()
                val ename = emergencyName.text.toString()
                val enumber = emergencyNumber.text.toString()
                val erelation = emergencyRelation.selectedItem.toString()

                val fnamePart = fname.toRequestBody("text/plain".toMediaTypeOrNull())
                val lnamePart = lname.toRequestBody("text/plain".toMediaTypeOrNull())
                val bioPart = bio.toRequestBody("text/plain".toMediaTypeOrNull())
                val bgroupPart = bgroup.toRequestBody("text/plain".toMediaTypeOrNull())
                val allergiesPart = allergies.toRequestBody("text/plain".toMediaTypeOrNull())
                val enamePart = ename.toRequestBody("text/plain".toMediaTypeOrNull())
                val enumberPart = enumber.toRequestBody("text/plain".toMediaTypeOrNull())
                val erelationPart = erelation.toRequestBody("text/plain".toMediaTypeOrNull())
                val prefrencePart = selectedInterest?.toRequestBody("text/plain".toMediaTypeOrNull())

                val imagePart = imageUri?.let {
                    val file = File(getRealPathFromURI(it))
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("profile_pic", file.name, requestFile)
                }

                val response = api.updateProfile(
                    fnamePart, lnamePart, null, null, bioPart,
                    imagePart, bgroupPart, allergiesPart, null,
                    enamePart, enumberPart, erelationPart, prefrencePart
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    fetchProfile()
                } else {
                    Toast.makeText(requireContext(), "Failed to update: ${response.code()}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindProfile(profile: GetProfileResponse.ProfileData) {
        nameField.setText("${profile.fname ?: ""} ${profile.lname ?: ""}")
        emailField.setText("example@gmail.com") // you can set from API if exists
        contactField.setText(profile.phone_number ?: "")
        bioField.setText(profile.bio ?: "")
        bloodGroupField.setText(profile.bgroup ?: "")
        allergiesField.setText(profile.allergies ?: "")
        emergencyName.setText(profile.ename ?: "")
        emergencyNumber.setText(profile.enumber ?: "")

        val relations = resources.getStringArray(R.array.relationship_options)
        val index = relations.indexOfFirst { it.equals(profile.erelation, true) }
        if (index >= 0) emergencyRelation.setSelection(index)

        highlightInterest(profile.prefrence)

        Glide.with(this)
            .load(profile.profile_pic_url ?: R.drawable.placeholder_image)
            .circleCrop()
            .into(avatarImage)
    }

    private fun highlightInterest(preference: String?) {
        val all = listOf(cardAdventure, cardRelaxation, cardSpiritual, cardHistoric)
        all.forEach { it.alpha = 0.5f }
        when (preference?.lowercase()) {
            "adventure" -> cardAdventure.alpha = 1f
            "relaxation" -> cardRelaxation.alpha = 1f
            "spiritual" -> cardSpiritual.alpha = 1f
            "historic" -> cardHistoric.alpha = 1f
        }
    }

    private fun getRealPathFromURI(uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = requireContext().contentResolver.query(uri, projection, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val filePath = cursor?.getString(columnIndex ?: 0)
        cursor?.close()
        return filePath ?: ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data?.data != null) {
            imageUri = data.data
            avatarImage.setImageURI(imageUri)
        }
    }
}
