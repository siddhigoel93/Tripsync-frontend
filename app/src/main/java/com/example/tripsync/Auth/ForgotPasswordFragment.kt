package com.example.tripsync.Auth

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.models.EmailRequest
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton

class ForgotPasswordFragment : Fragment() {

    private lateinit var usernameError: TextView
    private lateinit var email: EditText
    lateinit var verify: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_forgot_password, container, false)

        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        email = view.findViewById(R.id.etEmail)
         verify = view.findViewById(R.id.btn)
        val backToLogin = view.findViewById<TextView>(R.id.backtologin)
        usernameError = view.findViewById(R.id.usernameError)

        backToLogin.paintFlags = backToLogin.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        backToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_forgotPasswordFragment_to_loginFragment)
        }

        email.setOnFocusChangeListener { _, hasFocus ->
            email.setBackgroundResource(if (hasFocus) R.drawable.selected_input else R.drawable.input_border)
        }

        verify.setOnClickListener {
            usernameError.visibility = View.GONE
            email.setBackgroundResource(R.drawable.input_border)

            verify.isEnabled = false
            verify.text = "Sending OTP..."


            val emailText = email.text.toString().trim()
            if (emailText.isEmpty()) {
                showFieldError("Email cannot be empty")
                return@setOnClickListener
            }

            sendResetOtp(emailText, view)
        }

        return view
    }

    private fun sendResetOtp(emailText: String, view: View) {
        lifecycleScope.launch {
            try {
                val authService = ApiClient.getAuthService(requireContext())
                val request = EmailRequest(emailText)

                val response = authService.requestPasswordReset(request)

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "OTP sent successfully!", Toast.LENGTH_SHORT).show()
                    verify.text = "OTP sent"
                    val bundle = Bundle().apply { putString("email", emailText) }
                    view.findNavController().navigate(R.id.action_forgotPasswordFragment_to_resetOTP, bundle)
                } else {
                    showFieldError("No user found with this email")
                    verify.isEnabled = true
                    verify.text = "Send OTP"
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to send OTP. Please try again.", Toast.LENGTH_SHORT).show()
                verify.isEnabled = true
                verify.text = "Send OTP"
            }
        }
    }

    private fun showFieldError(message: String) {
        usernameError.visibility = View.VISIBLE
        usernameError.text = message
        email.setBackgroundResource(R.drawable.wrong_input)
    }
}
