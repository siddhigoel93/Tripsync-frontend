package com.example.tripsync.ProfileDetails

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.tripsync.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class EmergencyFragment : Fragment(R.layout.fragment_emergency) {

    private val vm: ProfileViewModel by activityViewModels()

    private lateinit var inputBloodGroup: EditText
    private lateinit var inputAllergies: EditText
    private lateinit var inputMedicalHistory: EditText
    private lateinit var inputContactName: EditText
    private lateinit var inputContactPhone: EditText
    private lateinit var inputRelationship: EditText
    private lateinit var btnNext: MaterialButton
    private lateinit var btnPrev: MaterialButton

    private val noEmojiFilter = InputFilter { source, start, end, _, _, _ ->
        val out = StringBuilder()
        var i = start
        while (i < end) {
            val cp = Character.codePointAt(source, i)
            val type = Character.getType(cp)
            val emoji = (cp in 0x1F600..0x1F64F) ||
                    (cp in 0x1F300..0x1F5FF) ||
                    (cp in 0x1F680..0x1F6FF) ||
                    (cp in 0x2600..0x26FF) ||
                    (cp in 0x2700..0x27BF)
            if (type != Character.SURROGATE.toInt() && type != Character.OTHER_SYMBOL.toInt() && !emoji) {
                out.appendCodePoint(cp)
            }
            i += Character.charCount(cp)
        }
        out.toString()
    }

    private val digitsOnlyWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            if (s == null) return
            var digits = s.filter { it.isDigit() }.toString()
            if (digits.length > 15) digits = digits.substring(0, 15)
            if (digits != s.toString()) {
                inputContactPhone.removeTextChangedListener(this)
                inputContactPhone.setText(digits)
                inputContactPhone.setSelection(digits.length.coerceAtLeast(0))
                inputContactPhone.addTextChangedListener(this)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        inputBloodGroup = view.findViewById(R.id.inputBloodGroup)
        inputAllergies = view.findViewById(R.id.inputAllergies)
        inputMedicalHistory = view.findViewById(R.id.inputMedicalHistory)
        inputContactName = view.findViewById(R.id.inputContactName)
        inputContactPhone = view.findViewById(R.id.inputContactPhone)
        inputRelationship = view.findViewById(R.id.inputRelationship)
        btnNext = view.findViewById(R.id.btn)
        btnPrev = view.findViewById(R.id.btnPrevious)

        inputMedicalHistory.filters = arrayOf(noEmojiFilter)
        inputContactPhone.addTextChangedListener(digitsOnlyWatcher)

        inputMedicalHistory.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString() ?: return
                val words = text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
                if (words.size > 100) {
                    val trimmed = words.take(100).joinToString(" ")
                    inputMedicalHistory.removeTextChangedListener(this)
                    inputMedicalHistory.setText(trimmed)
                    inputMedicalHistory.setSelection(trimmed.length)
                    inputMedicalHistory.addTextChangedListener(this)
                    Toast.makeText(requireContext(), "Maximum 100 words allowed", Toast.LENGTH_SHORT).show()
                }
            }
        })

        inputBloodGroup.setOnClickListener { showSelectionDialog(R.array.blood_group_options, inputBloodGroup) }
        inputRelationship.setOnClickListener { showSelectionDialog(R.array.relationship_options, inputRelationship) }

        restoreFromViewModel()

        btnNext.setOnClickListener { onNextClicked() }
        btnPrev.setOnClickListener { findNavController().navigate(R.id.action_emergencyFragment_to_fragment_personal_details) }
    }

    private fun onNextClicked() {
        val bgroup = inputBloodGroup.text?.toString()?.trim().orEmpty()
        val allergies = inputAllergies.text?.toString()?.trim().orEmpty()
        val medical = inputMedicalHistory.text?.toString()?.trim().orEmpty()
        val ename = inputContactName.text?.toString()?.trim().orEmpty()
        val enumberDigits = inputContactPhone.text?.toString()?.trim().orEmpty()
        val erelation = inputRelationship.text?.toString()?.trim().orEmpty()

        if (bgroup.isEmpty()) {
            toast("Please select blood group")
            return
        }
        if (ename.isEmpty()) {
            toast("Contact name cannot be empty")
            return
        }
        if (enumberDigits.isEmpty()) {
            toast("Emergency number cannot be empty")
            return
        }
        if (enumberDigits.length > 15) {
            toast("Emergency number cannot exceed 15 digits")
            return
        }
        if (erelation.isEmpty()) {
            toast("Please select relationship")
            return
        }

        val selfDigits = vm.personalPhoneDigits
        if (selfDigits.isNotEmpty() && selfDigits == enumberDigits) {
            toast("Emergency number cannot be same as your own")
            return
        }

        vm.bgroup = bgroup
        vm.allergies = allergies
        vm.medical = medical
        vm.ename = ename
        vm.enumberRaw = enumberDigits
        vm.erelation = erelation

        findNavController().navigate(R.id.action_emergencyFragment_to_preferencesFragment)
    }

    private fun showSelectionDialog(optionsArrayId: Int, targetView: EditText) {
        val options = resources.getStringArray(optionsArrayId)
        val builder = MaterialAlertDialogBuilder(requireContext())
        val listView = ListView(requireContext()).apply {
            adapter = ArrayAdapter(requireContext(), R.layout.list_choice, android.R.id.text1, options)
            choiceMode = ListView.CHOICE_MODE_SINGLE
            val idx = options.indexOf(targetView.text.toString())
            if (idx != -1) setItemChecked(idx, true)
        }
        val dialog = builder.setView(listView).create()
        listView.setOnItemClickListener { _, _, position, _ ->
            targetView.setText(options[position])
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun restoreFromViewModel() {
        if (vm.bgroup.isNotEmpty()) inputBloodGroup.setText(vm.bgroup)
        if (vm.allergies.isNotEmpty()) inputAllergies.setText(vm.allergies)
        if (vm.medical.isNotEmpty()) inputMedicalHistory.setText(vm.medical)
        if (vm.ename.isNotEmpty()) inputContactName.setText(vm.ename)
        if (vm.enumberRaw.isNotEmpty()) inputContactPhone.setText(vm.enumberRaw.filter { it.isDigit() })
        if (vm.erelation.isNotEmpty()) inputRelationship.setText(vm.erelation)
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
