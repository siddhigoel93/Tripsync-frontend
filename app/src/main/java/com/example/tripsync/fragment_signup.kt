package com.example.tripsync

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class fragment_signup : Fragment() {

    private lateinit var etPassword: EditText
    private lateinit var etConfirm: EditText
    private lateinit var ivTogglePass: ImageView
    private lateinit var ivToggleConfirm: ImageView
    private lateinit var btnSignUp: TextView
    private var isPasswordVisible = false
    private var isConfirmVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_signup, container, false)

        etPassword = view.findViewById(R.id.etPassword)
        etConfirm = view.findViewById(R.id.etConfirm)
        ivTogglePass = view.findViewById(R.id.ivTogglePass)
        ivToggleConfirm = view.findViewById(R.id.ivToggleConfirm)
        btnSignUp = view.findViewById(R.id.btnSignUp)

        setupPasswordValidation()
        setupVisibilityToggles()

        return view
    }

    private fun setupPasswordValidation() {
        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validatePasswords()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        etConfirm.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validatePasswords()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun validatePasswords() {
        val password = etPassword.text.toString()
        val confirm = etConfirm.text.toString()

        val passwordValid = password.length >= 8 &&
                password.any { it.isDigit() } &&
                password.any { !it.isLetterOrDigit() }

        if (!passwordValid && password.isNotEmpty()) {
            etPassword.error = "Your password must contain 8 letters, a number and a special character"
        } else {
            etPassword.error = null
        }

        if (passwordValid && confirm.isNotEmpty() && password != confirm) {
            etConfirm.error = "Both passwords are different"
        } else {
            etConfirm.error = null
        }
    }

    private fun setupVisibilityToggles() {
        ivTogglePass.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility(etPassword, ivTogglePass, isPasswordVisible)
        }

        ivToggleConfirm.setOnClickListener {
            isConfirmVisible = !isConfirmVisible
            togglePasswordVisibility(etConfirm, ivToggleConfirm, isConfirmVisible)
        }
    }

    private fun togglePasswordVisibility(editText: EditText, icon: ImageView, visible: Boolean) {
        if (visible) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            icon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_visibility))
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            icon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_visibility_off))
        }
        editText.setSelection(editText.text.length)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            fragment_signup().apply {
                arguments = Bundle().apply {
                    putString("param1", param1)
                    putString("param2", param2)
                }
            }
    }
}
