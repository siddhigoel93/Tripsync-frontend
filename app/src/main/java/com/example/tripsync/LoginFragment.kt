package com.example.tripsync

import android.graphics.Paint
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController

class LoginFragment : Fragment() {

    private var isRemembered = false
    private var passwordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val etUsername = view.findViewById<EditText>(R.id.etUsername)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val passwordField = view.findViewById<View>(R.id.passwordField)
        val ivPasswordEye = view.findViewById<ImageView>(R.id.ivPasswordEye)
        val rememberCheckbox = view.findViewById<View>(R.id.rememberCheckbox)
        val forgotPassword = view.findViewById<TextView>(R.id.forgotPassword)
        val signup = view.findViewById<TextView>(R.id.signup)
        val btnNext = view.findViewById<Button>(R.id.btnNext)
        val usernameError = view.findViewById<TextView>(R.id.usernameError)
        val passwordError = view.findViewById<TextView>(R.id.passwordError)

        forgotPassword.paintFlags = forgotPassword.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        signup.paintFlags = signup.paintFlags or Paint.UNDERLINE_TEXT_FLAG


        forgotPassword.setOnClickListener {
            it.findNavController().navigate(R.id.action_login_to_forgot)
        }

        signup.setOnClickListener {
//            Toast.makeText(requireContext(), "Sign-up Clicked", Toast.LENGTH_SHORT).show()
            view.findNavController().navigate(R.id.action_login_to_signup)
        }

        rememberCheckbox.setOnClickListener {
            isRemembered = !isRemembered
            rememberCheckbox.setBackgroundResource(
                if (isRemembered) R.drawable.tick else R.drawable.checkbox
            )
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

        ivPasswordEye.setOnClickListener {
            passwordVisible = !passwordVisible
            if (passwordVisible) {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivPasswordEye.setImageResource(R.drawable.eye)
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivPasswordEye.setImageResource(R.drawable.eyedisable)
            }
            etPassword.setSelection(etPassword.text.length)
        }

        btnNext.setOnClickListener {
            usernameError.visibility = View.GONE
            passwordError.visibility = View.GONE
            etUsername.setBackgroundResource(R.drawable.input_border)
            passwordField.setBackgroundResource(R.drawable.input_border)

            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            val usernameValid = username == "demouser"
            val passwordValid = password == "password123"

            if (usernameValid && passwordValid) {
                Toast.makeText(requireContext(), "Login Successful!", Toast.LENGTH_SHORT).show()

            } else {
                usernameError.visibility = View.VISIBLE
                passwordError.visibility = View.VISIBLE
            }
        }

        return view
    }
}
