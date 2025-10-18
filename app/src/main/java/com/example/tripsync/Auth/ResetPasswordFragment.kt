package com.example.tripsync.Auth

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.models.OTPVerifyRequest
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class ResetPasswordFragment : Fragment() {

    private lateinit var etPassword: EditText
    private lateinit var etPassword2: EditText
    private lateinit var btn: MaterialButton
    private lateinit var ivPasswordEye: ImageView
    private lateinit var ivPasswordEye2: ImageView
    private lateinit var passwordConfirmError: TextView
    private lateinit var signInText: TextView
    private lateinit var passwordField: View
    private lateinit var passwordConfirmField: View

    private var isPasswordVisible1 = false
    private var isPasswordVisible2 = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reset_password, container, false)

        etPassword = view.findViewById(R.id.etPassword)
        etPassword2 = view.findViewById(R.id.etPassword2)
        btn = view.findViewById(R.id.btn)
        ivPasswordEye = view.findViewById(R.id.PasswordEye)
        ivPasswordEye2 = view.findViewById(R.id.ivPasswordEye)
        passwordConfirmError = view.findViewById(R.id.passwordConfirmError)
        signInText = view.findViewById(R.id.signup)
        passwordField = view.findViewById(R.id.passwordField)
        passwordConfirmField = view.findViewById(R.id.passwordConfirmField)

        val icon1 = view.findViewById<ImageView>(R.id.icon1)
        val icon2 = view.findViewById<ImageView>(R.id.icon2)
        val icon3 = view.findViewById<ImageView>(R.id.icon3)
        val icon4 = view.findViewById<ImageView>(R.id.icon4)
        val rule1 = view.findViewById<TextView>(R.id.rule1)
        val rule2 = view.findViewById<TextView>(R.id.rule2)
        val rule3 = view.findViewById<TextView>(R.id.rule3)
        val rule4 = view.findViewById<TextView>(R.id.rule4)

        signInText.paintFlags = signInText.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        etPassword.setOnFocusChangeListener { _, hasFocus ->
            passwordField.setBackgroundResource(if (hasFocus) R.drawable.selected_input else R.drawable.input_border)
        }
        etPassword2.setOnFocusChangeListener { _, hasFocus ->
            passwordConfirmField.setBackgroundResource(if (hasFocus) R.drawable.selected_input else R.drawable.input_border)
        }

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = s.toString()
                updateRule(password.length >= 8, icon1, rule1)
                updateRule(password.any { it.isUpperCase() }, icon2, rule2)
                updateRule(password.any { "!@#$%^&*(),.?\":{}|<>".contains(it) }, icon3, rule3)
                updateRule(password.any { it.isDigit() }, icon4, rule4)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        setupPasswordToggle()
        setupButton()
        setupNavigation()

        return view
    }

    private fun setupPasswordToggle() {
        ivPasswordEye.setOnClickListener {
            isPasswordVisible1 = !isPasswordVisible1
            togglePasswordVisibility(etPassword, ivPasswordEye, isPasswordVisible1)
        }

        ivPasswordEye2.setOnClickListener {
            isPasswordVisible2 = !isPasswordVisible2
            togglePasswordVisibility(etPassword2, ivPasswordEye2, isPasswordVisible2)
        }
    }

    private fun togglePasswordVisibility(editText: EditText, eyeIcon: ImageView, isVisible: Boolean) {
        editText.transformationMethod = if (isVisible) null else PasswordTransformationMethod.getInstance()
        eyeIcon.setImageResource(if (isVisible) R.drawable.eye else R.drawable.eyedisable)
        editText.setSelection(editText.text.length)
    }

    private fun setupButton() {
        btn.setOnClickListener {
            val pass1 = etPassword.text.toString().trim()
            val pass2 = etPassword2.text.toString().trim()
            val email = arguments?.getString("email") ?: ""
            val otp = arguments?.getString("otp") ?: ""

            when {
                pass1.isEmpty() || pass2.isEmpty() -> {
                    passwordConfirmError.visibility = View.VISIBLE
                    passwordConfirmError.text = "Please fill in both fields"
                    passwordField.setBackgroundResource(R.drawable.wrong_input)
                    passwordConfirmField.setBackgroundResource(R.drawable.wrong_input)
                }
                pass1 != pass2 -> {
                    passwordConfirmError.visibility = View.VISIBLE
                    passwordConfirmError.text = "Passwords must be the same"
                    passwordConfirmField.setBackgroundResource(R.drawable.wrong_input)
                    passwordField.setBackgroundResource(R.drawable.input_border)
                }
                else -> {
                    passwordConfirmError.visibility = View.GONE
                    resetPassword(email, otp, pass1, pass2)
                }
            }
        }
    }

    private fun resetPassword(email: String, otp: String, newPassword: String, confirmPassword: String) {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getAuthService(requireContext())
                val request = OTPVerifyRequest(email, otp, newPassword, confirmPassword)
                val response = api.verifyOtp(request)

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Password reset successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_resetPasswordFragment_to_loginFragment)
                } else {
                    Toast.makeText(requireContext(), "Failed: Invalid OTP ", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupNavigation() {
        signInText.setOnClickListener {
            findNavController().navigate(R.id.action_resetPasswordFragment_to_loginFragment)
        }
    }

    private fun updateRule(isValid: Boolean, icon: ImageView, textView: TextView) {
        if (isValid) {
            icon.setColorFilter(Color.parseColor("#00C896"))
            textView.setTextColor(Color.parseColor("#00C896"))
        }
    }
}
