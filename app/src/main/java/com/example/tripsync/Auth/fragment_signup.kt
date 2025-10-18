package com.example.tripsync.Auth

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup as AndroidViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
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
import com.example.tripsync.api.models.RegisterRequest
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class fragment_signup : Fragment() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirm: EditText
    private lateinit var ivTogglePass: ImageView
    private lateinit var ivToggleConfirm: ImageView
    private lateinit var btnSignUp: TextView
    private var tvConfirmError: TextView? = null
    private lateinit var confirmContainer: View
    private lateinit var scrollView: ScrollView
    private lateinit var signUpCard: View
    private lateinit var headline: TextView
    private lateinit var subText: TextView
    private lateinit var signin: TextView
    private lateinit var pwRulesList: AndroidViewGroup
    private lateinit var tvRuleLen: TextView
    private lateinit var tvRuleSpecial: TextView
    private lateinit var tvRuleDigit: TextView
    private lateinit var lblEmail: TextView
    private lateinit var cbTerms: CheckBox
    private var originalEmailLabel: String = ""
    private val COLOR_OK = Color.parseColor("#00C896")
    private val COLOR_DIM = Color.parseColor("#808080")
    private val EMAIL_ERROR_COLOR = Color.parseColor("#D32F2F")
    private var passVisible = false
    private var confirmVisible = false

    private class AsteriskPasswordTransformation : PasswordTransformationMethod() {
        private class AsteriskCharSequence(private val source: CharSequence) : CharSequence {
            override val length: Int get() = source.length
            override fun get(index: Int): Char = '*'
            override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
                source.subSequence(startIndex, endIndex)
        }
        override fun getTransformation(source: CharSequence?, view: View?): CharSequence {
            if (source == null) return ""
            return AsteriskCharSequence(source)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: AndroidViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_signup, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        etConfirm = view.findViewById(R.id.etConfirm)
        ivTogglePass = view.findViewById(R.id.ivTogglePass)
        ivToggleConfirm = view.findViewById(R.id.ivToggleConfirm)
        btnSignUp = view.findViewById(R.id.btnSignUp)
        tvConfirmError = view.findViewById(R.id.tvConfirmErrorInline)
        confirmContainer = view.findViewById(R.id.confirmContainer)
        scrollView = view.findViewById(R.id.scrollView)
        signUpCard = view.findViewById(R.id.signUpCard)
        headline = view.findViewById(R.id.headline)
        subText = view.findViewById(R.id.subText)
        signin = view.findViewById(R.id.tvSignIn)
        signin.paintFlags = signin.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        pwRulesList = view.findViewById(R.id.pwRulesList)
        tvRuleLen = pwRulesList.getChildAt(0) as TextView
        tvRuleSpecial = pwRulesList.getChildAt(1) as TextView
        tvRuleDigit = pwRulesList.getChildAt(2) as TextView
        lblEmail = view.findViewById(R.id.lblEmail)
        cbTerms = view.findViewById(R.id.cbTerms)
        originalEmailLabel = lblEmail.text.toString()

        resetRuleColors()
        colorPasswordRules(etPassword.text?.toString() ?: "")

        signin.setOnClickListener {
            view.findNavController().navigate(R.id.action_fragment_signup_to_login)
        }

        ivTogglePass.setOnClickListener {
            passVisible = !passVisible
            setPasswordVisible(etPassword, ivTogglePass, passVisible)
        }

        ivToggleConfirm.setOnClickListener {
            confirmVisible = !confirmVisible
            setPasswordVisible(etConfirm, ivToggleConfirm, confirmVisible)
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                clearErrors()
            }
        }

        val passwordWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                colorPasswordRules(s?.toString() ?: "")
            }
        }

        etEmail.addTextChangedListener(watcher)
        etPassword.addTextChangedListener(watcher)
        etConfirm.addTextChangedListener(watcher)
        etPassword.addTextChangedListener(passwordWatcher)

        btnSignUp.setOnClickListener {
            clearErrors()
            val email = etEmail.text?.toString()?.trim() ?: ""
            val p1 = etPassword.text?.toString() ?: ""
            val p2 = etConfirm.text?.toString() ?: ""
            colorPasswordRules(p1)
            if (!isEmailValid(email)) {
                showEmailInlineError("Invalid email id")
                return@setOnClickListener
            }
            if (!cbTerms.isChecked) {
                Toast.makeText(requireContext(), "please accept terms and conditions", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val pwErrors = passwordValidationErrors(p1)
            if (pwErrors.isNotEmpty()) {
                val msg = pwErrors.joinToString("\n") { "• $it" }
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                showPasswordError("Password does not meet requirements")
                return@setOnClickListener
            }
            if (p1 != p2) {
                showPasswordError("Both passwords are different")
                return@setOnClickListener
            }
            registerUser(email, p1, p2, view, btnSignUp)
        }

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val keyboardHeight = ime.bottom
            if (keyboardHeight > 0) {
                signUpCard.animate().translationY(-keyboardHeight * 0.6f).setDuration(200).start()
                headline.animate().translationY(-keyboardHeight * 0.28f).scaleX(0.88f).scaleY(0.88f).setDuration(200).start()
                subText.animate().alpha(0.0f).setDuration(200).start()
                scrollView.postDelayed({ scrollView.smoothScrollTo(0, signUpCard.top) }, 160)
            } else {
                signUpCard.animate().translationY(0f).setDuration(200).start()
                headline.animate().translationY(0f).scaleX(1f).scaleY(1f).setDuration(200).start()
                subText.animate().alpha(1.0f).setDuration(200).start()
            }
            insets
        }
    }

    private fun resetRuleColors() {
        tvRuleLen.setTextColor(COLOR_DIM)
        tvRuleSpecial.setTextColor(COLOR_DIM)
        tvRuleDigit.setTextColor(COLOR_DIM)
    }

    private fun setPasswordVisible(editText: EditText, icon: ImageView, visible: Boolean) {
        val start = editText.selectionStart
        val end = editText.selectionEnd
        editText.transformationMethod =
            if (visible) null else AsteriskPasswordTransformation()
        editText.setSelection(start, end)
        icon.setImageResource(if (visible) R.drawable.ic_visibility else R.drawable.ic_visibility_off)
    }

    private fun clearErrors() {
        etEmail.error = null
        tvConfirmError?.visibility = View.GONE
        etEmail.setBackgroundResource(R.drawable.input_border)
        confirmContainer.setBackgroundResource(R.drawable.input_border)
        lblEmail.text = originalEmailLabel
        lblEmail.setTextColor(Color.parseColor("#737373"))
    }

    private fun showEmailInlineError(message: String) {
        val suffix = " (${message.trim()})"
        val combined = originalEmailLabel + suffix
        val spannable = SpannableString(combined)
        spannable.setSpan(
            ForegroundColorSpan(EMAIL_ERROR_COLOR),
            originalEmailLabel.length,
            combined.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            RelativeSizeSpan(0.75f),
            originalEmailLabel.length,
            combined.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        lblEmail.text = spannable
        etEmail.setBackgroundResource(R.drawable.input_border_error)
    }

    private fun showPasswordError(message: String) {
        tvConfirmError?.text = message
        tvConfirmError?.visibility = View.VISIBLE
        confirmContainer.setBackgroundResource(R.drawable.input_border_error)
    }

    private fun isEmailValid(email: String): Boolean {
        val pattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
        return pattern.matches(email)
    }

    private fun passwordValidationErrors(p: String): List<String> {
        val errors = mutableListOf<String>()
        if (p.length < 8) errors.add("At least 8 characters")
        if (!p.any { it.isLowerCase() }) errors.add("At least one lowercase letter")
        if (!p.any { it.isUpperCase() }) errors.add("At least one uppercase letter")
        if (!p.any { !it.isLetterOrDigit() }) errors.add("At least one special character")
        if (!p.any { it.isDigit() }) errors.add("At least one number")
        if (p.contains(" ")) errors.add("Password must not contain spaces")
        return errors
    }

    private fun colorPasswordRules(p: String) {
        val hasLen = p.length >= 8
        val hasSpecial = p.any { !it.isLetterOrDigit() }
        val hasDigit = p.any { it.isDigit() }
        tvRuleLen.setTextColor(if (hasLen) COLOR_OK else COLOR_DIM)
        tvRuleSpecial.setTextColor(if (hasSpecial) COLOR_OK else COLOR_DIM)
        tvRuleDigit.setTextColor(if (hasDigit) COLOR_OK else COLOR_DIM)
    }

    private fun registerUser(email: String, password: String, password2: String, view: View, signUpButton: TextView) {
        lifecycleScope.launch {
            try {
                signUpButton.isEnabled = false
                Toast.makeText(requireContext(), "Please wait...", Toast.LENGTH_SHORT).show()
                val api = ApiClient.getAuthService(requireContext())
                val response = api.registerUser(RegisterRequest(email, password, password2))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status.equals("success", ignoreCase = true)) {
                        Toast.makeText(requireContext(), "OTP sent to $email. Expires in: ${body.data?.otp_expires_in ?: "unknown"}", Toast.LENGTH_LONG).show()
                        val bundle = Bundle().apply { putString("email", email) }
                        view.findNavController().navigate(R.id.action_fragment_signup_to_fragment_otp, bundle)
                    } else {
                        Toast.makeText(requireContext(), body?.message ?: "Registration failed", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val code = response.code()
                    val rawError = response.errorBody()?.string()
                    if (!rawError.isNullOrBlank()) {
                        val lowered = rawError.lowercase()
                        if (lowered.contains("email") && lowered.contains("already exists")) {
                            showEmailInlineError("Account already exists")
                            return@launch
                        }
                        try {
                            val j = JSONObject(rawError)
                            if (j.has("email")) {
                                val emailObj = j.get("email")
                                val msg = when (emailObj) {
                                    is JSONArray -> {
                                        val arr = mutableListOf<String>()
                                        for (i in 0 until emailObj.length()) arr.add(emailObj.optString(i))
                                        arr.joinToString(", ")
                                    }
                                    else -> emailObj.toString()
                                }
                                showEmailInlineError(msg)
                                return@launch
                            }
                        } catch (_: Exception) {}
                    }
                    Toast.makeText(requireContext(), "Request failed (code $code)", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                signUpButton.isEnabled = true
            }
        }
    }
}
