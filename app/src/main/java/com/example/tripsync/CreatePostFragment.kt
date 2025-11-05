package com.example.tripsync

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.tripsync.api.ApiClient
import com.example.tripsync.databinding.FragmentCreatePostBinding
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class CreatePostFragment : Fragment() {

    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!

    private var selectedPhotoUri: Uri? = null
    private var selectedVideoUri: Uri? = null

    companion object {
        const val POST_CREATION_REQUEST_KEY = "post_creation_request"
        const val REFRESH_FEED_KEY = "refresh_feed_key"
    }

    private val photoPicker: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedPhotoUri = result.data?.data
                selectedVideoUri = null
                Toast.makeText(context, "Photo selected. Ready to upload.", Toast.LENGTH_SHORT).show()
            }
        }

    private val videoPicker: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedVideoUri = result.data?.data
                selectedPhotoUri = null
                Toast.makeText(context, "Video selected. Ready to upload.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupToolbar()
        setupListeners()
    }

    private fun setupToolbar() {
        val toolbar = binding.appBar.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)

        toolbar.findViewById<ImageView>(R.id.back_arrow)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        toolbar.findViewById<TextView>(R.id.save_draft)?.setOnClickListener {
            Toast.makeText(context, "Draft saved (placeholder)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        binding.postContentCard.findViewById<ImageView>(R.id.btn_upload_photo).setOnClickListener {
            openMediaPicker(photoPicker, "image/*")
        }

        binding.postContentCard.findViewById<ImageView>(R.id.btn_upload_video).setOnClickListener {
            openMediaPicker(videoPicker, "video/*")
        }

        binding.postContentCard.findViewById<MaterialButton>(R.id.btnNext).setOnClickListener {
            handlePostSubmission()
        }
    }

    private fun openMediaPicker(launcher: ActivityResultLauncher<Intent>, mimeType: String) {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = mimeType
        }
        launcher.launch(intent)
    }

    private fun handlePostSubmission() {
        val context = requireContext()
        val title = "Default Title"
        val desc = binding.postContentCard.findViewById<EditText>(R.id.edit_text_post).text.toString().trim()
        val loc = "Current Location"
        val locRating = 5

        if (desc.isEmpty()) {
            Toast.makeText(context, "Please enter a description.", Toast.LENGTH_SHORT).show()
            return
        }

        val mediaUri = selectedPhotoUri ?: selectedVideoUri
        val mimeType = if (selectedPhotoUri != null) "image/*" else if (selectedVideoUri != null) "video/*" else null

        uploadPost(title, desc, loc, locRating, mediaUri, mimeType)
    }

    private fun uploadPost(title: String, desc: String, loc: String, locRating: Int, mediaUri: Uri?, mimeType: String?) {
        val context = requireContext()

        val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val descPart = desc.toRequestBody("text/plain".toMediaTypeOrNull())
        val locPart = loc.toRequestBody("text/plain".toMediaTypeOrNull())

        var mediaPart: MultipartBody.Part? = null
        val partName = if (selectedPhotoUri != null) "img" else "vid"

        if (mediaUri != null && mimeType != null) {
            try {
                val contentResolver = context.contentResolver
                val mimeTypeFinal = contentResolver.getType(mediaUri) ?: "application/octet-stream"

                var fileName = "upload_file"
                val cursor = contentResolver.query(mediaUri, null, null, null, null)
                cursor?.use {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && it.moveToFirst()) {
                        fileName = it.getString(nameIndex)
                    }
                }

                if (!fileName.contains(".")) {
                    val ext = when {
                        mimeTypeFinal.contains("jpeg") -> "jpg"
                        mimeTypeFinal.contains("png") -> "png"
                        mimeTypeFinal.contains("mp4") -> "mp4"
                        else -> "bin"
                    }
                    fileName += ".$ext"
                }

                val inputStream = contentResolver.openInputStream(mediaUri)
                if (inputStream != null) {
                    val bytes = inputStream.readBytes()
                    inputStream.close()
                    val requestBody = bytes.toRequestBody(mimeTypeFinal.toMediaTypeOrNull())
                    mediaPart = MultipartBody.Part.createFormData(partName, fileName, requestBody)
                } else {
                    Toast.makeText(context, "Failed to open file stream.", Toast.LENGTH_SHORT).show()
                    return
                }

            } catch (e: Exception) {
                Toast.makeText(context, "Error reading file: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
                return
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val api = ApiClient.getTokenService(requireContext())
                val response = api.createPost(
                    title = titlePart,
                    desc = descPart,
                    loc = locPart,
                    loc_rating = locRating,
                    img = if (partName == "img") mediaPart else null,
                    vid = if (partName == "vid") mediaPart else null
                )

                if (response.isSuccessful) {
                    Toast.makeText(context, "Post Created Successfully!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.setFragmentResult(
                        POST_CREATION_REQUEST_KEY,
                        Bundle().apply { putBoolean(REFRESH_FEED_KEY, true) }
                    )
                    parentFragmentManager.popBackStack()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown API error"
                    Toast.makeText(context, "Failed: ${response.code()} — $errorBody", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network Error: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
