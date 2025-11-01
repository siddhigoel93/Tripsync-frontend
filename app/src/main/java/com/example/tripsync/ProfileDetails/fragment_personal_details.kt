package com.example.tripsync.ui

import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.models.CreateProfileRequest
import com.example.tripsync.databinding.FragmentPersonalDetailsBinding
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class FragmentPersonalDetails : Fragment() {

    private var _binding: FragmentPersonalDetailsBinding? = null
    private val binding get() = _binding!!

    private var selectedGender: String = "male"
    private var pickedImageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (!isAdded || _binding == null) return@registerForActivityResult
        if (uri != null) {
            pickedImageUri = uri
            binding.r56rex9ns33d.setImageURI(uri)
        }
    }

    private val noEmojiFilter = InputFilter { source, start, end, _, _, _ ->
        val out = StringBuilder()
        var i = start
        while (i < end) {
            val cp = Character.codePointAt(source, i)
            val type = Character.getType(cp)
            val isEmoji = (cp in 0x1F600..0x1F64F) || (cp in 0x1F300..0x1F5FF) ||
                    (cp in 0x1F680..0x1F6FF) || (cp in 0x2600..0x26FF) || (cp in 0x2700..0x27BF)
            if (type != Character.SURROGATE.toInt() && type != Character.OTHER_SYMBOL.toInt() && !isEmoji) {
                out.appendCodePoint(cp)
            }
            i += Character.charCount(cp)
        }
        out.toString()
    }

    private val nameFilter = InputFilter { source, start, end, _, _, _ ->
        val allowed = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ "
        buildString {
            for (i in start until end) {
                val ch = source[i]
                if (allowed.indexOf(ch) >= 0) append(ch)
            }
        }
    }

    private val phoneWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            if (!isAdded || _binding == null) return
            if (s == null) return
            var t = s.filter { it.isDigit() || it == '+' }.toString()
            t = "+" + t.replace("+", "")
            if (t != s.toString()) {
                binding.r74p8jg2m2qd.removeTextChangedListener(this)
                binding.r74p8jg2m2qd.setText(t)
                binding.r74p8jg2m2qd.setSelection(t.length.coerceAtLeast(0))
                binding.r74p8jg2m2qd.addTextChangedListener(this)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPersonalDetailsBinding.inflate(inflater, container, false)

        binding.r56rex9ns33d.setOnClickListener { pickImage.launch("image/*") }

        binding.ruy6dpr196f.filters = arrayOf(noEmojiFilter, nameFilter, InputFilter.LengthFilter(30))
        binding.rcuxd094pa8j.filters = arrayOf(noEmojiFilter, nameFilter, InputFilter.LengthFilter(30))
        binding.rmwn47x6u6m.filters = arrayOf(noEmojiFilter, InputFilter.LengthFilter(400))
        binding.r74p8jg2m2qd.addTextChangedListener(phoneWatcher)

        binding.rcousyd38th8.setOnClickListener { openDatePicker() }

        binding.rfcvab5pvu3c.setOnClickListener { selectGender("male") }
        binding.rawr3vr06wif.setOnClickListener { selectGender("female") }
        binding.rk3dh7nhmde.setOnClickListener { selectGender("others") }

        binding.btnNext.setOnClickListener { submitProfile() }

        return binding.root
    }

    private fun selectGender(value: String) {
        selectedGender = value
        if (!isAdded || _binding == null) return
        binding.rfcvab5pvu3c.alpha = if (value == "male") 1f else 0.6f
        binding.rawr3vr06wif.alpha = if (value == "female") 1f else 0.6f
        binding.rk3dh7nhmde.alpha = if (value == "others") 1f else 0.6f
    }

    private fun openDatePicker() {
        val ctx = context ?: return
        val cal = Calendar.getInstance()
        DatePickerDialog(ctx, { _, y, m, d ->
            if (!isAdded || _binding == null) return@DatePickerDialog
            val mm = (m + 1).toString().padStart(2, '0')
            val dd = d.toString().padStart(2, '0')
            binding.rcousyd38th8.setText("$y-$mm-$dd")
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun getToken(): String? {
        val ctx = context ?: return null
        val sp: SharedPreferences = ctx.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return sp.getString("access_token", null)
    }

    private suspend fun toMultipart(uri: Uri): MultipartBody.Part? = withContext(Dispatchers.IO) {
        try {
            val ctx = context ?: return@withContext null
            val cr = ctx.contentResolver
            val name = (getFileName(uri) ?: "profile.jpg").take(80)
            val temp = File(ctx.cacheDir, name)
            cr.openInputStream(uri)?.use { input ->
                FileOutputStream(temp).use { out -> input.copyTo(out) }
            } ?: return@withContext null
            val mime = cr.getType(uri) ?: "image/jpeg"
            val body = temp.asRequestBody(mime.toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", temp.name, body)
        } catch (_: CancellationException) {
            null
        } catch (_: Throwable) {
            null
        }
    }

    private fun getFileName(uri: Uri): String? {
        return try {
            val ctx = context ?: return null
            var result: String? = null
            val cursor: Cursor? = ctx.contentResolver.query(uri, null, null, null, null)
            if (cursor != null) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) result = cursor.getString(nameIndex)
                cursor.close()
            }
            result ?: uri.lastPathSegment
        } catch (_: Throwable) {
            null
        }
    }

    private fun submitProfile() {
        if (!isAdded || _binding == null) return

        val first = binding.ruy6dpr196f.text?.toString()?.trim().orEmpty()
        val last = binding.rcuxd094pa8j.text?.toString()?.trim().orEmpty()
        val code = binding.rky6hlaitpeg.text?.toString()?.trim().orEmpty()
        val phoneRaw = binding.r74p8jg2m2qd.text?.toString()?.trim().orEmpty()
        val phone = if (phoneRaw.startsWith("+")) phoneRaw else code + phoneRaw
        val dob = binding.rcousyd38th8.text?.toString()?.trim().orEmpty()
        val about = binding.rmwn47x6u6m.text?.toString()?.trim().orEmpty()

        if (first.isEmpty()) return
        if (last.isEmpty()) return
        if (!phone.startsWith("+")) return
        if (!dob.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) return

        val token = getToken() ?: return
        val bearer = "Bearer $token"

        val req = CreateProfileRequest(
            fname = first,
            lname = last,
            phone_number = phone,
            date = dob,
            gender = selectedGender,
            bio = about,
            bgroup = "",
            allergies = "",
            medical = "",
            ename = "",
            enumber = "",
            erelation = "",
            preference = ""
        )

        binding.btnNext.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val service = ApiClient.getAuthService(requireContext())
                val created = withContext(Dispatchers.IO) { service.createProfile(bearer, req) }
                Log.d("CreateProfile", "Code: ${created.code()}")
                Log.d("CreateProfile", "Success: ${created.isSuccessful}")
                Log.d("CreateProfile", "Body: ${created.body()}")
                Log.d("CreateProfile", "Error: ${created.errorBody()?.string()}")

                if (created.isSuccessful) {
                    pickedImageUri?.let { uri ->
                        toMultipart(uri)?.let { part ->
                            withContext(Dispatchers.IO) { service.uploadProfileImage(bearer, part) }
                        }
                    }
                }
            } catch (_: CancellationException) {
            } catch (_: Throwable) {
            } finally {
                if (isAdded && _binding != null) {
                    binding.btnNext.isEnabled = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.r74p8jg2m2qd.removeTextChangedListener(phoneWatcher)
        _binding = null
    }
}
