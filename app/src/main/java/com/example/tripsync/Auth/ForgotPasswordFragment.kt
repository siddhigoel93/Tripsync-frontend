package com.example.tripsync.Auth

import android.graphics.Paint
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.models.EmailRequest
import kotlinx.coroutines.launch

class ForgotPasswordFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_forgot_password, container, false)

        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )

        val email = view.findViewById<EditText>(R.id.etEmail)
        val verify = view.findViewById<Button>(R.id.btn)
        val backToLogin = view.findViewById<TextView>(R.id.backtologin)
        val usernameError = view.findViewById<TextView>(R.id.usernameError)

        backToLogin.paintFlags = backToLogin.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        backToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_forgotPasswordFragment_to_loginFragment)
        }

        email.setOnFocusChangeListener { _, hasFocus ->
            email.setBackgroundResource(
                if (hasFocus) R.drawable.selected_input else R.drawable.input_border
            )
        }

        verify.setOnClickListener {
            usernameError.visibility = View.GONE
            email.setBackgroundResource(R.drawable.input_border)

            val emailText = email.text.toString().trim()

            if (emailText.isEmpty()) {
                usernameError.visibility = View.VISIBLE
                usernameError.text = "(Email cannot be empty)"
                email.setBackgroundResource(R.drawable.wrong_input)
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                usernameError.visibility = View.VISIBLE
                usernameError.text = "(Invalid email format)"
                email.setBackgroundResource(R.drawable.wrong_input)
                return@setOnClickListener
            }

            // ✅ Call API to send OTP
            sendResetOtp(emailText, view)
        }

        return view
    }

    private fun sendResetOtp(email: String,  view: View) {


        lifecycleScope.launch {
            try {
                val authService = ApiClient.getAuthService(requireContext())
                val response = authService.requestPasswordReset(EmailRequest(email))


                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "OTP sent successfully!", Toast.LENGTH_SHORT).show()
                    val bundle = Bundle().apply { putString("email", email) }
                    view.findNavController().navigate(R.id.action_forgotPasswordFragment_to_resetOTP, bundle)
                } else {
                    val error = response.errorBody()?.string()
                    Toast.makeText(requireContext(), "Failed to send OTP :  $error", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
