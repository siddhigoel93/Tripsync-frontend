package com.example.tripsync.Auth

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
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
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.models.RegisterRequest
import kotlinx.coroutines.launch

class fragment_signup : Fragment() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirm: EditText
    private lateinit var ivTogglePass: ImageView
    private lateinit var ivToggleConfirm: ImageView
    private lateinit var ivEmailValid: ImageView
    private lateinit var btnSignUp: TextView
    private var tvConfirmError: TextView? = null
    private lateinit var confirmContainer: View
    private lateinit var passwordContainer: View
    private lateinit var signUpCard: View
    private lateinit var headline: TextView
    private lateinit var subText: TextView
    private lateinit var signin: TextView
    private lateinit var lblEmail: TextView
    private lateinit var cbTerms: CheckBox
    private lateinit var tvTerms: TextView

    private lateinit var pwRulesTitlePass: TextView
    private lateinit var pwRulesListPass: ViewGroup
    private lateinit var tvRuleLenPass: TextView
    private lateinit var tvRuleUpperPass: TextView
    private lateinit var tvRuleLowerPass: TextView
    private lateinit var tvRuleSpecialPass: TextView
    private lateinit var tvRuleDigitPass: TextView

    private lateinit var pwRulesTitleConfirm: TextView
    private lateinit var pwRulesListConfirm: ViewGroup
    private lateinit var tvRuleLenConfirm: TextView
    private lateinit var tvRuleUpperConfirm: TextView
    private lateinit var tvRuleLowerConfirm: TextView
    private lateinit var tvRuleSpecialConfirm: TextView
    private lateinit var tvRuleDigitConfirm: TextView

    private var originalEmailLabel: String = ""
    private val COLOR_OK = Color.parseColor("#00C896")
    private val COLOR_DIM = Color.parseColor("#808080")
    private val EMAIL_ERROR_COLOR = Color.parseColor("#D32F2F")
    private var passVisible = false
    private var confirmVisible = false

    override fun onCreateView(inflater: LayoutInflater, container: AndroidViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_signup, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        etConfirm = view.findViewById(R.id.etConfirm)
        ivTogglePass = view.findViewById(R.id.ivTogglePass)
        ivToggleConfirm = view.findViewById(R.id.ivToggleConfirm)
        ivEmailValid = view.findViewById(R.id.ivEmailValid)
        btnSignUp = view.findViewById(R.id.btnSignUp)
        tvConfirmError = view.findViewById(R.id.tvConfirmErrorInline)
        confirmContainer = view.findViewById(R.id.confirmContainer)
        passwordContainer = view.findViewById(R.id.passwordContainer)
        signUpCard = view.findViewById(R.id.signUpCard)
        headline = view.findViewById(R.id.headline)
        subText = view.findViewById(R.id.subText)
        signin = view.findViewById(R.id.tvSignIn)
        tvTerms = view.findViewById(R.id.tvTerms)
        lblEmail = view.findViewById(R.id.lblEmail)
        cbTerms = view.findViewById(R.id.cbTerms)
        originalEmailLabel = lblEmail.text.toString()

        pwRulesTitlePass = view.findViewById(R.id.pwRulesTitlePass)
        pwRulesListPass = view.findViewById(R.id.pwRulesListPass)
        tvRuleLenPass = view.findViewById(R.id.pwRuleLenPass)
        tvRuleUpperPass = view.findViewById(R.id.pwRuleUpperPass)
        tvRuleLowerPass = view.findViewById(R.id.pwRuleLowerPass)
        tvRuleSpecialPass = view.findViewById(R.id.pwRuleSpecialPass)
        tvRuleDigitPass = view.findViewById(R.id.pwRuleDigitPass)

        pwRulesTitleConfirm = view.findViewById(R.id.pwRulesTitleConfirm)
        pwRulesListConfirm = view.findViewById(R.id.pwRulesListConfirm)
        tvRuleLenConfirm = view.findViewById(R.id.pwRuleLenConfirm)
        tvRuleUpperConfirm = view.findViewById(R.id.pwRuleUpperConfirm)
        tvRuleLowerConfirm = view.findViewById(R.id.pwRuleLowerConfirm)
        tvRuleSpecialConfirm = view.findViewById(R.id.pwRuleSpecialConfirm)
        tvRuleDigitConfirm = view.findViewById(R.id.pwRuleDigitConfirm)

        val blockEmojiAndSpaces = InputFilter { source, _, _, _, _, _ ->
            if (source.any { Character.getType(it) == Character.SURROGATE.toInt() ||
                        Character.getType(it) == Character.OTHER_SYMBOL.toInt() ||
                        it.isWhitespace() }) "" else source
        }
        val limit20 = InputFilter.LengthFilter(20)

        etEmail.filters = arrayOf(blockEmojiAndSpaces)
        etPassword.filters = arrayOf(blockEmojiAndSpaces, limit20)
        etConfirm.filters = arrayOf(blockEmojiAndSpaces, limit20)

        signin.paintFlags = signin.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        tvTerms.paintFlags = tvTerms.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        ivEmailValid.setImageResource(R.drawable.ic_check_green)
        ivEmailValid.visibility = View.GONE

        signin.setOnClickListener {
            view.findNavController().navigate(R.id.action_fragment_signup_to_login)
        }


        tvTerms.setOnClickListener {
            val dlg = TermsDialogFragment()
            dlg.show(parentFragmentManager, "terms_dialog")
        }

        ivTogglePass.setOnClickListener {
            passVisible = !passVisible
            setPasswordVisible(etPassword, ivTogglePass, passVisible)
        }
        ivToggleConfirm.setOnClickListener {
            confirmVisible = !confirmVisible
            setPasswordVisible(etConfirm, ivToggleConfirm, confirmVisible)
        }

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                colorPasswordRulesFor(s?.toString() ?: "", true)
            }
        })

        etConfirm.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                colorPasswordRulesFor(s?.toString() ?: "", false)
            }
        })

        etPassword.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            passwordContainer.setBackgroundResource(if (hasFocus) R.drawable.selected_input else R.drawable.input_border)
            if (hasFocus) showPwRulesForPassword() else hidePwRulesPass()
        }

//        etConfirm.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
//            confirmContainer.setBackgroundResource(if (hasFocus) R.drawable.selected_input else R.drawable.input_border)
//            if (hasFocus) showPwRulesForConfirm() else hidePwRulesConfirm()
//        }

        etEmail.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            etEmail.setBackgroundResource(if (hasFocus) R.drawable.selected_input else R.drawable.input_border)
            if (!hasFocus) {
                val email = etEmail.text?.toString()?.trim() ?: ""
                ivEmailValid.visibility = if (isEmailSimpleValid(email)) View.VISIBLE else View.GONE
            } else {
                ivEmailValid.visibility = View.GONE
            }
        }

        etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val email = s?.toString()?.trim() ?: ""
                ivEmailValid.visibility = if (isEmailSimpleValid(email)) View.VISIBLE else View.GONE
            }
        })

        btnSignUp.setOnClickListener {
            clearErrors()
            val email = etEmail.text?.toString()?.trim() ?: ""
            val p1 = etPassword.text?.toString() ?: ""
            val p2 = etConfirm.text?.toString() ?: ""
            val pwErrors = passwordValidationErrors(p1)
            if (!isEmailSimpleValid(email)) {
                showEmailInlineError("Invalid email id")
                return@setOnClickListener
            }
            if (!cbTerms.isChecked) {
                Toast.makeText(requireContext(), "please accept terms and conditions", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (pwErrors.isNotEmpty()) {
                showPasswordError("(Invalid Password)")
                return@setOnClickListener
            }
            if (p1 != p2) {
                showPasswordError("(Both passwords are different)")
                return@setOnClickListener
            }
            registerUser(email, p1, p2, view, btnSignUp)
        }
    }

    private fun showPwRulesForPassword() {
        hidePwRulesConfirm()
        pwRulesTitlePass.visibility = View.VISIBLE
        pwRulesListPass.visibility = View.VISIBLE
        colorPasswordRulesFor(etPassword.text?.toString() ?: "", true)
    }

    private fun showPwRulesForConfirm() {
        hidePwRulesPass()
        pwRulesTitleConfirm.visibility = View.VISIBLE
        pwRulesListConfirm.visibility = View.VISIBLE
        colorPasswordRulesFor(etConfirm.text?.toString() ?: "", false)
    }

    private fun hidePwRulesPass() {
        pwRulesTitlePass.visibility = View.GONE
        pwRulesListPass.visibility = View.GONE
    }

    private fun hidePwRulesConfirm() {
        pwRulesTitleConfirm.visibility = View.GONE
        pwRulesListConfirm.visibility = View.GONE
    }

    private fun colorPasswordRulesFor(p: String, forPasswordField: Boolean) {
        val hasLen = p.length >= 8
        val hasUpper = p.any { it.isUpperCase() }
        val hasLower = p.any { it.isLowerCase() }
        val hasSpecial = p.any { !it.isLetterOrDigit() }
        val hasDigit = p.any { it.isDigit() }

        if (forPasswordField) {
            tvRuleLenPass.setTextColor(if (hasLen) COLOR_OK else COLOR_DIM)
            tvRuleUpperPass.setTextColor(if (hasUpper) COLOR_OK else COLOR_DIM)
            tvRuleLowerPass.setTextColor(if (hasLower) COLOR_OK else COLOR_DIM)
            tvRuleSpecialPass.setTextColor(if (hasSpecial) COLOR_OK else COLOR_DIM)
            tvRuleDigitPass.setTextColor(if (hasDigit) COLOR_OK else COLOR_DIM)
        } else {
            tvRuleLenConfirm.setTextColor(if (hasLen) COLOR_OK else COLOR_DIM)
            tvRuleUpperConfirm.setTextColor(if (hasUpper) COLOR_OK else COLOR_DIM)
            tvRuleLowerConfirm.setTextColor(if (hasLower) COLOR_OK else COLOR_DIM)
            tvRuleSpecialConfirm.setTextColor(if (hasSpecial) COLOR_OK else COLOR_DIM)
            tvRuleDigitConfirm.setTextColor(if (hasDigit) COLOR_OK else COLOR_DIM)
        }
    }

    private fun setPasswordVisible(editText: EditText, icon: ImageView, visible: Boolean) {
        val start = editText.selectionStart
        val end = editText.selectionEnd
        editText.transformationMethod = if (visible) null else PasswordTransformationMethod.getInstance()
        editText.setSelection(if (start >= 0 && end >= 0) start else editText.text.length)
        icon.setImageResource(if (visible) R.drawable.eye else R.drawable.eyedisable)
    }

    private fun clearErrors() {
        etEmail.error = null
        tvConfirmError?.visibility = View.GONE
        etEmail.setBackgroundResource(R.drawable.input_border)
        confirmContainer.setBackgroundResource(R.drawable.input_border)
        passwordContainer.setBackgroundResource(R.drawable.input_border)
        lblEmail.text = originalEmailLabel
        lblEmail.setTextColor(Color.parseColor("#737373"))
    }

    private fun showEmailInlineError(message: String) {
        val suffix = " (${message.trim()})"
        val combined = originalEmailLabel + suffix
        val spannable = SpannableString(combined)
        spannable.setSpan(ForegroundColorSpan(EMAIL_ERROR_COLOR), originalEmailLabel.length, combined.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(RelativeSizeSpan(0.75f), originalEmailLabel.length, combined.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        lblEmail.text = spannable
        etEmail.setBackgroundResource(R.drawable.input_border_error)
        ivEmailValid.visibility = View.GONE
    }

    private fun showPasswordError(message: String) {
        tvConfirmError?.text = message
        tvConfirmError?.visibility = View.VISIBLE
        passwordContainer.setBackgroundResource(R.drawable.input_border_error)
        confirmContainer.setBackgroundResource(R.drawable.input_border_error)
    }

    private fun isEmailSimpleValid(email: String): Boolean {
        val pattern = Regex("^\\S+@\\S+\$")
        return pattern.matches(email)
    }

    private fun passwordValidationErrors(p: String): List<String> {
        val errors = mutableListOf<String>()
        if (p.length < 8) errors.add("At least 8 characters")
        if (!p.any { it.isUpperCase() }) errors.add("At least one uppercase letter")
        if (!p.any { it.isLowerCase() }) errors.add("At least one lowercase letter")
        if (!p.any { !it.isLetterOrDigit() }) errors.add("At least one special character")
        if (!p.any { it.isDigit() }) errors.add("At least one number")
        if (p.contains(" ")) errors.add("Password must not contain spaces")
        return errors
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
                        Toast.makeText(requireContext(), "OTP sent to $email", Toast.LENGTH_LONG).show()
                        val bundle = Bundle().apply { putString("email", email) }
                        view.findNavController().navigate(R.id.action_fragment_signup_to_fragment_otp, bundle)
                    } else {
                        Toast.makeText(requireContext(), body?.message ?: "Registration failed", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val rawError = response.errorBody()?.string()
                    if (!rawError.isNullOrBlank() && rawError.lowercase().contains("already exists")) {
                        showEmailInlineError("Account already exists")
                        return@launch
                    }
                    Toast.makeText(requireContext(), "Request failed", Toast.LENGTH_LONG).show()
                }
            } catch (_: Exception) {
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_LONG).show()
            } finally {
                signUpButton.isEnabled = true
            }
        }
    }
}
