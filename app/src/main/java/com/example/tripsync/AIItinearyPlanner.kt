package com.example.tripsync

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast

class AIItinearyPlanner : AppCompatActivity() {
    private var editTextValue1: String = ""
    private var editTextValue2: String = ""
    private var editTextValue3: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_a_i_itineary_planner)

        val editText1: EditText = findViewById(R.id.rkfj7pis4rom)
        editText1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { editTextValue1 = s.toString() }
            override fun afterTextChanged(s: Editable?) {}
        })

        val editText2: EditText = findViewById(R.id.rcv1e9lg3qio)
        editText2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { editTextValue2 = s.toString() }
            override fun afterTextChanged(s: Editable?) {}
        })

        val editText3: EditText = findViewById(R.id.rslzomi0ihm8)
        editText3.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { editTextValue3 = s.toString() }
            override fun afterTextChanged(s: Editable?) {}
        })

        findViewById<View>(R.id.rr7hpem12xxh).setOnClickListener { Toast.makeText(this, "Adventure", Toast.LENGTH_SHORT).show() }
        findViewById<View>(R.id.rqwfe5qhxt1).setOnClickListener { Toast.makeText(this, "Relaxation", Toast.LENGTH_SHORT).show() }
        findViewById<View>(R.id.r30npb48x1w7).setOnClickListener { Toast.makeText(this, "Spiritual", Toast.LENGTH_SHORT).show() }
        findViewById<View>(R.id.rrqagbhzn0gj).setOnClickListener { Toast.makeText(this, "Business Trip", Toast.LENGTH_SHORT).show() }
        findViewById<View>(R.id.rdd7g48bxhrn).setOnClickListener { Toast.makeText(this, "Group Trip", Toast.LENGTH_SHORT).show() }
        findViewById<View>(R.id.reqgbovj3s1i).setOnClickListener { Toast.makeText(this, "Solo Trip", Toast.LENGTH_SHORT).show() }
        findViewById<View>(R.id.btn_continue_budget).setOnClickListener { Toast.makeText(this, "Continue", Toast.LENGTH_SHORT).show() }
        findViewById<View>(R.id.btn_save_draft).setOnClickListener { Toast.makeText(this, "Draft Saved", Toast.LENGTH_SHORT).show() }
    }
}
