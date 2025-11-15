package com.example.tripsync.community

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.databinding.FragmentCreatePostBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class CreatePostFragment : Fragment() {

    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!

    private var selectedMediaUri: Uri? = null
    private var selectedMediaType: MediaType? = null
    private var locationRating: Int = 5

    enum class MediaType {
        PHOTO, VIDEO
    }

    companion object {
        const val POST_CREATION_REQUEST_KEY = "post_creation_request"
        const val REFRESH_FEED_KEY = "refresh_feed_key"
    }

    private val photoPicker: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedMediaUri = uri
                    selectedMediaType = MediaType.PHOTO
                    showMediaPreview()
                }
            }
        }

    private val videoPicker: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedMediaUri = uri
                    selectedMediaType = MediaType.VIDEO
                    showMediaPreview()
                }
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
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupUserProfile()
        setupListeners()
        setupRatingStars()
    }

    private fun setupRatingStars() {
        val stars = listOf(
            binding.postContentCard.findViewById<ImageView>(R.id.star1),
            binding.postContentCard.findViewById<ImageView>(R.id.star2),
            binding.postContentCard.findViewById<ImageView>(R.id.star3),
            binding.postContentCard.findViewById<ImageView>(R.id.star4),
            binding.postContentCard.findViewById<ImageView>(R.id.star5)
        )

        stars.forEachIndexed { index, star ->
            star.setOnClickListener {
                locationRating = index + 1
                updateStars(stars, locationRating)
            }
        }
    }

    private fun updateStars(stars: List<ImageView>, rating: Int) {
        stars.forEachIndexed { index, star ->
            if (index < rating) {
                star.setImageResource(R.drawable.ic_star_filled)
            } else {
                star.setImageResource(R.drawable.ic_star_empty)
            }
        }
    }

    private fun showMediaPreview() {
        val mediaPreviewContainer = binding.postContentCard.findViewById<View>(R.id.media_preview_container)
        val previewImage = binding.postContentCard.findViewById<ImageView>(R.id.preview_image)
        val previewVideo = binding.postContentCard.findViewById<VideoView>(R.id.preview_video)

        mediaPreviewContainer.visibility = View.VISIBLE

        when (selectedMediaType) {
            MediaType.PHOTO -> {
                previewImage.visibility = View.VISIBLE
                previewVideo.visibility = View.GONE
                Glide.with(this)
                    .load(selectedMediaUri)
                    .into(previewImage)
            }
            MediaType.VIDEO -> {
                previewImage.visibility = View.GONE
                previewVideo.visibility = View.VISIBLE
                previewVideo.setVideoURI(selectedMediaUri)
                previewVideo.setOnPreparedListener { mp ->
                    mp.isLooping = true
                    previewVideo.start()
                }
            }
            null -> {
                mediaPreviewContainer.visibility = View.GONE
            }
        }
    }

    private fun clearMediaSelection() {
        selectedMediaUri = null
        selectedMediaType = null

        val mediaPreviewContainer = binding.postContentCard.findViewById<View>(R.id.media_preview_container)
        val previewVideo = binding.postContentCard.findViewById<VideoView>(R.id.preview_video)

        previewVideo.stopPlayback()
        mediaPreviewContainer.visibility = View.GONE

        Toast.makeText(context, "Media removed", Toast.LENGTH_SHORT).show()
    }

    private fun setupUserProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            val sharedPref = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

            val fname = sharedPref.getString("fname", "")
            val lname = sharedPref.getString("lname", "")
            val fullName = listOfNotNull(fname, lname)
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .ifEmpty { "Unknown User" }

            val userNameTextView = binding.postContentCard.findViewById<TextView>(R.id.user_name)
            userNameTextView.text = fullName

            val avatarUrl = sharedPref.getString("userAvatarUrl", null)
            val profileAvatarImageView = binding.postContentCard.findViewById<ShapeableImageView>(R.id.profile_avatar)

            if (!avatarUrl.isNullOrEmpty()) {
                Glide.with(this@CreatePostFragment)
                    .load(avatarUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(profileAvatarImageView)
            } else {
                profileAvatarImageView.setImageResource(R.drawable.placeholder_image)
            }
        }
    }

    private fun setupToolbar() {
        val toolbar = binding.appBar.findViewById<Toolbar>(R.id.toolbar)

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

        binding.postContentCard.findViewById<View>(R.id.btn_remove_media)?.setOnClickListener {
            clearMediaSelection()
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
        val title = binding.postContentCard.findViewById<EditText>(R.id.ettitle).text.toString().trim()
        val desc = binding.postContentCard.findViewById<EditText>(R.id.edit_text_post).text.toString().trim()
        val loc = "Current Location"

        if (title.isEmpty()) {
            Toast.makeText(context, "Please enter a title.", Toast.LENGTH_SHORT).show()
            return
        }

        if (desc.isEmpty()) {
            Toast.makeText(context, "Please enter a caption.", Toast.LENGTH_SHORT).show()
            return
        }

        uploadPost(title, desc, loc, locationRating, selectedMediaUri, selectedMediaType)
    }

    private fun uploadPost(
        title: String,
        desc: String,
        loc: String,
        locRating: Int,
        mediaUri: Uri?,
        mediaType: MediaType?
    ) {
        val context = requireContext()

        val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val descPart = desc.toRequestBody("text/plain".toMediaTypeOrNull())
        val locPart = loc.toRequestBody("text/plain".toMediaTypeOrNull())

        viewLifecycleOwner.lifecycleScope.launch {
            var mediaPart: MultipartBody.Part? = null
            var tempFile: File? = null

            try {
                if (mediaUri != null && mediaType != null) {
                    tempFile = withContext(Dispatchers.IO) {
                        createTempFileFromUri(context, mediaUri, mediaType)
                    }

                    if (tempFile == null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Failed to process media file", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }

                    val mimeType = context.contentResolver.getType(mediaUri) ?: when (mediaType) {
                        MediaType.PHOTO -> "image/jpeg"
                        MediaType.VIDEO -> "video/mp4"
                    }

                    android.util.Log.d("CreatePostFragment", "Uploading ${mediaType.name}: ${tempFile.name}, size: ${tempFile.length() / 1024}KB, mime: $mimeType")

                    val requestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
                    val partName = if (mediaType == MediaType.PHOTO) "img" else "vid"
                    mediaPart = MultipartBody.Part.createFormData(partName, tempFile.name, requestBody)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Uploading ${if (mediaType == MediaType.VIDEO) "video" else "photo"}...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                val api = ApiClient.getTokenService(requireContext())

                val response = withContext(Dispatchers.IO) {
                    api.createPost(
                        title = titlePart,
                        desc = descPart,
                        loc = locPart,
                        loc_rating = locRating,
                        img = if (mediaType == MediaType.PHOTO) mediaPart else null,
                        vid = if (mediaType == MediaType.VIDEO) mediaPart else null
                    )
                }

                tempFile?.delete()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Post Created Successfully!", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.setFragmentResult(
                            POST_CREATION_REQUEST_KEY,
                            Bundle().apply { putBoolean(REFRESH_FEED_KEY, true) }
                        )
                        parentFragmentManager.popBackStack()
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        android.util.Log.e("CreatePostFragment", "Upload failed: ${response.code()} — $errorBody")
                        Toast.makeText(context, "Failed: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                tempFile?.delete()
                android.util.Log.e("CreatePostFragment", "Upload error", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private suspend fun createTempFileFromUri(context: Context, uri: Uri, mediaType: MediaType): File? {
        return withContext(Dispatchers.IO) {
            try {
                val contentResolver = context.contentResolver
                val mimeType = contentResolver.getType(uri) ?: return@withContext null

                val extension = when {
                    mimeType.contains("jpeg") || mimeType.contains("jpg") -> ".jpg"
                    mimeType.contains("png") -> ".png"
                    mimeType.contains("gif") -> ".gif"
                    mimeType.contains("webp") -> ".webp"
                    mimeType.contains("mp4") -> ".mp4"
                    mimeType.contains("3gpp") || mimeType.contains("3gp") -> ".3gp"
                    mimeType.contains("webm") -> ".webm"
                    mimeType.contains("mpeg") -> ".mpeg"
                    mimeType.contains("quicktime") || mimeType.contains("mov") -> ".mov"
                    mimeType.contains("avi") -> ".avi"
                    mimeType.contains("mkv") -> ".mkv"
                    mediaType == MediaType.VIDEO -> ".mp4"
                    mediaType == MediaType.PHOTO -> ".jpg"
                    else -> ".tmp"
                }

                android.util.Log.d("CreatePostFragment", "Creating temp file with extension: $extension for MIME: $mimeType")

                val inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
                val tempFile = File.createTempFile("upload_", extension, context.cacheDir)

                FileOutputStream(tempFile).use { output ->
                    inputStream.copyTo(output)
                }
                inputStream.close()

                android.util.Log.d("CreatePostFragment", "Temp file created: ${tempFile.name}, size: ${tempFile.length() / 1024}KB")

                tempFile
            } catch (e: Exception) {
                android.util.Log.e("CreatePostFragment", "Error creating temp file", e)
                null
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val previewVideo = binding.postContentCard.findViewById<VideoView>(R.id.preview_video)
        previewVideo?.stopPlayback()
        _binding = null
    }
}