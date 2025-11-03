package com.example.tripsync.Auth

import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.models.EmailRequest
import com.example.tripsync.api.models.OtpCodeRequest
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import org.json.JSONObject

class ContactVerifyFragment : Fragment() {

    private lateinit var hiddenEditText: EditText
    private lateinit var boxes: List<TextView>
    private lateinit var otpError: TextView
    private lateinit var btnVerifyOtp : MaterialButton
    private lateinit var btnPrevious : MaterialButton
    private lateinit var verifyOtpTitle: TextView
    private lateinit var otpSubtitle: TextView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_contact_verify, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        verifyOtpTitle = view.findViewById(R.id.verifyOtpTitle)
        otpSubtitle = view.findViewById(R.id.otpSubtitle)
        btnVerifyOtp = view.findViewById(R.id.btn)
        btnPrevious = view.findViewById(R.id.btnPrev)
        val tvResend = view.findViewById<TextView>(R.id.tvResendOtp)
        otpError = view.findViewById(R.id.otpError)
        otpError.visibility = View.GONE

        boxes = listOf(
            view.findViewById(R.id.box1),
            view.findViewById(R.id.box2),
            view.findViewById(R.id.box3),
            view.findViewById(R.id.box4),
            view.findViewById(R.id.box5),
            view.findViewById(R.id.box6)
        )
        hiddenEditText = view.findViewById(R.id.hiddenOtpEditText)

        tvResend.paintFlags = tvResend.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        hiddenEditText.requestFocus()

        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(hiddenEditText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)

        setupOtpInput()

        btnPrevious.setOnClickListener {
            findNavController().popBackStack()
        }

        btnVerifyOtp.setOnClickListener {
            val otp = boxes.joinToString("") { it.text.toString().trim() }
            if (otp.length < 6) {
                Toast.makeText(requireContext(), "Please enter the full 6-digit OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnVerifyOtp.isEnabled = false
            btnVerifyOtp.text = "Verifying..."
            verifyProfileOtp(otp)
        }

        tvResend.setOnClickListener {
            resendProfileOtp()
        }

        boxes.forEach { box ->
            box.setOnClickListener {
                hiddenEditText.requestFocus()
                imm.showSoftInput(hiddenEditText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val keyboardHeight = imeInsets.bottom
            if (keyboardHeight > 0) {
                view.animate().translationY(-keyboardHeight * 0.55f).setDuration(200).start()
                verifyOtpTitle.animate().translationY(-keyboardHeight * 0.28f)

            }else{
                view.animate().translationY(0f).setDuration(200).start()
                verifyOtpTitle.animate().translationY(0f)
                    .scaleX(1f).scaleY(1f).setDuration(200).start()
                otpSubtitle.animate().alpha(1f).setDuration(200).start()
            }

            insets
        }
    }



    private fun verifyProfileOtp(otp: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val api = ApiClient.getTokenService(requireContext())
                val response = api.verifyPhoneOtp(OtpCodeRequest(otp))


                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Phone number verified!", Toast.LENGTH_SHORT).show()
                    navigateToExplore()
                } else {
                    val error = response.errorBody()?.string()
                    Log.e("VerifyProfileOtp", "Error: $error")
                    Toast.makeText(requireContext(), "Invalid or expired OTP", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("VerifyProfileOtp", "Exception: ${e.message}")
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
            }finally {
                btnVerifyOtp.isEnabled = true
                btnVerifyOtp.text = "Verify OTP"
            }
        }
    }

    fun navigateToExplore() {
        val action = ContactVerifyFragmentDirections.actionContactVerifyFragmentToHomeFragment(showHeader = true)

        findNavController().navigate(action)
    }
    private fun resendProfileOtp() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val api = ApiClient.getTokenService(requireContext())
                val response = api.resendPersonalOtp()
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "OTP resent successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to resend OTP", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // --- UI/Input Logic ---

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
                otpError.visibility = View.GONE // Hide error on new input

                boxes.forEachIndexed { index, box ->
                    if (index < text.length) {
                        box.text = text[index].toString()
                        box.setBackgroundResource(R.drawable.otp_box_active)
                    } else {
                        box.text = ""
                        box.setBackgroundResource(R.drawable.otp_box_bg)
                    }
                }

                if (text.length < boxes.size) {
                    boxes[text.length].setBackgroundResource(R.drawable.otp_box_active)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.length > 6) {
                        hiddenEditText.setText(it.take(6))
                        hiddenEditText.setSelection(6)
                    }
                }
            }
        })
    }
}