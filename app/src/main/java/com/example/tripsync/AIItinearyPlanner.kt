package com.example.tripsync

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AIItinearyPlannerFragment : Fragment() {
    private var editTextValue1: String = ""
    private var editTextValue2: String = ""
    private var editTextValue3: String = ""

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val startCalendar: Calendar = Calendar.getInstance()
    private val endCalendar: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_a_i_itineary_planner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val etTripName: EditText = view.findViewById(R.id.rkfj7pis4rom)
        val etCurrentLocation: EditText = view.findViewById(R.id.rcv1e9lg3qio)
        val etDestination: EditText = view.findViewById(R.id.rslzomi0ihm8)
        val etStartDate: EditText = view.findViewById(R.id.r_start_date)
        val etEndDate: EditText = view.findViewById(R.id.r_end_date)

        val emojiAndSpacesFilter = InputFilter { source, start, end, dest, dstart, dend ->
            val sb = StringBuilder()
            var i = start
            while (i < end) {
                val cp = Character.codePointAt(source, i)
                val charCount = Character.charCount(cp)
                val part = String(Character.toChars(cp))
                if (cp <= 0xFFFF) sb.append(part)
                i += charCount
            }
            val filtered = sb.toString()
            if (dest.isEmpty() && filtered.trim().isEmpty()) {
                ""
            } else {
                if (filtered == source.toString()) null else filtered
            }
        }

        etTripName.filters = arrayOf(emojiAndSpacesFilter)
        etCurrentLocation.filters = arrayOf(emojiAndSpacesFilter)
        etDestination.filters = arrayOf(emojiAndSpacesFilter)

        etTripName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { editTextValue1 = s.toString() }
            override fun afterTextChanged(s: Editable?) {}
        })
        etCurrentLocation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { editTextValue2 = s.toString() }
            override fun afterTextChanged(s: Editable?) {}
        })
        etDestination.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { editTextValue3 = s.toString() }
            override fun afterTextChanged(s: Editable?) {}
        })

        etStartDate.isFocusable = false
        etStartDate.isClickable = true
        etStartDate.setOnClickListener {
            val now = Calendar.getInstance()
            val dp = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                startCalendar.set(year, month, dayOfMonth)
                etStartDate.setText(dateFormat.format(startCalendar.time))
                if (endCalendar.before(startCalendar)) {
                    endCalendar.time = startCalendar.time
                    etEndDate.setText(dateFormat.format(endCalendar.time))
                }
            }, startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH))
            dp.datePicker.minDate = now.timeInMillis
            dp.show()
        }

        etEndDate.isFocusable = false
        etEndDate.isClickable = true
        etEndDate.setOnClickListener {
            val minDate = if (etStartDate.text.isNullOrBlank()) Calendar.getInstance().timeInMillis else startCalendar.timeInMillis
            val dp = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                endCalendar.set(year, month, dayOfMonth)
                etEndDate.setText(dateFormat.format(endCalendar.time))
            }, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH))
            dp.datePicker.minDate = minDate
            dp.show()
        }

        view.findViewById<View>(R.id.rr7hpem12xxh).setOnClickListener { it.isSelected = !it.isSelected }
        view.findViewById<View>(R.id.rqwfe5qhxt1).setOnClickListener { it.isSelected = !it.isSelected }
        view.findViewById<View>(R.id.r30npb48x1w7).setOnClickListener { it.isSelected = !it.isSelected }
        view.findViewById<View>(R.id.rrqagbhzn0gj).setOnClickListener { it.isSelected = !it.isSelected }
        view.findViewById<View>(R.id.rdd7g48bxhrn).setOnClickListener { it.isSelected = !it.isSelected }
        view.findViewById<View>(R.id.reqgbovj3s1i).setOnClickListener { it.isSelected = !it.isSelected }

        view.findViewById<View>(R.id.btn_continue_budget).setOnClickListener { Toast.makeText(requireContext(), "Continue", Toast.LENGTH_SHORT).show() }
        view.findViewById<View>(R.id.btn_save_draft).setOnClickListener { Toast.makeText(requireContext(), "Draft Saved", Toast.LENGTH_SHORT).show() }
    }
}
