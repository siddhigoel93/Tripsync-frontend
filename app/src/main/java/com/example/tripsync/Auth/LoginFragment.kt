package com.example.tripsync.Auth

import android.graphics.Paint
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.models.LoginRequest
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class LoginFragment : Fragment() {

    private var passwordVisible = false

    private class AsteriskPasswordTransformation : PasswordTransformationMethod() {
        private class AsteriskCharSequence(private val source: CharSequence) : CharSequence {
            override val length get() = source.length
            override fun get(index: Int) = '*'
            override fun subSequence(startIndex: Int, endIndex: Int) = source.subSequence(startIndex, endIndex)
        }

        override fun getTransformation(source: CharSequence?, view: View?) = source?.let { AsteriskCharSequence(it) } ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val etUsername = view.findViewById<EditText>(R.id.etUsername)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val passwordField = view.findViewById<View>(R.id.passwordField)
        val ivPasswordEye = view.findViewById<ImageView>(R.id.ivPasswordEye)
        val forgotPassword = view.findViewById<TextView>(R.id.forgotPassword)
        val signup = view.findViewById<TextView>(R.id.signup)
        val btnNext = view.findViewById<Button>(R.id.btnNext)
        val usernameError = view.findViewById<TextView>(R.id.usernameError)
        val passwordError = view.findViewById<TextView>(R.id.passwordError)

        etPassword.transformationMethod = AsteriskPasswordTransformation()
        passwordField.visibility = View.VISIBLE

        // Underline links
        forgotPassword.paintFlags = forgotPassword.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        signup.paintFlags = signup.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        forgotPassword.setOnClickListener { it.findNavController().navigate(R.id.action_login_to_forgot) }
        signup.setOnClickListener { it.findNavController().navigate(R.id.action_login_to_signup) }

        // Focus highlight
        etUsername.setOnFocusChangeListener { _, hasFocus ->
            etUsername.setBackgroundResource(if (hasFocus) R.drawable.selected_input else R.drawable.input_border)
        }
        etPassword.setOnFocusChangeListener { _, hasFocus ->
            passwordField.setBackgroundResource(if (hasFocus) R.drawable.selected_input else R.drawable.input_border)
        }

        // Toggle password visibility
        ivPasswordEye.setOnClickListener {
            passwordVisible = !passwordVisible
            val start = etPassword.selectionStart
            val end = etPassword.selectionEnd
            etPassword.transformationMethod = if (passwordVisible) null else AsteriskPasswordTransformation()
            etPassword.setSelection(start, end)
            ivPasswordEye.setImageResource(if (passwordVisible) R.drawable.eye else R.drawable.eyedisable)
        }

        btnNext.setOnClickListener {
            usernameError.visibility = View.GONE
            passwordError.visibility = View.GONE
            etUsername.setBackgroundResource(R.drawable.input_border)
            passwordField.setBackgroundResource(R.drawable.input_border)

            val email = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty()) return@setOnClickListener showFieldError(usernameError, etUsername, "(Empty field)")
            if (!isEmailFormatValid(email)) return@setOnClickListener showFieldError(usernameError, etUsername, "(Invalid email format)")
            if (password.isEmpty()) return@setOnClickListener showFieldError(passwordError, passwordField, "(Empty field)")

            lifecycleScope.launch {
                try {
                    val authService = ApiClient.getAuthService(requireContext())
                    val response = authService.loginUser(LoginRequest(email, password))

                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(requireContext(), "Login Successful!", Toast.LENGTH_SHORT).show()
                        val tokens = response.body()?.data?.tokens
                        requireContext().getSharedPreferences("auth", 0).edit().apply {
                            putString("access_token", tokens?.access)
                            putString("refresh_token", tokens?.refresh)
                            apply()
                        }
                        // navigate to home
                    } else {
                        usernameError.text= "(Invalid email or password)"
                        usernameError.visibility = View.VISIBLE
                        etUsername.setBackgroundResource(R.drawable.wrong_input)
                        passwordField.setBackgroundResource(R.drawable.wrong_input)
                    }
                } catch (e: HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    val message = try { JSONObject(errorBody ?: "{}").optString("message", "Invalid credentials") } catch (_: Exception) { "Invalid credentials" }
                    handleLoginError(message, usernameError, passwordError, etUsername, passwordField)
                } catch (e: IOException) {
                    Toast.makeText(requireContext(), "Network failure: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Unexpected error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }

    private fun showFieldError(errorView: TextView, field: View, message: String) {
        errorView.text = message
        errorView.visibility = View.VISIBLE
        field.setBackgroundResource(R.drawable.wrong_input)
    }

    private fun handleLoginError(message: String, usernameError: TextView, passwordError: TextView, etUsername: EditText, passwordField: View) {
        when {
            message.contains("email", true) -> showFieldError(usernameError, etUsername, "($message)")
            message.contains("password", true) -> showFieldError(passwordError, passwordField, "($message)")
            else -> { // fallback
                showFieldError(usernameError, etUsername, "($message)")
                showFieldError(passwordError, passwordField, "($message)")
            }
        }
    }

    private fun isEmailFormatValid(email: String): Boolean {
        val pattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return pattern.matches(email)
    }
}
