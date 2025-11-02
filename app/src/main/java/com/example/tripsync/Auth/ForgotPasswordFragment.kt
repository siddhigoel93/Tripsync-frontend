package com.example.tripsync.Auth

import android.graphics.Paint
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
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
        email.filters = arrayOf(EMAIL_EMOJI_FILTER)
         verify = view.findViewById(R.id.btn)
        val backToLogin = view.findViewById<TextView>(R.id.backtologin)
        usernameError = view.findViewById(R.id.usernameError)
        verify.text = "Next"

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


            val emailText = email.text.toString().trim()
            if (emailText.isEmpty()) {
                showFieldError("Email cannot be empty")
                return@setOnClickListener
            }

            verify.isEnabled = false
            verify.text = "Please wait..."
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
                    val bundle = Bundle().apply { putString("email", emailText) }
                    view.findNavController().navigate(R.id.action_forgotPasswordFragment_to_resetPasswordFragment, bundle)

                } else if (response.code() == 400) {
                    showFieldError("No user found with this email address.")
                    verify.isEnabled = true
                    verify.text = "Next"

                } else if (response.code() == 429) {
                    showFieldError("Too many attempts. Please try again later.")
                    verify.isEnabled = true
                    verify.text = "Next"

                } else {
                    showFieldError("Something went wrong. Please try again.")
                    verify.isEnabled = true
                    verify.text = "Next"
                }

            } catch (e: java.net.UnknownHostException) {
                showFieldError("No internet connection. Please check your network.")
                verify.isEnabled = true
                verify.text = "Next"
            } catch (e: java.net.SocketTimeoutException) {
                showFieldError("Request timed out. Please try again.")
                verify.isEnabled = true
                verify.text = "Next"
            } catch (e: Exception) {
                Log.e("ForgotPasswordFragment", "Error sending OTP", e)
                showFieldError("An unexpected error occurred. Please try again.")
                verify.isEnabled = true
                verify.text = "Next"
            }
        }
    }

    val EMAIL_EMOJI_FILTER = InputFilter { source, start, end, dest, dstart, dend ->
        for (i in start until end) {
            val type = Character.getType(source[i])
            if (type == Character.SURROGATE.toInt() ||
                type == Character.OTHER_SYMBOL.toInt() ||
                type == Character.NON_SPACING_MARK.toInt()
            ) {
                return@InputFilter ""
            }
        }
        return@InputFilter null
    }

    private fun showFieldError(message: String) {
        usernameError.visibility = View.VISIBLE
        usernameError.text = message
        email.setBackgroundResource(R.drawable.wrong_input)
    }
}
