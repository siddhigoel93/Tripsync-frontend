package com.example.tripsync.Auth

import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.models.EmailRequest
import com.example.tripsync.api.models.RegistrationOtpVerifyRequest
import com.example.tripsync.api.models.VerifyOtpResponse
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import kotlin.math.min

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
    private var isProgrammaticChange: Boolean = false
    private var isSuccess: Boolean = false
    private lateinit var successVideoContainer: View
    private lateinit var successVideoView: VideoView
    private lateinit var successVideoInner: FrameLayout

    private var resendCooldownUntil: Long = 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_otp, container, false)

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
        successVideoContainer = view.findViewById(R.id.successVideoContainer)
        successVideoView = view.findViewById(R.id.successVideoView)
        successVideoInner = view.findViewById(R.id.successVideoInner)
        val scrollView = view.findViewById<ScrollView>(R.id.scrollViewOtp)
        val otpCard = view.findViewById<View>(R.id.otpCard)
        val headline = view.findViewById<TextView>(R.id.headline)
        val subText = view.findViewById<TextView>(R.id.subText)

        backToLogin.paintFlags = backToLogin.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
        backToLogin.setOnClickListener { findNavController().navigate(R.id.action_fragment_otp_to_login) }

        boxes = listOf(et1, et2, et3, et4, et5, et6)
        setupOtpBoxes()
        et1.requestFocus()
        btnVerify.setOnClickListener { submitOtp() }

        // ⬅️ updated click with cooldown check + start
        tvResend.setOnClickListener {
            val now = SystemClock.elapsedRealtime()
            if (now < resendCooldownUntil) {
                Toast.makeText(requireContext(), "please wait...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startResendCooldown()
            resendOtp()
        }

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

    // ⬅️ added helper to start 10s window
    private fun startResendCooldown() {
        resendCooldownUntil = SystemClock.elapsedRealtime() + 10_000L
    }

    private fun setupOtpBoxes() {
        val pasteAwareFilter = InputFilter { source, start, end, dest, dstart, dend ->
            val incoming = source.subSequence(start, end).toString()
            if (incoming.length > 1) {
                val digits = incoming.filter { it.isDigit() }
                if (digits.isNotEmpty()) {
                    distributeFromStart(digits)
                    return@InputFilter ""
                }
            }
            if (dest != null && dest.length >= 1 && incoming.isNotEmpty()) return@InputFilter ""
            null
        }
        boxes.forEachIndexed { index, et ->
            et.filters = arrayOf(pasteAwareFilter)
            et.inputType = InputType.TYPE_CLASS_NUMBER
            et.isCursorVisible = false
            et.setTextColor(resources.getColor(android.R.color.black))
            et.setBackgroundResource(R.drawable.otp_box_bg)
            et.setPadding(et.paddingLeft, et.paddingTop, et.paddingRight, 0)
            et.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (isProgrammaticChange) return
                    val txt = s?.toString() ?: ""
                    if (txt.length == 1 && index < boxes.lastIndex) boxes[index + 1].requestFocus()
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
                    if (et.text.isNotEmpty()) {
                        et.text.clear()
                        isSuccess = false
                        updateUnderlineVisuals()
                        return@setOnKeyListener true
                    } else if (index > 0) {
                        val prev = boxes[index - 1]
                        prev.requestFocus()
                        if (prev.text.isNotEmpty()) prev.text.clear()
                        isSuccess = false
                        updateFocusVisual(index - 1)
                        updateUnderlineVisuals()
                        return@setOnKeyListener true
                    }
                }
                false
            }
            et.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) updateFocusVisual(index) }
        }
    }

    private fun distributeFromStart(digits: String) {
        isProgrammaticChange = true
        boxes.forEach { it.setText("") }
        val take = min(digits.length, boxes.size)
        for (i in 0 until take) boxes[i].setText(digits[i].toString())
        isProgrammaticChange = false
        val focusIndex = if (take - 1 >= 0) take - 1 else 0
        boxes[focusIndex].requestFocus()
        if (isError) {
            isError = false
            errorBadge.visibility = View.GONE
        }
        isSuccess = false
        updateUnderlineVisuals()
    }

    private fun updateFocusVisual(activeIndex: Int) {
        boxes.forEachIndexed { index, box ->
            box.isSelected = index == activeIndex
            box.isActivated = index == activeIndex
        }
    }

    private fun updateUnderlineVisuals() {
        if (isSuccess) {
            boxes.forEach { it.setBackgroundResource(R.drawable.otp_box_active) }
            return
        }
        if (isError) {
            boxes.forEach { it.setBackgroundResource(R.drawable.otp_box_incorrect) }
            return
        }
//        val lastFilledIndex = boxes.indexOfLast { it.text.toString().trim().isNotEmpty() }
//        boxes.forEachIndexed { index, box ->
//            if (index == lastFilledIndex && lastFilledIndex != -1)
//                box.setBackgroundResource(R.drawable.otp_box_active)
//            else box.setBackgroundResource(R.drawable.otp_box_bg)
//            box.invalidate()
//        }
        boxes.forEach { box ->
            val filled = box.text.toString().trim().isNotEmpty()
            if (filled) {
                box.setBackgroundResource(R.drawable.otp_box_active)
            } else {
                box.setBackgroundResource(R.drawable.otp_box_bg)
            }
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

    private fun showIncorrectOtpUi() {
        isError = true
        isSuccess = false
        errorBadge.visibility = View.VISIBLE
        updateUnderlineVisuals()
    }

    private fun showOtpVerifiedUi() {
        isSuccess = true
        isError = false
        errorBadge.visibility = View.GONE
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
                        showOtpVerifiedUi()
                        playSuccessVideo()
                    }
                } else {
                    Toast.makeText(requireContext(), "Verification failed", Toast.LENGTH_SHORT).show()
                    showIncorrectOtpUi()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Verification failed", Toast.LENGTH_SHORT).show()
                showIncorrectOtpUi()
            }
        }
    }

    private fun playSuccessVideo() {
        successVideoContainer.visibility = View.VISIBLE
        val rawId = resources.getIdentifier("success", "raw", requireContext().packageName)
        if (rawId == 0) {
            navigateToWelcome()
            return
        }
        val videoUri = Uri.parse("android.resource://${requireContext().packageName}/$rawId")
        successVideoView.setVideoURI(videoUri)
        successVideoView.setOnPreparedListener {
            successVideoInner.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    successVideoInner.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    sizeVideoToFillScreen(successVideoView, successVideoInner.width, successVideoInner.height)
                    successVideoView.start()
                }
            })
        }
        successVideoView.setOnCompletionListener {
            successVideoContainer.visibility = View.GONE
            navigateToWelcome()
        }
        successVideoView.setOnErrorListener { _, _, _ ->
            successVideoContainer.visibility = View.GONE
            navigateToWelcome()
            true
        }
    }

    private fun navigateToWelcome() {
        findNavController().navigate(
            R.id.welcomeFragment,
            null,
            androidx.navigation.navOptions {
                popUpTo(R.id.nav_graph) { inclusive = true }
                launchSingleTop = true
            }
        )
    }

    private fun sizeVideoToFillScreen(videoView: VideoView, containerW: Int, containerH: Int) {
        if (containerW <= 0 || containerH <= 0) return
        val lp = FrameLayout.LayoutParams(containerW, containerH, Gravity.CENTER)
        videoView.layoutParams = lp
    }

    private fun resendOtp() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getAuthService(requireContext())
                val response = api.resendOtp(EmailRequest(email))
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "OTP resent successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to resend OTP", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        successVideoView.stopPlayback()
    }
}
