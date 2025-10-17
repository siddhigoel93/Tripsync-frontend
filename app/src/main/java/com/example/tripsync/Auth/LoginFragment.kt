package com.example.tripsync.Auth

import android.graphics.Paint
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        forgotPassword.paintFlags = forgotPassword.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        signup.paintFlags = signup.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        forgotPassword.setOnClickListener {
            it.findNavController().navigate(R.id.action_login_to_forgot)
        }
        signup.setOnClickListener {
            it.findNavController().navigate(R.id.action_login_to_signup)
        }



        etUsername.setOnFocusChangeListener { _, hasFocus ->
            etUsername.setBackgroundResource(
                if (hasFocus) R.drawable.selected_input else R.drawable.input_border
            )
        }
        etPassword.setOnFocusChangeListener { _, hasFocus ->
            passwordField.setBackgroundResource(
                if (hasFocus) R.drawable.selected_input else R.drawable.input_border
            )
        }

fun setPasswordVisible(editText: EditText, icon: ImageView, visible: Boolean) {
    val start = editText.selectionStart
    val end = editText.selectionEnd
    editText.transformationMethod =
        if (visible) null else AsteriskPasswordTransformation()
    editText.setSelection(start, end)
    icon.setImageResource(if (visible) R.drawable.ic_visibility else R.drawable.ic_visibility_off)
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

            val email = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty()) {
                usernameError.text = "(Empty field)"
                usernameError.visibility = View.VISIBLE
                etUsername.setBackgroundResource(R.drawable.wrong_input)
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordError.text = "(Empty field)"
                passwordError.visibility = View.VISIBLE
                passwordField.setBackgroundResource(R.drawable.wrong_input)
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val authService = ApiClient.getAuthService(requireContext())
                    val request = LoginRequest(email, password)
                    val response = authService.loginUser(request)

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.status == "success") {  // API-level check
                            val accessToken = body.data?.tokens?.access
                            val refreshToken = body.data?.tokens?.refresh

                            // Save tokens
                            val prefs = requireContext().getSharedPreferences("auth", 0)
                            prefs.edit().apply {
                                putString("access_token", accessToken)
                                putString("refresh_token", refreshToken)
                                apply()
                            }

                            Toast.makeText(requireContext(), "Login Successful!", Toast.LENGTH_SHORT).show()
                            // Navigate to home
//
                        } else {
                            val errorMessage = body?.message ?:"(No user found with this email)"
                            usernameError.text = "($errorMessage)"
                            usernameError.visibility = View.VISIBLE
                            etUsername.setBackgroundResource(R.drawable.wrong_input)
                            passwordField.setBackgroundResource(R.drawable.wrong_input)
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = try {
                            JSONObject(errorBody ?: "{}").optString("message", "Invalid credentials")
                        } catch (_: Exception) {
                            "Invalid credentials"
                        }
                        usernameError.text = "($errorMessage)"
                        usernameError.visibility = View.VISIBLE
                        etUsername.setBackgroundResource(R.drawable.wrong_input)
                        passwordField.setBackgroundResource(R.drawable.wrong_input)
                    }
                } catch (e: IOException) {
                    Toast.makeText(requireContext(), "Network failure: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Unexpected error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }


        return view
    }
}