package com.example.tripsync.ProfileDetails

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import java.io.File
import java.io.FileOutputStream

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
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
                    if (prevId != card.id) view.findViewById<ConstraintLayout>(prevId).isSelected = false
                }
                val nowSelected = !card.isSelected
                card.isSelected = nowSelected
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

        btnPrev.setOnClickListener {
            findNavController().navigate(R.id.action_preferencesFragment_to_emergencyFragment)
        }

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
            FileOutputStream(temp).use { out -> input.copyTo(out) }
        } ?: return@withContext null
        val mime = cr.getType(uri) ?: "image/jpeg"
        val body = temp.asRequestBody(mime.toMediaTypeOrNull())
        MultipartBody.Part.createFormData("image", temp.name, body)
    }

    private fun submitProfile() {
        val bearer = bearer()
        if (bearer.isNullOrEmpty()) {
            Toast.makeText(context, "Not logged in. Please sign in again.", Toast.LENGTH_SHORT).show()
            return
        }

        val service = ApiClient.getAuthService(requireContext())

        val safeBgroup = (vm.bgroup?.trim().orEmpty()).ifEmpty { "O+" }
        val safeEname = vm.ename.trim().ifEmpty { "NA" }
        val safeERelation = when (vm.erelation.trim()) {
            "Parent", "Parents" -> "Parent"
            "Sibling" -> "Sibling"
            "Friend" -> "Friend"
            "Spouse" -> "Spouse"
            "Other" -> "Other"
            else -> "Other"
        }
        val safePhone = normalizeToIndiaE164(vm.phoneNumber)
        val safeENumber = normalizeToIndiaE164(vm.enumberRaw)
        val safePref = vm.preference

        val req = CreateProfileRequest(
            fname = vm.firstName,
            lname = vm.lastName,
            phone_number = safePhone,
            date = vm.dob,
            gender = vm.gender,
            bio = vm.aboutMe,
            bgroup = safeBgroup,
            allergies = vm.allergies,
            medical = vm.medical,
            ename = safeEname,
            enumber = safeENumber,
            erelation = safeERelation,
            preference = safePref
        )

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            val createResp = withContext(Dispatchers.IO) { service.createProfile(bearer, req) }

            if (createResp.isSuccessful) {
                val part = uriToPart()
                if (part != null) {
                    withContext(Dispatchers.IO) { service.uploadProfileImage(bearer, part) }
                }
                Toast.makeText(context, "Profile created", Toast.LENGTH_SHORT).show()
                findNavController().navigate(
                    R.id.action_preferencesFragment_to_contactVerifyFragment,
                    Bundle().apply { putString("travel_preference", vm.preference) }
                )
            } else {
                val code = createResp.code()
                val body = createResp.errorBody()?.string()
                android.util.Log.e("ProfileCreate", "Error: $body")
                Toast.makeText(context, "failed to upload details ($code)", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
