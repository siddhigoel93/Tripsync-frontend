package com.example.tripsync

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class fragment_signup : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_signup, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val etConfirm = view.findViewById<EditText>(R.id.etConfirm)
        val ivTogglePass = view.findViewById<ImageView>(R.id.ivTogglePass)
        val ivToggleConfirm = view.findViewById<ImageView>(R.id.ivToggleConfirm)
        val btnSignUp = view.findViewById<TextView>(R.id.btnSignUp)
        val tvConfirmError = view.findViewById<TextView>(R.id.tvConfirmError)
        val confirmContainer = view.findViewById<View>(R.id.confirmContainer)

        var passVisible = false
        var confirmVisible = false

        fun setPasswordVisible(editText: EditText, icon: ImageView, visible: Boolean) {
            val start = editText.selectionStart
            val end = editText.selectionEnd
            editText.transformationMethod = if (visible) null else PasswordTransformationMethod.getInstance()
            editText.setSelection(start, end)
            icon.setImageResource(if (visible) R.drawable.ic_visibility else R.drawable.ic_visibility_off)
        }

        ivTogglePass.setOnClickListener {
            passVisible = !passVisible
            setPasswordVisible(etPassword, ivTogglePass, passVisible)
        }

        ivToggleConfirm.setOnClickListener {
            confirmVisible = !confirmVisible
            setPasswordVisible(etConfirm, ivToggleConfirm, confirmVisible)
        }

        fun clearError() {
            tvConfirmError.visibility = View.GONE
            confirmContainer.setBackgroundResource(R.drawable.input_border)
        }

        fun showMismatchError() {
            tvConfirmError.text = "Both passwords are different"
            tvConfirmError.visibility = View.VISIBLE
            confirmContainer.setBackgroundResource(R.drawable.input_border_error)
        }

        btnSignUp.setOnClickListener {
            val p1 = etPassword.text?.toString() ?: ""
            val p2 = etConfirm.text?.toString() ?: ""
            if (p1.isNotEmpty() && p2.isNotEmpty() && p1 == p2) {
                clearError()
                findNavController().navigate(R.id.action_fragment_signup_to_fragment_otp)
            } else {
                showMismatchError()
            }
        }
    }
}
