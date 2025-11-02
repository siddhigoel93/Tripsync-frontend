package com.example.tripsync.ui

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.tripsync.ProfileDetails.ProfileViewModel
import com.example.tripsync.R
import com.example.tripsync.databinding.FragmentPersonalDetailsBinding
import java.util.Calendar

class FragmentPersonalDetails : Fragment() {

    private var _binding: FragmentPersonalDetailsBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by activityViewModels()

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
            val isEmoji = (cp in 0x1F600..0x1F64F) ||
                    (cp in 0x1F300..0x1F5FF) ||
                    (cp in 0x1F680..0x1F6FF) ||
                    (cp in 0x2600..0x26FF) ||
                    (cp in 0x2700..0x27BF)
            if (type != Character.SURROGATE.toInt() &&
                type != Character.OTHER_SYMBOL.toInt() &&
                !isEmoji
            ) out.appendCodePoint(cp)
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
            val digitsOnly = s.filter { it.isDigit() }.toString()
            if (digitsOnly != s.toString()) {
                binding.r74p8jg2m2qd.removeTextChangedListener(this)
                binding.r74p8jg2m2qd.setText(digitsOnly)
                binding.r74p8jg2m2qd.setSelection(digitsOnly.length.coerceAtLeast(0))
                binding.r74p8jg2m2qd.addTextChangedListener(this)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPersonalDetailsBinding.inflate(inflater, container, false)

        binding.r56rex9ns33d.setOnClickListener { pickImage.launch("image/*") }

        binding.ruy6dpr196f.filters = arrayOf(noEmojiFilter, nameFilter, InputFilter.LengthFilter(30))
        binding.rcuxd094pa8j.filters = arrayOf(noEmojiFilter, nameFilter, InputFilter.LengthFilter(30))
        binding.rmwn47x6u6m.filters  = arrayOf(noEmojiFilter, InputFilter.LengthFilter(400))
        binding.r74p8jg2m2qd.addTextChangedListener(phoneWatcher)

        binding.rcousyd38th8.setOnClickListener { openDatePicker() }

        binding.rfcvab5pvu3c.setOnClickListener { selectGender("male") }
        binding.rawr3vr06wif.setOnClickListener { selectGender("female") }
        binding.rk3dh7nhmde.setOnClickListener { selectGender("others") }

        binding.btnNext.setOnClickListener { onNextClicked() }

        binding.ruy6dpr196f.setText(profileViewModel.firstName)
        binding.rcuxd094pa8j.setText(profileViewModel.lastName)
        binding.r74p8jg2m2qd.setText(profileViewModel.phoneNumber)
        binding.rcousyd38th8.setText(profileViewModel.dob)
        binding.rmwn47x6u6m.setText(profileViewModel.aboutMe)
        profileViewModel.imageUri?.let { binding.r56rex9ns33d.setImageURI(it) }
        selectGender(profileViewModel.gender)

        return binding.root
    }

    private fun onNextClicked() {
        val first = binding.ruy6dpr196f.text?.toString() ?: ""
        val last  = binding.rcuxd094pa8j.text?.toString() ?: ""
        val phoneDigits = binding.r74p8jg2m2qd.text?.toString() ?: ""
        val dob   = binding.rcousyd38th8.text?.toString() ?: ""
        val about = binding.rmwn47x6u6m.text?.toString() ?: ""

        if (first.isBlank()) {
            Toast.makeText(requireContext(), "First name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (last.isBlank()) {
            Toast.makeText(requireContext(), "Last name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (first.trim().isEmpty()) {
            Toast.makeText(requireContext(), "First name cannot be only spaces", Toast.LENGTH_SHORT).show()
            return
        }
        if (last.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Last name cannot be only spaces", Toast.LENGTH_SHORT).show()
            return
        }

        if (phoneDigits.isEmpty()) {
            Toast.makeText(requireContext(), "Phone number cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (phoneDigits.length > 15) {
            Toast.makeText(requireContext(), "Phone number cannot exceed 15 digits", Toast.LENGTH_SHORT).show()
            return
        }

        if (dob.isBlank()) {
            Toast.makeText(requireContext(), "Please select your date of birth", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isAtLeast13(dob)) {
            Toast.makeText(requireContext(), "You must be at least 13 years old", Toast.LENGTH_SHORT).show()
            return
        }

        profileViewModel.firstName = first.trim()
        profileViewModel.lastName  = last.trim()
        profileViewModel.phoneNumber = phoneDigits
        profileViewModel.dob       = dob
        profileViewModel.gender    = selectedGender
        profileViewModel.aboutMe   = about
        profileViewModel.imageUri  = pickedImageUri

        requireContext()
            .getSharedPreferences("profile_local", 0)
            .edit()
            .putString("self_phone_digits", phoneDigits)
            .apply()

        findNavController().navigate(R.id.emergencyFragment)
    }

    private fun isAtLeast13(dobIso: String): Boolean {
        val parts = dobIso.split("-")
        if (parts.size != 3) return false
        val y = parts[0].toIntOrNull() ?: return false
        val m = parts[1].toIntOrNull() ?: return false
        val d = parts[2].toIntOrNull() ?: return false

        val today = Calendar.getInstance()
        val dob = Calendar.getInstance().apply {
            set(Calendar.YEAR, y)
            set(Calendar.MONTH, (m - 1).coerceIn(0, 11))
            set(Calendar.DAY_OF_MONTH, d.coerceIn(1, 31))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        val thisYearBirthday = Calendar.getInstance().apply {
            set(Calendar.YEAR, today.get(Calendar.YEAR))
            set(Calendar.MONTH, dob.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, dob.get(Calendar.DAY_OF_MONTH))
        }
        if (today.before(thisYearBirthday)) age -= 1
        return age >= 13
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
        DatePickerDialog(
            ctx,
            { _, y, m, d ->
                if (!isAdded || _binding == null) return@DatePickerDialog
                val mm = (m + 1).toString().padStart(2, '0')
                val dd = d.toString().padStart(2, '0')
                binding.rcousyd38th8.setText("$y-$mm-$dd")
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.r74p8jg2m2qd.removeTextChangedListener(phoneWatcher)
        _binding = null
    }
}
