package com.example.tripsync.Auth

import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.KeyEvent
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
import com.example.tripsync.api.models.RegistrationOtpVerifyRequest
import com.example.tripsync.api.models.VerifyOtpResponse
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject

class FragmentOtp : Fragment() {

    private lateinit var et1: EditText
    private lateinit var et2: EditText
    private lateinit var et3: EditText
    private lateinit var et4: EditText
    private lateinit var et5: EditText
    private lateinit var et6: EditText
    private lateinit var boxes: List<EditText>

    private lateinit var btnVerify: MaterialTextView
    private lateinit var tvResend: TextView
    private lateinit var backToLogin: TextView
    private lateinit var errorBadge: TextView
    private lateinit var email: String

    private var isError: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_otp, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        email = arguments?.getString("email") ?: ""

        et1 = view.findViewById(R.id.et1)
        et2 = view.findViewById(R.id.et2)
        et3 = view.findViewById(R.id.et3)
        et4 = view.findViewById(R.id.et4)
        et5 = view.findViewById(R.id.et5)
        et6 = view.findViewById(R.id.et6)
        btnVerify = view.findViewById(R.id.btnVerify)
        tvResend = view.findViewById(R.id.tvResendOtp)
        backToLogin = view.findViewById(R.id.tvBackToLogin)
        errorBadge = view.findViewById(R.id.verifyOtpError)

        val scrollView = view.findViewById<ScrollView>(R.id.scrollViewOtp)
        val otpCard = view.findViewById<View>(R.id.otpCard)
        val headline = view.findViewById<TextView>(R.id.headline)
        val subText = view.findViewById<TextView>(R.id.subText)

        backToLogin.paintFlags = backToLogin.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        backToLogin.setOnClickListener {
            view.findNavController().navigate(R.id.action_fragment_otp_to_login)
        }

        boxes = listOf(et1, et2, et3, et4, et5, et6)
        setupOtpBoxes()
        et1.requestFocus()

        btnVerify.setOnClickListener { submitOtp() }
        tvResend.setOnClickListener { resendOtp() }

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val keyboardHeight = imeInsets.bottom
            if (keyboardHeight > 0) {
                otpCard.animate().translationY(-keyboardHeight * 0.55f).setDuration(200).start()
                headline.animate().translationY(-keyboardHeight * 0.28f).scaleX(0.95f).scaleY(0.95f).setDuration(200).start()
                subText.animate().alpha(0.0f).setDuration(200).start()
                scrollView.postDelayed({ scrollView.smoothScrollTo(0, otpCard.top) }, 120)
            } else {
                otpCard.animate().translationY(0f).setDuration(200).start()
                headline.animate().translationY(0f).scaleX(1f).scaleY(1f).setDuration(200).start()
                subText.animate().alpha(1.0f).setDuration(200).start()
            }
            insets
        }

        updateFocusVisual(-1)
        updateUnderlineVisuals()
    }

    private fun setupOtpBoxes() {
        boxes.forEachIndexed { index, et ->
            et.filters = arrayOf(InputFilter.LengthFilter(1))
            et.inputType = InputType.TYPE_CLASS_NUMBER
            et.isCursorVisible = false
            et.setTextColor(resources.getColor(android.R.color.black))
            et.setBackgroundResource(R.drawable.otp_box_bg)
            et.setPadding(et.paddingLeft, et.paddingTop, et.paddingRight, 0)

            et.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && index < boxes.lastIndex) boxes[index + 1].requestFocus()
                    if (isError) {
                        isError = false
                        errorBadge.visibility = View.GONE
                    }
                    updateFocusVisual(index)
                    updateUnderlineVisuals()
                }
            })

            et.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (et.text.isEmpty() && index > 0) {
                        boxes[index - 1].apply {
                            requestFocus()
                            setSelection(text.length)
                        }
                        updateFocusVisual(index - 1)
                        updateUnderlineVisuals()
                        return@setOnKeyListener true
                    }
                }
                false
            }

            et.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) updateFocusVisual(index)
            }
        }
    }

    private fun updateFocusVisual(activeIndex: Int) {
        boxes.forEachIndexed { index, box ->
            box.isSelected = index == activeIndex
            box.isActivated = index == activeIndex
        }
    }

    private fun updateUnderlineVisuals() {
        if (isError) {
            boxes.forEach { it.setBackgroundResource(R.drawable.otp_bg_with_red_underline) }
            return
        }

        val lastFilledIndex = boxes.indexOfLast { it.text.toString().trim().isNotEmpty() }
        boxes.forEachIndexed { index, box ->
            if (index == lastFilledIndex && lastFilledIndex != -1) {
                box.setBackgroundResource(R.drawable.otp_bg_with_underline)
            } else {
                box.setBackgroundResource(R.drawable.otp_box_bg)
            }
            box.invalidate()
        }
    }

    private fun getOtp(): String = boxes.joinToString("") { it.text.toString().trim() }

    private fun ResponseBody?.readApiError(): String? = try {
        val text = this?.string() ?: return null
        val obj = JSONObject(text)
        when {
            obj.has("message") -> obj.getString("message")
            obj.has("detail") -> obj.getString("detail")
            else -> null
        }
    } catch (_: Exception) { null }

    private fun mapVerifyOtpError(code: Int, fallback: String?): String = when (code) {
        400, 401 -> "Verification failed"
        403 -> "Verification failed"
        404 -> "Verification failed"
        429 -> "Verification failed"
        500, 502, 503 -> "Verification failed"
        else -> fallback ?: "Verification failed"
    }

    private fun mapResendError(code: Int, fallback: String?): String = when (code) {
        400 -> "Verification failed"
        404 -> "Verification failed"
        429 -> "Verification failed"
        500, 502, 503 -> "Verification failed"
        else -> fallback ?: "Verification failed"
    }

    private fun showIncorrectOtpUi() {
        isError = true
        errorBadge.visibility = View.VISIBLE
        updateUnderlineVisuals()
    }

    private fun submitOtp() {
        val otp = getOtp()
        if (otp.length != 6) {
            Toast.makeText(requireContext(), "Enter complete OTP", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val api = ApiClient.getAuthService(requireContext())
                val request = RegistrationOtpVerifyRequest(email, otp)
                val response = api.verifyOtp(request)
                if (response.isSuccessful) {
                    val body: VerifyOtpResponse? = response.body()
                    val accessToken = body?.data?.tokens?.access
                    val refreshToken = body?.data?.tokens?.refresh
                    if (accessToken != null && refreshToken != null) {
                        val prefs = requireContext().getSharedPreferences("auth", 0)
                        prefs.edit().apply {
                            putString("access_token", accessToken)
                            putString("refresh_token", refreshToken)
                            apply()
                        }
                        Toast.makeText(requireContext(), "OTP verified", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val friendly = mapVerifyOtpError(response.code(), response.errorBody().readApiError())
                    Toast.makeText(requireContext(), friendly, Toast.LENGTH_SHORT).show()
                    showIncorrectOtpUi()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Verification failed", Toast.LENGTH_SHORT).show()
                showIncorrectOtpUi()
            }
        }
    }

    private fun resendOtp() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getAuthService(requireContext())
                val response = api.resendOtp(EmailRequest(email))
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "OTP resent successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    val friendly = mapResendError(response.code(), response.errorBody().readApiError())
                    Toast.makeText(requireContext(), friendly, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Wrong OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
