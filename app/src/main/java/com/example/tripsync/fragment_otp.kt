package com.example.tripsync

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController

class fragment_otp : Fragment() {

    private lateinit var et1: EditText
    private lateinit var et2: EditText
    private lateinit var et3: EditText
    private lateinit var et4: EditText
    private lateinit var et5: EditText
    private lateinit var et6: EditText
    private lateinit var boxes: List<EditText>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_otp, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        et1 = view.findViewById(R.id.et1)
        et2 = view.findViewById(R.id.et2)
        et3 = view.findViewById(R.id.et3)
        et4 = view.findViewById(R.id.et4)
        et5 = view.findViewById(R.id.et5)
        et6 = view.findViewById(R.id.et6)
        val scrollView = view.findViewById<ScrollView>(R.id.scrollViewOtp)
        val otpCard = view.findViewById<View>(R.id.otpCard)
        val headline = view.findViewById<TextView>(R.id.headline)
        val subText = view.findViewById<TextView>(R.id.subText)
        val backtologin = view.findViewById<TextView>(R.id.tvBackToLogin)

        backtologin.setOnClickListener {
            view.findNavController().navigate(R.id.action_fragment_signup_to_login)
        }

        boxes = listOf(et1, et2, et3, et4, et5, et6)

        boxes.forEachIndexed { index, et ->
            et.filters = arrayOf(InputFilter.LengthFilter(1))
            et.inputType = android.text.InputType.TYPE_CLASS_NUMBER
            et.isCursorVisible = false
            et.setTextColor(resources.getColor(android.R.color.black))
            et.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && index < boxes.lastIndex) {
                        boxes[index + 1].requestFocus()
                    }
                    updateFocusVisual(index)
                }
            })
            et.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (et.text.isEmpty() && index > 0) {
                        boxes[index - 1].apply {
                            requestFocus()
                            setSelection(text.length)
                        }
                        return@setOnKeyListener true
                    }
                }
                false
            }
            et.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) updateFocusVisual(index)
            }
        }

        et1.requestFocus()


        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val keyboardHeight = imeInsets.bottom
            if (keyboardHeight > 0) {
                otpCard.animate().translationY(-keyboardHeight * 0.55f).setDuration(200).start()
                headline.animate().translationY(-keyboardHeight * 0.28f).scaleX(0.95f).scaleY(0.95f).setDuration(200).start()
                subText.animate().alpha(0.0f).setDuration(200).start()
                scrollView.postDelayed({
                    scrollView.smoothScrollTo(0, otpCard.top)
                }, 120)
            } else {
                otpCard.animate().translationY(0f).setDuration(200).start()
                headline.animate().translationY(0f).scaleX(1f).scaleY(1f).setDuration(200).start()
                subText.animate().alpha(1.0f).setDuration(200).start()
            }
            insets
        }
    }

    private fun updateFocusVisual(activeIndex: Int) {
        boxes.forEachIndexed { index, box ->
            if (index == activeIndex) {
                box.isSelected = true
                box.isActivated = true
            } else {
                box.isSelected = false
                box.isActivated = false
            }
        }
    }
}
