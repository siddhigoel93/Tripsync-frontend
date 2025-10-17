package com.example.tripsync.Auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.tripsync.R

class fragment_signup : Fragment() {

    private var passVisible = false
    private var confirmVisible = false

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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_signup, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val etConfirm = view.findViewById<EditText>(R.id.etConfirm)
        val ivTogglePass = view.findViewById<ImageView>(R.id.ivTogglePass)
        val ivToggleConfirm = view.findViewById<ImageView>(R.id.ivToggleConfirm)
        val btnSignUp = view.findViewById<TextView>(R.id.btnSignUp)
        val tvEmailError = view.findViewById<TextView>(R.id.tvEmailError)
        val tvConfirmError = view.findViewById<TextView>(R.id.tvConfirmError)
        val confirmContainer = view.findViewById<View>(R.id.confirmContainer)
        val scrollView = view.findViewById<ScrollView>(R.id.scrollView)
        val signUpCard = view.findViewById<View>(R.id.signUpCard)
        val headline = view.findViewById<TextView>(R.id.headline)
        val subText = view.findViewById<TextView>(R.id.subText)
        val signin = view.findViewById<TextView>(R.id.tvSignIn)

        signin.setOnClickListener {
            view.findNavController().navigate(R.id.action_fragment_signup_to_login)
        }

        fun setPasswordVisible(editText: EditText, icon: ImageView, visible: Boolean) {
            val start = editText.selectionStart
            val end = editText.selectionEnd
            editText.transformationMethod =
                if (visible) null else AsteriskPasswordTransformation()
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

        fun clearErrors() {
            tvEmailError.visibility = View.GONE
            tvConfirmError.visibility = View.GONE
            etEmail.setBackgroundResource(R.drawable.input_border)
            confirmContainer.setBackgroundResource(R.drawable.input_border)
        }

        fun showEmailError() {
            tvEmailError.visibility = View.VISIBLE
            etEmail.setBackgroundResource(R.drawable.input_border_error)
        }

        fun showPasswordError(message: String) {
            tvConfirmError.text = message
            tvConfirmError.visibility = View.VISIBLE
            confirmContainer.setBackgroundResource(R.drawable.input_border_error)
        }

        fun isEmailValid(email: String): Boolean {
            val pattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
            return pattern.matches(email)
        }

        fun isPasswordFormatValid(p: String): Boolean {
            val pattern = Regex("^(?=\\S+\$)(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,}\$")
            return pattern.matches(p)
        }

        fun showPasswordNoteDialog() {
            val dialogView = layoutInflater.inflate(R.layout.dialog_password_note, null)
            val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
            dialogView.findViewById<TextView>(R.id.btnCloseNote).setOnClickListener { dialog.dismiss() }
            dialog.show()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        btnSignUp.setOnClickListener {
            val email = etEmail.text?.toString()?.trim() ?: ""
            val p1 = etPassword.text?.toString() ?: ""
            val p2 = etConfirm.text?.toString() ?: ""

            clearErrors()

            if (!isEmailValid(email)) {
                showEmailError()
                return@setOnClickListener
            }

            if (!isPasswordFormatValid(p1)) {
                showPasswordNoteDialog()
                showPasswordError("Password must be at least 8 characters, contain a number and a special character, and have no spaces")
                return@setOnClickListener
            }

            if (p1.isNotEmpty() && p2.isNotEmpty() && p1 == p2) {
                findNavController().navigate(R.id.action_fragment_signup_to_fragment_otp)
            } else {
                showPasswordError("Both passwords are different")
            }
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                clearErrors()
            }
        }

        etEmail.addTextChangedListener(watcher)
        etPassword.addTextChangedListener(watcher)
        etConfirm.addTextChangedListener(watcher)

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val keyboardHeight = ime.bottom
            if (keyboardHeight > 0) {
                signUpCard.animate().translationY(-keyboardHeight * 0.6f).setDuration(200).start()
                headline.animate().translationY(-keyboardHeight * 0.28f).scaleX(0.88f).scaleY(0.88f).setDuration(200).start()
                subText.animate().alpha(0.0f).setDuration(200).start()
                scrollView.postDelayed({
                    scrollView.smoothScrollTo(0, signUpCard.top)
                }, 160)
            } else {
                signUpCard.animate().translationY(0f).setDuration(200).start()
                headline.animate().translationY(0f).scaleX(1f).scaleY(1f).setDuration(200).start()
                subText.animate().alpha(1.0f).setDuration(200).start()
            }
            insets
        }
    }
}