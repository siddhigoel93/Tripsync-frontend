package com.example.tripsync.Auth

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.text.method.ReplacementTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.models.ResetPasswordOTPRequest
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import org.json.JSONObject

class ResetPasswordFragment : Fragment() {

    private lateinit var etPassword: EditText
    private lateinit var etPassword2: EditText
    private lateinit var btn: MaterialButton
    private lateinit var ivPasswordEye: ImageView
    private lateinit var ivPasswordEye2: ImageView
    private lateinit var passwordConfirmError: TextView
    private lateinit var passwordError: TextView
    private lateinit var signInText: TextView
    private lateinit var passwordField: View
    private lateinit var passwordConfirmField: View

    private lateinit var icon1: ImageView
    private lateinit var icon2: ImageView
    private lateinit var icon3: ImageView
    private lateinit var icon4: ImageView
    private lateinit var icon5: ImageView
    private lateinit var rule1: TextView
    private lateinit var rule2: TextView
    private lateinit var rule3: TextView
    private lateinit var rule4: TextView
    private lateinit var rule5: TextView
    private lateinit var passwordRules: LinearLayout


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
        passwordError = view.findViewById(R.id.passwordError)
        signInText = view.findViewById(R.id.signup)
        passwordField = view.findViewById(R.id.passwordField)
        passwordConfirmField = view.findViewById(R.id.passwordConfirmField)
        passwordRules = view.findViewById(R.id.passwordRules)

        icon1 = view.findViewById(R.id.icon1)
        icon2 = view.findViewById(R.id.icon2)
        icon3 = view.findViewById(R.id.icon3)
        icon4 = view.findViewById(R.id.icon4)
        icon5 = view.findViewById(R.id.icon5)
        rule1 = view.findViewById(R.id.rule1)
        rule2 = view.findViewById(R.id.rule2)
        rule3 = view.findViewById(R.id.rule3)
        rule4 = view.findViewById(R.id.rule4)
        rule5 = view.findViewById(R.id.rule5)

        signInText.paintFlags = signInText.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                passwordField.setBackgroundResource(R.drawable.selected_input)
                passwordError.visibility = View.GONE
                passwordRules.visibility = View.VISIBLE
            } else {
                passwordField.setBackgroundResource(R.drawable.input_border)
                passwordRules.visibility = View.GONE
            }
        }
        etPassword2.setOnFocusChangeListener { _, hasFocus ->
            passwordConfirmField.setBackgroundResource(if (hasFocus) R.drawable.selected_input else R.drawable.input_border)
        }

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = s.toString()
                updateRule(password.length >= 8, icon1, rule1)
                updateRule(password.any { it.isUpperCase() }, icon2, rule2)
                updateRule(password.any { it.isLowerCase() }, icon5, rule5)
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

            passwordConfirmError.visibility = View.GONE
            passwordError.visibility = View.GONE
            passwordField.setBackgroundResource(R.drawable.input_border)
            passwordConfirmField.setBackgroundResource(R.drawable.input_border)



            var hasError = false
            btn.isEnabled = false

            if (pass1.isEmpty() || pass2.isEmpty()) {
                passwordConfirmError.visibility = View.VISIBLE
                passwordConfirmError.text = "Please fill in both fields"
                passwordField.setBackgroundResource(R.drawable.wrong_input)
                passwordConfirmField.setBackgroundResource(R.drawable.wrong_input)
                btn.isEnabled = true
                return@setOnClickListener
            } else {
                passwordConfirmError.visibility = View.GONE
                passwordField.setBackgroundResource(R.drawable.input_border)
                passwordConfirmField.setBackgroundResource(R.drawable.input_border)
            }

            if (pass1 != pass2) {
                passwordConfirmError.visibility = View.VISIBLE
                passwordConfirmError.text = "Passwords must be the same"
                passwordConfirmField.setBackgroundResource(R.drawable.wrong_input)
                btn.isEnabled = true
                return@setOnClickListener
            }

            if (pass1.length < 8) { icon1.setColorFilter(Color.RED); rule1.setTextColor(Color.RED); hasError = true }
            if (!pass1.any { it.isUpperCase() }) { icon2.setColorFilter(Color.RED); rule2.setTextColor(Color.RED); hasError = true }
            if (!pass1.any { it.isLowerCase() }) { icon5.setColorFilter(Color.RED); rule5.setTextColor(Color.RED); hasError = true }
            if (!pass1.any { it.isDigit() }) { icon4.setColorFilter(Color.RED); rule4.setTextColor(Color.RED); hasError = true }
            if (!pass1.any { "!@#$%^&*(),.?\":{}|<>".contains(it) }) { icon3.setColorFilter(Color.RED); rule3.setTextColor(Color.RED); hasError = true }

            if (hasError) {
                passwordError.visibility = View.VISIBLE
                passwordRules.visibility = View.VISIBLE
                passwordError.text = "Please fix the highlighted rules"
                btn.isEnabled = true
                return@setOnClickListener
            }
//            else {
//                passwordError.visibility = View.GONE
//            }
            val bundle = Bundle().apply {
                putString("email", email)
                putString("new_password" , pass1)
                putString("confirm_password" , pass2)
            }
            Toast.makeText(requireContext(), "Please verify OTP !", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_resetPasswordFragment_to_resetOTP , bundle)
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
        } else {
            icon.setColorFilter(Color.parseColor("#737373"))
            textView.setTextColor(Color.parseColor("#737373"))
        }
    }

}
