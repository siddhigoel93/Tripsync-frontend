package com.example.tripsync.Auth

import android.annotation.SuppressLint
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
import androidx.navigation.fragment.findNavController
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.models.LoginRequest
import kotlinx.coroutines.launch
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

    @SuppressLint("MissingInflatedId")
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
        val verified = view.findViewById<ImageView>(R.id.verified)

        etPassword.transformationMethod = AsteriskPasswordTransformation()

        forgotPassword.paintFlags = forgotPassword.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        signup.paintFlags = signup.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        forgotPassword.setOnClickListener { it.findNavController().navigate(R.id.action_login_to_forgot) }
        signup.setOnClickListener { it.findNavController().navigate(R.id.action_login_to_signup) }

        etUsername.setOnFocusChangeListener { _, hasFocus ->
            etUsername.setBackgroundResource(if (hasFocus) R.drawable.selected_input else R.drawable.input_border)
        }
        etPassword.setOnFocusChangeListener { _, hasFocus ->
            passwordField.setBackgroundResource(if (hasFocus) R.drawable.selected_input else R.drawable.input_border)
        }

        fun setPasswordVisible(editText: EditText, icon: ImageView, visible: Boolean) {
            val start = editText.selectionStart
            val end = editText.selectionEnd
            editText.transformationMethod = if (visible) null else AsteriskPasswordTransformation()
            editText.setSelection(start, end)
            icon.setImageResource(if (visible) R.drawable.eye else R.drawable.eyedisable)
        }

        ivPasswordEye.setOnClickListener {
            passwordVisible = !passwordVisible
            setPasswordVisible(etPassword, ivPasswordEye, passwordVisible)
        }

        btnNext.setOnClickListener {
            usernameError.visibility = View.GONE
            passwordError.visibility = View.GONE
            etUsername.setBackgroundResource(R.drawable.input_border)
            passwordField.setBackgroundResource(R.drawable.input_border)

            val emailText = etUsername.text.toString().trim()
            val passwordText = etPassword.text.toString().trim()

            var hasError = false
            btnNext.isEnabled = false
            btnNext.text = "Logging in..."

            if (emailText.isEmpty()) {
                showFieldError(usernameError, etUsername, "(Empty field)")
                hasError = true
            }

            if (passwordText.isEmpty()) {
                showFieldError(passwordError, passwordField, "(Empty field)")
                hasError = true
            }

            if(hasError) return@setOnClickListener
            lifecycleScope.launch {
                try {
                    val authService = ApiClient.getAuthService(requireContext())
                    val response = authService.loginUser(LoginRequest(emailText, passwordText))
                    val body = response.body()


                    if (response.isSuccessful && body != null && body.status == "success") {
                        val tokens = body.data?.tokens
                        requireContext().getSharedPreferences("auth", 0).edit().apply {
                            putString("access_token", tokens?.access)
                            putString("refresh_token", tokens?.refresh)
                            apply()
                        }
                        verified.visibility = View.VISIBLE
                        Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    } else {
                        showFieldError(usernameError, etUsername, "(Incorrect email or password)")
                        showFieldError(passwordError, passwordField, "")
                    }

                } catch (httpException: HttpException) {
                    showFieldError(usernameError, etUsername, "(Please check your email or password)")
                    showFieldError(passwordError, passwordField, "")
                } catch (ioException: IOException) {
                    Toast.makeText(requireContext(), "Please check your internet connection and try again.", Toast.LENGTH_SHORT).show()
                } catch (exception: Exception) {
                    Toast.makeText(requireContext(), "Something went wrong. Please try again later.", Toast.LENGTH_SHORT).show()
                }finally {
                    btnNext.isEnabled = true
                    btnNext.text = "Login"
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
}
