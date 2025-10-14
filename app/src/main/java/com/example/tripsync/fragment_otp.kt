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
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class fragment_otp : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_otp, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val et1 = view.findViewById<EditText>(R.id.et1)
        val et2 = view.findViewById<EditText>(R.id.et2)
        val et3 = view.findViewById<EditText>(R.id.et3)
        val et4 = view.findViewById<EditText>(R.id.et4)
        val et5 = view.findViewById<EditText>(R.id.et5)
        val et6 = view.findViewById<EditText>(R.id.et6)
        val tvBack = view.findViewById<TextView>(R.id.tvBackToLogin)

        val boxes = listOf(et1, et2, et3, et4, et5, et6)

        boxes.forEachIndexed { index, et ->
            et.filters = arrayOf(InputFilter.LengthFilter(1))
            et.inputType = android.text.InputType.TYPE_CLASS_NUMBER
            et.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && index < boxes.lastIndex) {
                        boxes[index + 1].requestFocus()
                    }
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
        }

        et1.requestFocus()

        tvBack.setOnClickListener {
            findNavController().navigate(R.id.action_fragment_otp_to_fragment_signup)
        }
    }
}
