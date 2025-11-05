package com.example.tripsync.ui

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.tripsync.R
import com.example.tripsync.ProfileDetails.ProfileViewModel
import com.example.tripsync.databinding.FragmentPersonalDetailsBinding
import java.util.Calendar


class FragmentPersonalDetails : Fragment() {

    private var _binding: FragmentPersonalDetailsBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by activityViewModels()

    private var selectedGender: String? = null

    private var pickedImageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (!isAdded || _binding == null) return@registerForActivityResult
        if (uri != null) {
            pickedImageUri = uri
            binding.profileImageView.setImageURI(uri)
        }
    }

    private val noEmojiFilter = InputFilter { source, start, end, _, _, _ ->
        val out = StringBuilder()
        var i = start
        while (i < end) {
            val cp = Character.codePointAt(source, i)
            val type = Character.getType(cp)
            val isEmoji = (cp in 0x1F600..0x1F64F) || (cp in 0x1F300..0x1F5FF) || (cp in 0x1F680..0x1F6FF) || (cp in 0x2600..0x26FF) || (cp in 0x2700..0x27BF)
            if (type != Character.SURROGATE.toInt() && type != Character.OTHER_SYMBOL.toInt() && !isEmoji) out.appendCodePoint(cp)
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

    private val digitsOnlyWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            if (!isAdded || _binding == null) return
            if (s == null) return
            var t = s.filter { it.isDigit() }.toString()
            if (t.length > 10) t = t.substring(0, 10)
            if (t != s.toString()) {
                binding.etPhoneNumber.removeTextChangedListener(this)
                binding.etPhoneNumber.setText(t)
                binding.etPhoneNumber.setSelection(t.length.coerceAtLeast(0))
                binding.etPhoneNumber.addTextChangedListener(this)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPersonalDetailsBinding.inflate(inflater, container, false)

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                100)
        }


        binding.profileImageView.setOnClickListener { pickImage.launch("image/*") }

        binding.etFirstName.filters = arrayOf(noEmojiFilter, nameFilter, InputFilter.LengthFilter(30))
        binding.etLastName.filters = arrayOf(noEmojiFilter, nameFilter, InputFilter.LengthFilter(30))
        binding.etAboutMe.filters = arrayOf(noEmojiFilter, InputFilter.LengthFilter(500))
        binding.etPhoneNumber.addTextChangedListener(digitsOnlyWatcher)

        binding.etDOB.setOnClickListener { openDatePicker() }

        binding.tvGenderMale.setOnClickListener { selectGender("male") }
        binding.tvGenderFemale.setOnClickListener { selectGender("female") }
        binding.tvGenderOthers.setOnClickListener { selectGender("others") }

        binding.btnNext.setOnClickListener { onNextClicked() }

        binding.etFirstName.setText(profileViewModel.firstName)
        binding.etLastName.setText(profileViewModel.lastName)
        binding.etPhoneNumber.setText(profileViewModel.phoneNumber)
        binding.etDOB.setText(profileViewModel.dob)
        binding.etAboutMe.setText(profileViewModel.aboutMe)
        profileViewModel.imageUri?.let { binding.profileImageView.setImageURI(it) }
        selectGender(profileViewModel.gender)

        return binding.root
    }

    private fun onNextClicked() {
        binding.firstnameError.visibility = View.GONE
        binding.lastnameError.visibility = View.GONE
        binding.phoneNumError.visibility = View.GONE
        binding.dobError.visibility = View.GONE
        binding.aboutError.visibility = View.GONE
        binding.genderError.visibility = View.GONE

        val first = binding.etFirstName.text?.toString()?.trim().orEmpty()
        val last = binding.etLastName.text?.toString()?.trim().orEmpty()
        val phoneDigits = binding.etPhoneNumber.text?.toString()?.filter { it.isDigit() }.orEmpty()
        val dob = binding.etDOB.text?.toString()?.trim().orEmpty()
        val about = binding.etAboutMe.text?.toString()?.trim().orEmpty()

        var hasError = false

        if (first.isEmpty()) {
            binding.firstnameError.text = "Required"
            binding.firstnameError.visibility = View.VISIBLE
            hasError = true
        }
        if (last.isEmpty()) {
            binding.lastnameError.text = "Required"
            binding.lastnameError.visibility = View.VISIBLE
            hasError = true
        }
        if (phoneDigits.isEmpty()) {
            binding.phoneNumError.text = "Required"
            binding.phoneNumError.visibility = View.VISIBLE
            hasError = true
        } else if (phoneDigits.length > 10 || phoneDigits.length < 10) {
            binding.phoneNumError.text = "Must be 10 digits"
            binding.phoneNumError.visibility = View.VISIBLE
            hasError = true
        }
        if (dob.isEmpty()) {
            binding.dobError.text = "Required"
            binding.dobError.visibility = View.VISIBLE
            hasError = true
        } else if (!isAtLeast13(dob)) {
            binding.dobError.text = "Must be 13+"
            binding.dobError.visibility = View.VISIBLE
            hasError = true
        }
        if (about.isEmpty()) {
            binding.aboutError.text = "Required"
            binding.aboutError.visibility = View.VISIBLE
            hasError = true
        }

        if (selectedGender.isNullOrBlank()) {
            binding.genderError.text = "Select gender"
            binding.genderError.visibility = View.VISIBLE
            hasError = true
        }


        if (hasError) return

        profileViewModel.firstName = first
        profileViewModel.lastName = last
        profileViewModel.phoneNumber = phoneDigits
        profileViewModel.personalPhoneDigits = phoneDigits
        profileViewModel.dob = dob
        profileViewModel.gender = selectedGender ?: ""
        profileViewModel.aboutMe = about
        profileViewModel.imageUri = pickedImageUri

        val sharedPref = requireContext().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("userName", first)
            putString("userAvatarUrl", pickedImageUri?.toString())
            apply()
        }

        findNavController().navigate(R.id.emergencyFragment)
    }


    private fun selectGender(value: String?) {
        selectedGender = value
        if (!isAdded || _binding == null) return

        binding.tvGenderMale.isSelected = value == "male"
        binding.tvGenderFemale.isSelected = value == "female"
        binding.tvGenderOthers.isSelected = value == "others"
    }




    private fun openDatePicker() {
        val ctx = context ?: return
        val cal = Calendar.getInstance()
        DatePickerDialog(ctx, { _, y, m, d ->
            if (!isAdded || _binding == null) return@DatePickerDialog
            val mm = (m + 1).toString().padStart(2, '0')
            val dd = d.toString().padStart(2, '0')
            binding.etDOB.setText("$y-$mm-$dd")
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding.etPhoneNumber.removeTextChangedListener(digitsOnlyWatcher)
        _binding = null
    }
}
