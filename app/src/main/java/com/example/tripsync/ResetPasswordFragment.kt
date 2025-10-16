package com.example.tripsync

import android.app.AlertDialog
import android.graphics.Paint
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import androidx.navigation.fragment.findNavController

class ResetPasswordFragment : Fragment() {

    private lateinit var etPassword: EditText
    private lateinit var etPassword2: EditText
    private lateinit var btn: MaterialButton
    private lateinit var ivPasswordEye: ImageView
    private lateinit var ivPasswordEye2: ImageView
    private lateinit var passwordConfirmError: TextView
    private lateinit var signInText: TextView
    private lateinit var passwordField: View
    private lateinit var passwordConfirmField: View

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
        signInText = view.findViewById(R.id.signup)
        passwordField = view.findViewById(R.id.passwordField)
        passwordConfirmField = view.findViewById(R.id.passwordConfirmField)

        signInText.paintFlags = signInText.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        etPassword2.setOnFocusChangeListener { _, hasFocus ->
            passwordConfirmField.setBackgroundResource(
                if (hasFocus) R.drawable.selected_input else R.drawable.input_border
            )
        }
        etPassword.setOnFocusChangeListener { _, hasFocus ->
            passwordField.setBackgroundResource(
                if (hasFocus) R.drawable.selected_input else R.drawable.input_border
            )
        }

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
        if (isVisible) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            eyeIcon.setImageResource(R.drawable.eye)
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            eyeIcon.setImageResource(R.drawable.eyedisable)
        }
        // Move cursor to end after changing inputType
        editText.setSelection(editText.text.length)


    }

    private fun setupButton() {
        btn.setOnClickListener {
            val pass1 = etPassword.text.toString().trim()
            val pass2 = etPassword2.text.toString().trim()

            when {
                pass1.isEmpty() || pass2.isEmpty() -> {
                    Toast.makeText(requireContext(), "Please fill in both fields", Toast.LENGTH_SHORT).show()
                    passwordField.setBackgroundResource(R.drawable.wrong_input)
                    passwordConfirmField.setBackgroundResource(R.drawable.wrong_input)
                    passwordConfirmError.visibility = View.GONE
                }
                pass1.length < 8 -> {
                    showPasswordNoteDialog()
                    passwordConfirmError.visibility = View.GONE
                }
                pass1 != pass2 -> {
                    passwordConfirmField.setBackgroundResource(R.drawable.wrong_input)
                    passwordConfirmError.visibility = View.VISIBLE
                }
                else -> {
                    passwordConfirmError.visibility = View.GONE
                    findNavController().navigate(R.id.action_resetPasswordFragment_to_loginFragment)
                }
            }
        }
    }

    private fun setupNavigation() {
        signInText.setOnClickListener {
            findNavController().navigate(R.id.action_resetPasswordFragment_to_loginFragment)
        }
    }
    fun showPasswordNoteDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_password_note, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialogView.findViewById<TextView>(R.id.btnCloseNote).setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}
