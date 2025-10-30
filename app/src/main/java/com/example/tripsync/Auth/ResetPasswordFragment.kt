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

    private var isPasswordVisible1 = false
    private var isPasswordVisible2 = false
//    class AsteriskPasswordTransformation : ReplacementTransformationMethod() {
//        override fun getOriginal(): CharArray {
//            // All possible characters in password
//            return charArrayOf(
//                'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P',
//                'Q','R','S','T','U','V','W','X','Y','Z',
//                'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p',
//                'q','r','s','t','u','v','w','x','y','z',
//                '0','1','2','3','4','5','6','7','8','9',
//                '!','@','#','$','%','^','&','*','(',')','-','_','+','=','{','}','[',']','|',';',':','"','\'','<','>',',','.','?','/','`','~',' '
//            )
//        }
//
//        override fun getReplacement(): CharArray {
//            return CharArray(getOriginal().size) { '*' }
//        }
//    }
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
        val passwordRules = view.findViewById<LinearLayout>(R.id.passwordRules)

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
//
//        etPassword.transformationMethod = AsteriskPasswordTransformation()
//        etPassword2.transformationMethod = AsteriskPasswordTransformation()

        etPassword.setOnFocusChangeListener { _, hasFocus ->
            passwordField.setBackgroundResource(if (hasFocus) R.drawable.selected_input else R.drawable.input_border)
            passwordRules.visibility = if (hasFocus) View.VISIBLE else View.GONE
        }
        etPassword2.setOnFocusChangeListener { _, hasFocus ->
            passwordConfirmField.setBackgroundResource(if (hasFocus) R.drawable.selected_input else R.drawable.input_border)
        }

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
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

            var hasError = false
            btn.isEnabled = false

            if (pass1.isEmpty() || pass2.isEmpty()) {
                passwordConfirmError.visibility = View.VISIBLE
                passwordConfirmError.text = "Please fill in both fields"
                passwordField.setBackgroundResource(R.drawable.wrong_input)
                passwordConfirmField.setBackgroundResource(R.drawable.wrong_input)
                hasError = true
            } else {
                passwordConfirmError.visibility = View.GONE
                passwordField.setBackgroundResource(R.drawable.input_border)
                passwordConfirmField.setBackgroundResource(R.drawable.input_border)
            }

            if (pass1 != pass2) {
                passwordConfirmError.visibility = View.VISIBLE
                passwordConfirmError.text = "Passwords must be the same"
                passwordConfirmField.setBackgroundResource(R.drawable.wrong_input)
                hasError = true
            }

            if (pass1.length < 8) { icon1.setColorFilter(Color.RED); rule1.setTextColor(Color.RED); hasError = true }
            if (!pass1.any { it.isUpperCase() }) { icon2.setColorFilter(Color.RED); rule2.setTextColor(Color.RED); hasError = true }
            if (!pass1.any { it.isLowerCase() }) { icon5.setColorFilter(Color.RED); rule5.setTextColor(Color.RED); hasError = true }
            if (!pass1.any { it.isDigit() }) { icon4.setColorFilter(Color.RED); rule4.setTextColor(Color.RED); hasError = true }
            if (!pass1.any { "!@#$%^&*(),.?\":{}|<>".contains(it) }) { icon3.setColorFilter(Color.RED); rule3.setTextColor(Color.RED); hasError = true }

            if (hasError) {
                passwordError.visibility = View.VISIBLE
                passwordError.text = "Please fix the highlighted rules"
                btn.isEnabled = true
                return@setOnClickListener
            } else {
                passwordError.visibility = View.GONE
            }
//            btn.isEnabled = false
//            btn.text = "Please wait..."
//            resetPassword(email, otp, pass1, pass2)
            val bundle = Bundle().apply {
                putString("email", email)
                putString("new_password" , pass1)
                putString("confirm_password" , pass2)
            }
            Toast.makeText(requireContext(), "Please verify OTP !", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_resetPasswordFragment_to_resetOTP , bundle)
        }
    }

//    private fun resetPassword(email: String, otp: String, newPassword: String, confirmPassword: String) {
//        lifecycleScope.launch {
//            try {
//                val api = ApiClient.getAuthService(requireContext())
//                val request = ResetPasswordOTPRequest(email, otp, newPassword, confirmPassword)
//                val response = api.verifyOtp(request)
//
//                if (response.isSuccessful) {
//                    Toast.makeText(requireContext(), "Password reset successfully!", Toast.LENGTH_SHORT).show()
//                    findNavController().navigate(R.id.action_resetPasswordFragment_to_loginFragment)
//                } else {
//                    val errorJson = response.errorBody()?.string()
//                    val message = try {
//                        val obj = JSONObject(errorJson ?: "")
//                        val attemptsLeft = obj.optInt("attemptsLeft", -1)
//                        when {
//                            attemptsLeft > 0 -> "Invalid OTP, please re-enter"
//                            attemptsLeft == 0 -> "OTP expired. Please resend OTP"
//                            else -> obj.optString("message", "Something went wrong")
//                        }
//                    } catch (e: Exception) {
//                        "Something went wrong. Please try again."
//                    }
//                    findNavController().previousBackStackEntry?.savedStateHandle?.set("otp_error", true)
//                    findNavController().previousBackStackEntry?.savedStateHandle?.set("otp_error_message", message)
//                    findNavController().popBackStack()
//                }
//
//            } catch (e: java.net.UnknownHostException) {
//                Toast.makeText(requireContext(), "No internet connection. Please check your network.", Toast.LENGTH_SHORT).show()
//            } catch (e: java.net.SocketTimeoutException) {
//                Toast.makeText(requireContext(), "Request timed out. Please try again.", Toast.LENGTH_SHORT).show()
//            } catch (e: Exception) {
//                Toast.makeText(requireContext(), "An unexpected error occurred. Please try again.", Toast.LENGTH_SHORT).show()
//            }finally {
//                btn.isEnabled = true
//                btn.text = "Reset Password"
//            }
//        }
//    }


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
