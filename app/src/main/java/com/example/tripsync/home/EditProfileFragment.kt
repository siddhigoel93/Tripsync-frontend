package com.example.tripsync.home

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.tripsync.MainActivity
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.SessionManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class EditProfileFragment : Fragment() {

    private lateinit var avatarImage: ShapeableImageView
    private lateinit var firstNameInput: TextInputEditText
    private lateinit var lastNameInput: TextInputEditText
    private lateinit var phoneInput: TextInputEditText
    private lateinit var dobInput: TextInputEditText
    private lateinit var aboutInput: TextInputEditText
    private lateinit var emergencyNameInput: TextInputEditText
    private lateinit var emergencyPhoneInput: TextInputEditText
    private lateinit var relationshipSpinner: Spinner
    private lateinit var genderMale: RadioButton
    private lateinit var genderFemale: RadioButton
    private lateinit var genderOthers: RadioButton

    private var selectedImageUri: Uri? = null
    private var selectedGender: String? = null
    private var selectedTravelStyle: String? = null
    private var selectedDate: String? = null

    private val travelStyleCards = mutableMapOf<String, MaterialCardView>()

    private val imagePicker: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedImageUri = uri
                    Glide.with(this)
                        .load(uri)
                        .circleCrop()
                        .into(avatarImage)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)
        initViews(view)
        setupListeners(view)
        setupSpinner()
        setupTravelStyleCards(view)
        loadProfileData()
        requestPermissions()
        return view
    }

    private fun initViews(view: View) {
        avatarImage = view.findViewById(R.id.avatar_image)
        firstNameInput = view.findViewById(R.id.first_name_input)
        lastNameInput = view.findViewById(R.id.last_name_input)
        phoneInput = view.findViewById(R.id.phone_input)
        dobInput = view.findViewById(R.id.dob_input)
        aboutInput = view.findViewById(R.id.about_input)
        emergencyNameInput = view.findViewById(R.id.emergency_name_input)
        emergencyPhoneInput = view.findViewById(R.id.emergency_phone_input)
        relationshipSpinner = view.findViewById(R.id.relationship_spinner)
        genderMale = view.findViewById(R.id.gender_male)
        genderFemale = view.findViewById(R.id.gender_female)
        genderOthers = view.findViewById(R.id.gender_others)
    }

    private fun setupListeners(view: View) {
        view.findViewById<ImageView>(R.id.back_button).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        view.findViewById<TextView>(R.id.save_button).setOnClickListener {
            saveProfile()
        }

        view.findViewById<View>(R.id.edit_photo_button).setOnClickListener {
            openImagePicker()
        }

        avatarImage.setOnClickListener {
            openImagePicker()
        }

        dobInput.setOnClickListener {
            showDatePicker()
        }

        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.done_button).setOnClickListener {
            saveProfile()
        }

        // Gender radio buttons
        genderMale.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedGender = "male"
        }
        genderFemale.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedGender = "female"
        }
        genderOthers.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedGender = "other"
        }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.relationship_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        relationshipSpinner.adapter = adapter
    }

    private fun setupTravelStyleCards(view: View) {
        travelStyleCards["Adventure"] = view.findViewById(R.id.style_adventure)
        travelStyleCards["Relaxation"] = view.findViewById(R.id.style_relaxation)
        travelStyleCards["Retreat"] = view.findViewById(R.id.style_retreat)
        travelStyleCards["Explore"] = view.findViewById(R.id.style_explore)
        travelStyleCards["Spiritual"] = view.findViewById(R.id.style_spiritual)
        travelStyleCards["Historic"] = view.findViewById(R.id.style_historic)

        travelStyleCards.forEach { (style, card) ->
            card.setOnClickListener {
                selectTravelStyle(style)
            }
        }
    }

    private fun selectTravelStyle(style: String) {
        selectedTravelStyle = style

        // Reset all cards
        travelStyleCards.forEach { (_, card) ->
            card.strokeColor = ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
            card.strokeWidth = 2
        }

        // Highlight selected card
        travelStyleCards[style]?.apply {
            strokeColor = ContextCompat.getColor(requireContext(), R.color.green_gradient_end)
            strokeWidth = 4
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        imagePicker.launch(intent)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        // Parse existing date if available
        selectedDate?.let { dateStr ->
            try {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = dateFormat.parse(dateStr)
                if (date != null) {
                    calendar.time = date
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(selectedYear, selectedMonth, selectedDay)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            selectedDate = dateFormat.format(calendar.time)
            dobInput.setText(selectedDate)
        }, year, month, day).show()
    }

    private fun loadProfileData() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.getTokenService(requireContext()).getProfile()
                if (response.isSuccessful) {
                    val profile = response.body()?.data?.profile
                    if (profile != null) {
                        withContext(Dispatchers.Main) {
                            firstNameInput.setText(profile.fname ?: "")
                            lastNameInput.setText(profile.lname ?: "")
                            phoneInput.setText(profile.phone_number ?: "")
                            dobInput.setText(profile.date ?: "")
                            selectedDate = profile.date
                            aboutInput.setText(profile.bio ?: "")
                            emergencyNameInput.setText(profile.ename ?: "")
                            emergencyPhoneInput.setText(profile.enumber ?: "")

                            // Set gender
                            when (profile.gender?.lowercase()) {
                                "male" -> {
                                    genderMale.isChecked = true
                                    selectedGender = "male"
                                }
                                "female" -> {
                                    genderFemale.isChecked = true
                                    selectedGender = "female"
                                }
                                else -> {
                                    genderOthers.isChecked = true
                                    selectedGender = "other"
                                }
                            }

                            // Set relationship
                            val relations = resources.getStringArray(R.array.relationship_options)
                            val index = relations.indexOfFirst { it.equals(profile.erelation, true) }
                            if (index >= 0) relationshipSpinner.setSelection(index)

                            // Set travel style
                            profile.prefrence?.let {
                                selectTravelStyle(it)
                                selectedTravelStyle = it
                            }

                            // Load avatar
                            if (!profile.profile_pic_url.isNullOrEmpty()) {
                                Glide.with(this@EditProfileFragment)
                                    .load(profile.profile_pic_url)
                                    .circleCrop()
                                    .placeholder(R.drawable.camera)
                                    .error(R.drawable.camera)
                                    .into(avatarImage)
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveProfile() {
        // Validate required fields
        if (firstNameInput.text.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Please enter first name", Toast.LENGTH_SHORT).show()
            return
        }
        if (lastNameInput.text.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Please enter last name", Toast.LENGTH_SHORT).show()
            return
        }
        if (phoneInput.text.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Please enter phone number", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val api = ApiClient.getTokenService(requireContext())

                val dobFormatted = selectedDate?.let {
                    try {
                        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val date = inputFormat.parse(it)
                        outputFormat.format(date!!)
                    } catch (e: Exception) {
                        null
                    }
                }

                val dobPart = dobFormatted
                    ?.toRequestBody("text/plain".toMediaTypeOrNull())

                val fnamePart = firstNameInput.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val lnamePart = lastNameInput.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val phonePart = phoneInput.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val bioPart = aboutInput.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val genderPart = selectedGender?.toRequestBody("text/plain".toMediaTypeOrNull())
                val enamePart = emergencyNameInput.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val enumberPart = emergencyPhoneInput.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val erelationPart = relationshipSpinner.selectedItem.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val preferencePart = selectedTravelStyle?.toRequestBody("text/plain".toMediaTypeOrNull())

                val imagePart = selectedImageUri?.let { uri ->
                    withContext(Dispatchers.IO) {
                        compressImage(uri)
                    }?.let { file ->
                        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("profile_pic", file.name, requestFile)
                    }
                }

                val response = withContext(Dispatchers.IO) {
                    api.updateProfile(
                        fnamePart, lnamePart, dobPart,genderPart,  bioPart,
                        imagePart, null, null, null ,
                        enamePart, enumberPart, erelationPart, preferencePart
                    )
                }

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val updatedProfile = response.body()?.data?.profile

                        if (updatedProfile != null) {
                            SessionManager.saveUserProfile(
                                context = requireContext(),
                                firstName = updatedProfile.fname,
                                lastName = updatedProfile.lname,
                                phone = updatedProfile.phone_number,
                                avatarUrl = updatedProfile.profile_pic_url,
                                bio = updatedProfile.bio,
                                gender = updatedProfile.gender,
                                preference = updatedProfile.prefrence,
                                bloodGroup = updatedProfile.bgroup,
                                allergies = updatedProfile.allergies
                            )

                            if (activity != null && isAdded) {
                                (activity as? MainActivity)?.updateDrawerProfileImage(updatedProfile.profile_pic_url)
                            }
                        }

                        Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        Toast.makeText(requireContext(), "Update failed: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun compressImage(uri: Uri, maxSizeKB: Int = 500): File? {
        return try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (bitmap == null) return null

            val outputStream = ByteArrayOutputStream()
            var quality = 100
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

            while (outputStream.toByteArray().size / 1024 > maxSizeKB && quality > 10) {
                outputStream.reset()
                quality -= 10
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }

            val file = File(requireContext().cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { it.write(outputStream.toByteArray()) }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 101)
        }
    }
}