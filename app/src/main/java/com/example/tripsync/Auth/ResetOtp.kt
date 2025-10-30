package com.example.tripsync.Auth

import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.models.EmailRequest
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController
import com.example.tripsync.api.models.ResetPasswordOTPRequest
import org.json.JSONObject

class ResetOTP : Fragment() {

    private lateinit var hiddenEditText: EditText
    private lateinit var boxes: List<TextView>
    lateinit var otpError: TextView
    lateinit var btnVerify : com.google.android.material.button.MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_otp_screen, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val scrollView = view.findViewById<ScrollView>(R.id.scrollViewOtp)
        val card = view.findViewById<View>(R.id.otpCard)
        val headline = view.findViewById<TextView>(R.id.headline)
        val subText = view.findViewById<TextView>(R.id.subText)
        val backToLogin = view.findViewById<TextView>(R.id.tvBackToLogin)
        btnVerify = view.findViewById(R.id.btn)
        val tvResend = view.findViewById<TextView>(R.id.tvResendOtp)
        otpError = view.findViewById(R.id.otpError)
        otpError.visibility = View.GONE

        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Boolean>("otp_error")?.observe(viewLifecycleOwner) { isError ->
                if (isError) {
                    val errorMessage = findNavController().currentBackStackEntry?.savedStateHandle?.get<String>("otp_error_message")
                    showOtpError(errorMessage ?: "Invalid OTP")
                    // consume event
                    findNavController().currentBackStackEntry?.savedStateHandle?.remove<Boolean>("otp_error")
                    findNavController().currentBackStackEntry?.savedStateHandle?.remove<String>("otp_error_message")
                }
            }


        headline.text = "Verification"
        subText.text = "Enter the OTP sent to reset your password"

        backToLogin.paintFlags = backToLogin.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        tvResend.paintFlags = tvResend.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        backToLogin.setOnClickListener {
            view.findNavController().navigate(R.id.action_resetOTP_to_loginFragment)
        }

        boxes = listOf(
            view.findViewById(R.id.box1),
            view.findViewById(R.id.box2),
            view.findViewById(R.id.box3),
            view.findViewById(R.id.box4),
            view.findViewById(R.id.box5),
            view.findViewById(R.id.box6)
        )
        hiddenEditText = view.findViewById(R.id.hiddenOtpEditText)
        hiddenEditText.requestFocus()

        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(hiddenEditText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)


        setupOtpInput()

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val keyboardHeight = imeInsets.bottom
            if (keyboardHeight > 0) {
                card.animate().translationY(-keyboardHeight * 0.55f).setDuration(200).start()
                headline.animate().translationY(-keyboardHeight * 0.28f)
                    .scaleX(0.95f).scaleY(0.95f).setDuration(200).start()
                subText.animate().alpha(0f).setDuration(200).start()
                scrollView.postDelayed({ scrollView.smoothScrollTo(0, card.top) }, 120)
            } else {
                card.animate().translationY(0f).setDuration(200).start()
                headline.animate().translationY(0f)
                    .scaleX(1f).scaleY(1f).setDuration(200).start()
                subText.animate().alpha(1f).setDuration(200).start()
            }
            insets
        }

        val email = arguments?.getString("email") ?: ""

        btnVerify.setOnClickListener {
            val otp = boxes.joinToString("") { it.text.toString().trim() }
            if (otp.length < 6) {
                Toast.makeText(requireContext(), "Please enter the full 6-digit OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val email = arguments?.getString("email") ?: ""
            val pass1 = arguments?.getString("new_password") ?: ""
            val pass2 = arguments?.getString("confirm_password")?: ""


//            val bundle = Bundle().apply {
//                putString("email", email)
//                putString("otp", otp)
//            }
            btnVerify.isEnabled = false
            btnVerify.text = "Verifying..."
            resetPassword(email, otp, pass1, pass2)
        }

        tvResend.setOnClickListener {
            resendOtp(email)
        }
        boxes.forEach { box ->
            box.setOnClickListener {
                hiddenEditText.requestFocus()
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.showSoftInput(hiddenEditText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            }
        }

    }
        private fun resetPassword(email: String, otp: String, newPassword: String, confirmPassword: String) {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getAuthService(requireContext())
                val request = ResetPasswordOTPRequest(email, otp, newPassword, confirmPassword)
                val response = api.verifyOtp(request)

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Password reset successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_resetOTP_to_loginFragment)
                } else {
                    val errorJson = response.errorBody()?.string()
                    val message = try {
                        val obj = JSONObject(errorJson ?: "")
                        val attemptsLeft = obj.optInt("attemptsLeft", -1)
                        when {
                            attemptsLeft > 0 -> "Invalid OTP, please re-enter"
                            attemptsLeft == 0 -> "OTP expired. Please resend OTP"
                            else -> obj.optString("message", "Something went wrong")
                        }
                    } catch (e: Exception) {
                        "Something went wrong. Please try again."
                    }
                    showOtpError(message)
                }

            } catch (e: java.net.UnknownHostException) {
                Toast.makeText(requireContext(), "No internet connection. Please check your network.", Toast.LENGTH_SHORT).show()
            } catch (e: java.net.SocketTimeoutException) {
                Toast.makeText(requireContext(), "Request timed out. Please try again.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "An unexpected error occurred. Please try again.", Toast.LENGTH_SHORT).show()
            }finally {
                btnVerify.isEnabled = true
                btnVerify.text = "Verify"
            }
        }
    }

    private fun showOtpError(message: String) {
        otpError.text = message
        otpError.visibility = View.VISIBLE
        boxes.forEach { it.setBackgroundResource(R.drawable.otp_box_incorrect) }
    }


    private fun setupOtpInput() {
        hiddenEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s.toString()
                boxes.forEachIndexed { index, box ->
                    if (index < text.length) {
                        box.text = text[index].toString()
                        box.setBackgroundResource(R.drawable.otp_box_active)
                    } else {
                        box.text = ""
                        box.setBackgroundResource(R.drawable.otp_box_bg)
                    }
                }
            }



            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.length > 6) hiddenEditText.setText(it.take(6))
                }
            }
        })
    }

    private fun resendOtp(email: String) {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getAuthService(requireContext())
                val response = api.requestPasswordReset(EmailRequest(email))

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "OTP resent successfully", Toast.LENGTH_SHORT).show()
                } else {
                    showSimpleError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to resend OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSimpleError(errorBody: String?) {
        Toast.makeText(requireContext(), "Failed to send OTP", Toast.LENGTH_LONG).show()
    }
}

