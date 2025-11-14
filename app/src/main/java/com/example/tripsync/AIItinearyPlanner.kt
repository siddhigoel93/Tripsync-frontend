package com.example.tripsync

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tripsync.utils.DialogUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

class AIItinearyPlannerFragment : Fragment() {
    private var editTextValue1: String = ""
    private var editTextValue2: String = ""
    private var editTextValue3: String = ""
    private var selectedPreference: String = ""
    private var selectedTripType: String = ""
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val startCalendar: Calendar = Calendar.getInstance()
    private val endCalendar: Calendar = Calendar.getInstance()
    private lateinit var tripLenTitle: TextView
    private lateinit var tripLenSubtitle: TextView
    private val oneDayMillis = 24L * 60L * 60L * 1000L
    private val maxDays = 6

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_a_i_itineary_planner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etTripName: EditText = view.findViewById(R.id.rkfj7pis4rom)
        val etCurrentLocation: EditText = view.findViewById(R.id.rcv1e9lg3qio)
        val etDestination: EditText = view.findViewById(R.id.rslzomi0ihm8)
        val etStartDate: EditText = view.findViewById(R.id.r_start_date)
        val etEndDate: EditText = view.findViewById(R.id.r_end_date)

        tripLenTitle = view.findViewById(R.id.r_trip_length_title)
        tripLenSubtitle = view.findViewById(R.id.r_trip_length_subtitle)

        tripLenTitle.text = "0 Day Trip"
        tripLenSubtitle.text = ""

        val tripBusiness: View = view.findViewById(R.id.rrqagbhzn0gj)
        val tripGroup: View = view.findViewById(R.id.rdd7g48bxhrn)
        val tripSolo: View = view.findViewById(R.id.reqgbovj3s1i)

        val prefAdventure: View = view.findViewById(R.id.rr7hpem12xxh)
        val prefRelax: View = view.findViewById(R.id.rqwfe5qhxt1)
        val prefSpiritual: View = view.findViewById(R.id.r30npb48x1w7)

        val lettersOnlyFilter = InputFilter { source, start, end, _, _, _ ->
            val sb = StringBuilder()
            var i = start
            while (i < end) {
                val cp = Character.codePointAt(source, i)
                val cc = Character.charCount(cp)
                val ch = cp.toChar()
                if (Character.isLetter(cp) || ch == ' ') sb.appendCodePoint(cp)
                i += cc
            }
            sb.toString()
        }

        etTripName.filters = arrayOf(lettersOnlyFilter, InputFilter.LengthFilter(60))
        etCurrentLocation.filters = arrayOf(lettersOnlyFilter, InputFilter.LengthFilter(60))
        etDestination.filters = arrayOf(lettersOnlyFilter, InputFilter.LengthFilter(60))

        etTripName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                editTextValue1 = s?.toString().orEmpty()
                tripLenSubtitle.text = if (editTextValue1.isNotBlank()) "A perfect time to explore $editTextValue1" else ""
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        etCurrentLocation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { editTextValue2 = s?.toString().orEmpty() }
            override fun afterTextChanged(s: Editable?) {}
        })
        etDestination.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { editTextValue3 = s?.toString().orEmpty() }
            override fun afterTextChanged(s: Editable?) {}
        })

        fun selectOnlyType(selected: View, group: List<View>, label: String) {
            group.forEach { it.isSelected = (it == selected) }
            selectedTripType = label
        }
        tripBusiness.setOnClickListener { selectOnlyType(it, listOf(tripBusiness, tripGroup, tripSolo), "Business") }
        tripGroup.setOnClickListener { selectOnlyType(it, listOf(tripBusiness, tripGroup, tripSolo), "Group") }
        tripSolo.setOnClickListener { selectOnlyType(it, listOf(tripBusiness, tripGroup, tripSolo), "Solo") }

        fun selectOnlyPref(selected: View, label: String) {
            listOf(prefAdventure, prefRelax, prefSpiritual).forEach { it.isSelected = (it == selected) }
            selectedPreference = label
        }
        prefAdventure.setOnClickListener { selectOnlyPref(prefAdventure, "Adventure") }
        prefRelax.setOnClickListener { selectOnlyPref(prefRelax, "Relaxation") }
        prefSpiritual.setOnClickListener { selectOnlyPref(prefSpiritual, "Spiritual") }

        etTripName.setOnFocusChangeListener { v, hasFocus ->
            v.isSelected = hasFocus
            v.refreshDrawableState()
            if (hasFocus) etTripName.post { showKeyboardFor(etTripName) }
        }
        etCurrentLocation.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) etCurrentLocation.post { showKeyboardFor(etCurrentLocation) } }
        etDestination.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) etDestination.post { showKeyboardFor(etDestination) } }

        etStartDate.isFocusable = false
        etStartDate.isClickable = true
        etEndDate.isFocusable = false
        etEndDate.isClickable = true

        fun inclusiveDays(): Int {
            val diff = max(0L, endCalendar.timeInMillis - startCalendar.timeInMillis)
            val days = (diff / oneDayMillis).toInt() + 1
            return min(days, maxDays)
        }

        fun updateTripLength() {
            val days = inclusiveDays()
            val label = if (days == 1) "1 Day Trip" else "$days Day Trip"
            tripLenTitle.text = label
        }

        val startClick = View.OnClickListener {
            val now = Calendar.getInstance()

            DialogUtils.showDatePicker(
                requireContext(),
                initialCalendar = startCalendar,
                minDate = now.timeInMillis,
                onDateSelected = { year, month, dayOfMonth ->
                    startCalendar.set(year, month, dayOfMonth, 0, 0, 0)
                    etStartDate.setText(dateFormat.format(startCalendar.time))
                    val maxEndAllowed = startCalendar.timeInMillis + (maxDays - 1) * oneDayMillis
                    if (endCalendar.timeInMillis < startCalendar.timeInMillis) {
                        endCalendar.timeInMillis = startCalendar.timeInMillis
                        etEndDate.setText(dateFormat.format(endCalendar.time))
                    } else if (endCalendar.timeInMillis > maxEndAllowed) {
                        endCalendar.timeInMillis = maxEndAllowed
                        etEndDate.setText(dateFormat.format(endCalendar.time))
                    }
                    updateTripLength()
                }
            )
        }
        etStartDate.setOnClickListener(startClick)

        val endClick = View.OnClickListener {
            val minDate = if (etStartDate.text.isNullOrBlank()) Calendar.getInstance().timeInMillis else startCalendar.timeInMillis
            val maxEndAllowed = if (etStartDate.text.isNullOrBlank()) Long.MAX_VALUE else startCalendar.timeInMillis + (maxDays - 1) * oneDayMillis

            DialogUtils.showDatePicker(
                requireContext(),
                initialCalendar = endCalendar,
                minDate = minDate,
                maxDate = if (maxEndAllowed != Long.MAX_VALUE) maxEndAllowed else null,
                onDateSelected = { year, month, dayOfMonth ->
                    endCalendar.set(year, month, dayOfMonth, 0, 0, 0)
                    if (endCalendar.timeInMillis < startCalendar.timeInMillis) {
                        Toast.makeText(requireContext(), "End date must be after start date", Toast.LENGTH_SHORT).show()
                        return@showDatePicker
                    }
                    val diffMillis = endCalendar.timeInMillis - startCalendar.timeInMillis
                    val daysInc = (diffMillis / oneDayMillis).toInt() + 1
                    if (daysInc > maxDays) {
                        Toast.makeText(requireContext(), "Trip length cannot exceed $maxDays days", Toast.LENGTH_SHORT).show()
                        endCalendar.timeInMillis = startCalendar.timeInMillis + (maxDays - 1) * oneDayMillis
                    }
                    etEndDate.setText(dateFormat.format(endCalendar.time))
                    updateTripLength()
                }
            )
        }
        etEndDate.setOnClickListener(endClick)

        view.findViewById<View>(R.id.btn_continue_budget).setOnClickListener {
            val tripName = etTripName.text.toString().trim()
            val currentLocation = etCurrentLocation.text.toString().trim()
            val destination = etDestination.text.toString().trim()
            val startText = etStartDate.text.toString().trim()
            val endText = etEndDate.text.toString().trim()

            val hasLetters = { s: String -> s.any { it.isLetter() } }
            val allFilled = tripName.isNotEmpty() && currentLocation.isNotEmpty() && destination.isNotEmpty() && startText.isNotEmpty() && endText.isNotEmpty() && selectedPreference.isNotEmpty() && selectedTripType.isNotEmpty()
            val onlyLettersOk = hasLetters(tripName) && hasLetters(currentLocation) && hasLetters(destination)
            val datesValid = if (allFilled) {
                val s = runCatching { dateFormat.parse(startText) }.getOrNull()
                val e = runCatching { dateFormat.parse(endText) }.getOrNull()
                s != null && e != null && e.time >= s.time
            } else false

            if (!allFilled || !onlyLettersOk) {
                Toast.makeText(requireContext(), "Please enter letters only and fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!datesValid) {
                Toast.makeText(requireContext(), "Select valid travel dates", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val totalDays = inclusiveDays()
            if (totalDays > maxDays) {
                Toast.makeText(requireContext(), "Trip length cannot exceed $maxDays days", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bundle = Bundle().apply {
                putString("tripName", tripName)
                putString("startDate", startText)
                putString("endDate", endText)
                putString("preference", selectedPreference.lowercase(Locale.getDefault()))
                putString("currentLocation", currentLocation)
                putString("destination", destination)
                putString("tripType", selectedTripType)
            }
            findNavController().navigate(R.id.action_AIItinearyPlannerFragment_to_budgetFragment, bundle)
        }

        view.findViewById<View>(R.id.btn_save_draft).setOnClickListener {
            Toast.makeText(requireContext(), "Draft Saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showKeyboardFor(view: View) {
        view.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}