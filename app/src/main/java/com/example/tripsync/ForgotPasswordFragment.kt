package com.example.tripsync

import android.graphics.Paint
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController

class ForgotPasswordFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_forgot_password, container, false)

        val email = view.findViewById<EditText>(R.id.etEmail)
        val verify = view.findViewById<Button>(R.id.btn)
        val backToLogin = view.findViewById<TextView>(R.id.backtologin)
        val usernameError = view.findViewById<TextView>(R.id.usernameError)
        val usernameError2 = view.findViewById<TextView>(R.id.usernameError2)

        backToLogin.paintFlags = backToLogin.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        backToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_forgotPasswordFragment_to_loginFragment)

        }

        email.setOnFocusChangeListener { _, hasFocus ->
            email.setBackgroundResource(
                if (hasFocus) R.drawable.selected_input else R.drawable.input_border
            )
        }

        verify.setOnClickListener {
            usernameError.visibility = View.GONE
            usernameError2.visibility = View.GONE
            email.setBackgroundResource(R.drawable.input_border)

            val emailText = email.text.toString().trim()

            if (emailText.isEmpty()) {
                usernameError.visibility = View.VISIBLE
                usernameError.text = "(Email cannot be empty)"
                email.setBackgroundResource(R.drawable.wrong_input)
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                usernameError.visibility = View.VISIBLE
                email.setBackgroundResource(R.drawable.wrong_input)
                return@setOnClickListener
            }
            view.findNavController().navigate(R.id.action_forgotPasswordFragment_to_resetPasswordFragment)

        }

        return view
    }
}
