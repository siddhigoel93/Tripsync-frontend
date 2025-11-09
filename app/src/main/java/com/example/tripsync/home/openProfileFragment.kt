package com.example.tripsync.home

import android.Manifest
import android.app.Activity
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.tripsync.MainActivity
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.models.GetProfileResponse
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class OpenProfileFragment : Fragment() {

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

    private lateinit var cardAdventure: View
    private lateinit var cardRelaxation: View
    private lateinit var cardSpiritual: View
    private lateinit var cardHistoric: View

    private var selectedInterest: String? = null
    private var isEditing = false
    private var selectedGender: String? = null
    private var imageUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1001
        private const val READ_PERMISSION_REQUEST = 101
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
        setupAvatarClick()
        setupBackButton(view)
        fetchProfile()
        requestReadPermission()
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
        val normalBg = ContextCompat.getDrawable(requireContext(), R.drawable.selected_gender)
        val selectedBg = ContextCompat.getDrawable(requireContext(), R.drawable.selected_gender)

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
            if (!isEditing) updateProfile()

            if (isEditing) {
                editIcon.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.green_gradient_end)
                )
            } else {
                editIcon.clearColorFilter()
                updateProfile()
            }

        }
    }

    private fun setupAvatarClick() {
        avatarImage.setOnClickListener {
            if (isEditing) {
                val intent = Intent(Intent.ACTION_PICK).apply {
                    type = "image/*"
                }
                startActivityForResult(intent, PICK_IMAGE_REQUEST)
            }
        }
    }

    private fun setupBackButton(view: View) {
        val backButton = view.findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
    }

    private fun toggleEditable(enable: Boolean) {
        listOf(
            nameField, emailField, contactField, bioField,
            bloodGroupField, allergiesField, emergencyName, emergencyNumber
        ).forEach { it.isEnabled = enable }
        emergencyRelation.isEnabled = enable
        listOf(cardAdventure, cardRelaxation, cardSpiritual, cardHistoric).forEach { it.isClickable = enable }
    }

    private fun fetchProfile() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.getTokenService(requireContext()).getProfile()
                if (response.isSuccessful) {
                    val profile = response.body()?.data?.profile
                    if (profile != null) bindProfile(profile)
                } else Toast.makeText(requireContext(), "Error ${response.code()}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun bindProfile(profile: GetProfileResponse.ProfileData) {
        val sp = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val email = sp.getString("currentUserEmail", "example@email.com")
        nameField.setText("${profile.fname ?: ""} ${profile.lname ?: ""}")
        emailField.setText(email ?: "")
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

        Glide.with(this).load(profile.profile_pic_url ?: R.drawable.placeholder_image)
            .circleCrop()
            .into(avatarImage)

        Glide.with(this).load(R.drawable.cover_image)
            .centerCrop()
            .into(coverPhoto)
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

    private fun updateProfile() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getTokenService(requireContext())

                val fname = nameField.text.toString().split(" ").firstOrNull().orEmpty()
                val lname = nameField.text.toString().split(" ").getOrNull(1).orEmpty()

                val fnamePart = fname.toRequestBody("text/plain".toMediaTypeOrNull())
                val lnamePart = lname.toRequestBody("text/plain".toMediaTypeOrNull())
                val bioPart = bioField.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val bgroupPart = bloodGroupField.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val allergiesPart = allergiesField.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val enamePart = emergencyName.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val enumberPart = emergencyNumber.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val erelationPart = emergencyRelation.selectedItem.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val preferencePart = selectedInterest?.toRequestBody("text/plain".toMediaTypeOrNull())

                val imagePart = imageUri?.let { uri ->
                    compressImage(uri)?.let { file ->
                        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("profile_pic", file.name, requestFile)
                    }
                }

                val response = api.updateProfile(
                    fnamePart, lnamePart, null, null, bioPart,
                    imagePart, bgroupPart, allergiesPart, null,
                    enamePart, enumberPart, erelationPart, preferencePart
                )

                if (response.isSuccessful) {
                    requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit()
                        .putString("userAvatarUrl", response.body()?.data?.profile?.profile_pic_url)
                        .apply()
                    val imgUrl = response.body()?.data?.profile?.profile_pic_url
                    (activity as? MainActivity)?.updateDrawerProfileImage(imgUrl)


                    Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
                    fetchProfile()
                } else {
                    Toast.makeText(requireContext(), "Update failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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

    private fun requestReadPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_PERMISSION_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data?.data != null) {
            imageUri = data.data
            Glide.with(this).load(imageUri).circleCrop().into(avatarImage)
        }
    }
}
