package com.example.tripsync.ProfileDetails

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.models.CreateProfileRequest
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

fun String.toRequestBody(): RequestBody = this.toRequestBody("text/plain".toMediaTypeOrNull())


class PreferencesFragment : Fragment() {

    private val vm: ProfileViewModel by activityViewModels()
    private var selectedCardId: Int? = null

    private val preferenceMap = mapOf(
        R.id.cardAdventure to "Adventure",
        R.id.cardRelaxation to "Relaxation",
        R.id.cardNature to "Nature",
        R.id.cardExplore to "Explore",
        R.id.cardSpiritual to "Spiritual",
        R.id.cardHistoric to "Historic"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_preferences, container, false)
        val btnPrev = view.findViewById<MaterialButton>(R.id.btnPrevious)
        val btnNext = view.findViewById<MaterialButton>(R.id.btn)

        val cards = listOf(
            view.findViewById<ConstraintLayout>(R.id.cardAdventure),
            view.findViewById<ConstraintLayout>(R.id.cardRelaxation),
            view.findViewById<ConstraintLayout>(R.id.cardNature),
            view.findViewById<ConstraintLayout>(R.id.cardExplore),
            view.findViewById<ConstraintLayout>(R.id.cardSpiritual),
            view.findViewById<ConstraintLayout>(R.id.cardHistoric)
        )

        selectedCardId = preferenceMap.entries.firstOrNull { it.value == vm.preference }?.key
        selectedCardId?.let { id -> view.findViewById<ConstraintLayout>(id).isSelected = true }

        cards.forEach { card ->
            card.setOnClickListener {
                selectedCardId?.let { prevId ->
                    val prevCard = view.findViewById<ConstraintLayout>(prevId)
                    prevCard.isSelected = false
                    val prevIcon = prevCard.getChildAt(0) as? ImageView
                    prevIcon?.isSelected = false
                }
                val nowSelected = !card.isSelected
                card.isSelected = nowSelected
                val icon = card.getChildAt(0) as? ImageView
                icon?.isSelected = nowSelected
                selectedCardId = if (nowSelected) card.id else null
            }
        }

        btnNext.setOnClickListener {
            val selected = selectedCardId?.let { preferenceMap[it] }
            if (selected == null) {
                Toast.makeText(context, "Please select one preference.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            vm.preference = selected
            submitProfile()
        }

        btnPrev.setOnClickListener { findNavController().navigate(R.id.action_preferencesFragment_to_emergencyFragment) }
        return view
    }

    private fun bearer(): String? {
        val token = requireContext()
            .getSharedPreferences("auth", Context.MODE_PRIVATE)
            .getString("access_token", null)
        return token?.let { "Bearer $it" }
    }

    private fun normalizeToIndiaE164(raw: String): String {
        val digitsOnly = raw.filter { it.isDigit() }
        return if (raw.trim().startsWith("+")) raw.trim() else "+91$digitsOnly"
    }

    private suspend fun uriToPart(): MultipartBody.Part? = withContext(Dispatchers.IO) {
        val uri = vm.imageUri ?: return@withContext null
        val cr = requireContext().contentResolver
        val name = (uri.lastPathSegment ?: "profile.jpg").take(80)
        val temp = File(requireContext().cacheDir, name)
        cr.openInputStream(uri)?.use { input ->
            FileOutputStream(temp).use { out ->
                val bitmap = android.graphics.BitmapFactory.decodeStream(input)
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, out)
            }
        } ?: return@withContext null
        val mime = cr.getType(uri) ?: "image/jpeg"
        val body = temp.asRequestBody(mime.toMediaTypeOrNull())
        MultipartBody.Part.createFormData("profile_pic", temp.name, body)
    }
//
//    private fun submitProfile() {
//        val bearer = bearer()
//        if (bearer.isNullOrEmpty()) {
//            Toast.makeText(context, "Not logged in. Please sign in again.", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val service = ApiClient.getAuthService(requireContext())
//
//        val req = CreateProfileRequest(
//            fname = vm.firstName,
//            lname = vm.lastName,
//            phone_number = normalizeToIndiaE164(vm.phoneNumber),
//            date = vm.dob,
//            gender = vm.gender,
//            bio = vm.aboutMe,
//            bgroup = vm.bgroup,
//            allergies = vm.allergies,
//            medical = vm.medical,
//            ename = vm.ename,
//            enumber = normalizeToIndiaE164(vm.enumberRaw),
//            erelation = vm.erelation,
//            preference = vm.preference
//        )
////        val prefs = requireContext().getSharedPreferences("user", Context.MODE_PRIVATE)
////        prefs.edit()
////            .putString("fname", vm.firstName)
////            .putString("lname", vm.lastName)
////            .apply()
//
//
//        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
//            try {
//                // Step 1: Create profile via JSON
//                val createResp = withContext(Dispatchers.IO) { service.createProfile(bearer, req) }
//
//                if (!createResp.isSuccessful) {
//                    val code = createResp.code()
//                    val errorBody = createResp.errorBody()?.string()?.trim().orEmpty()
//                    Log.e("ProfileCreate", "Error ($code): $errorBody")
//                    Toast.makeText(context, "Failed to create profile ($code)", Toast.LENGTH_LONG).show()
//                    return@launchWhenStarted
//                }
//
//                Toast.makeText(context, "Profile created successfully", Toast.LENGTH_SHORT).show()
//
//                // Step 2: Upload profile picture if selected
//                val part = uriToPart()
//                if (part != null) {
//                    val imageResp = withContext(Dispatchers.IO) { service.uploadProfileImage(bearer, part) }
//
//                    if (imageResp.isSuccessful) {
//                        Toast.makeText(context, "Profile picture uploaded successfully", Toast.LENGTH_SHORT).show()
//                        val partName = part.headers?.get("Content-Disposition") ?: "unknown"
//                        Log.d("ProfileCreate", "Image uploaded: $partName")
//                    } else {
//                        val code = imageResp.code()
//                        val errorBody = imageResp.errorBody()?.string()?.trim().orEmpty()
//                        Log.e("ProfileCreate", "Image upload failed ($code): $errorBody")
//                        Toast.makeText(context, "Profile created but image upload failed", Toast.LENGTH_LONG).show()
//                    }
//                } else {
//                    Log.d("ProfileCreate", "No image selected, skipping upload")
//                }
//
//                // Step 3: Navigate to next screen
//                findNavController().navigate(
//                    R.id.action_preferencesFragment_to_contactVerifyFragment,
//                    Bundle().apply { putString("travel_preference", vm.preference) }
//                )
//
//            } catch (e: Exception) {
//                Log.e("ProfileCreate", "Unexpected error: ${e.message}")
//                Toast.makeText(context, "Something went wrong. Please try again later.", Toast.LENGTH_SHORT).show()
//            }

    private fun submitProfile() {
        val bearer = bearer()
        if (bearer.isNullOrEmpty()) {
            Toast.makeText(context, "Not logged in. Please sign in again.", Toast.LENGTH_SHORT).show()
            return
        }

        val service = ApiClient.getAuthService(requireContext())

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            try {
                val imagePart = uriToPart() // optional

                val response = withContext(Dispatchers.IO) {
                    service.createProfileWithImage(
                        bearer,
                        vm.firstName.toRequestBody(),
                        vm.lastName.toRequestBody(),
                        normalizeToIndiaE164(vm.phoneNumber).toRequestBody(),
                        vm.dob.toRequestBody(),
                        vm.gender.toRequestBody(),
                        vm.aboutMe.toRequestBody(),
                        vm.bgroup.toRequestBody(),
                        vm.allergies.toRequestBody(),
                        vm.medical.toRequestBody(),
                        vm.ename.toRequestBody(),
                        normalizeToIndiaE164(vm.enumberRaw).toRequestBody(),
                        vm.erelation.toRequestBody(),
                        vm.preference.toRequestBody(),
                        imagePart
                    )
                }

                if (response.isSuccessful) {
                    Toast.makeText(context, "Profile created successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(
                        R.id.action_preferencesFragment_to_contactVerifyFragment,
                        Bundle().apply { putString("travel_preference", vm.preference) }
                    )
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e("ProfileCreate", "Error: $errorBody")
                    Toast.makeText(context, "Failed to create profile", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("ProfileCreate", "Exception: ${e.message}")
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }
    }

}





