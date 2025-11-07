package com.example.tripsync

import android.os.Bundle
import android.text.InputFilter
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

class BudgetFragment : Fragment(R.layout.activity_budget) {

    private var preference: String = ""
    private lateinit var budgetInput: EditText
    private lateinit var daysText: TextView
    private lateinit var dateText: TextView
    private lateinit var title: TextView
    private var lastSavedBudget: String = ""
    private var currentLocationArg: String = ""
    private var destinationArg: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        title = view.findViewById(R.id.rwenfcmsasp)
        daysText = view.findViewById(R.id.r_trip_days)
        dateText = view.findViewById(R.id.r_trip_date_range)

        val tripName = arguments?.getString("tripName").orEmpty()
        val startDate = arguments?.getString("startDate").orEmpty()
        val endDate = arguments?.getString("endDate").orEmpty()
        preference = arguments?.getString("preference").orEmpty()
        currentLocationArg = arguments?.getString("currentLocation").orEmpty()
        destinationArg = arguments?.getString("destination").orEmpty()

        if (tripName.isNotBlank()) title.text = tripName

        if (startDate.isNotBlank() && endDate.isNotBlank()) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val start = runCatching { sdf.parse(startDate) }.getOrNull()
            val end = runCatching { sdf.parse(endDate) }.getOrNull()
            if (start != null && end != null) {
                val diffDays = (abs(end.time - start.time) / (1000L * 60 * 60 * 24)).toInt()
                val pref = if (preference.isBlank()) "Adventure" else preference
                daysText.text = "$diffDays days  •  $pref"
                val fmtStart = SimpleDateFormat("MMM d", Locale.getDefault()).format(start)
                val sameMonth = SimpleDateFormat("MM", Locale.getDefault()).format(start) ==
                        SimpleDateFormat("MM", Locale.getDefault()).format(end)
                val endFmt = if (sameMonth)
                    SimpleDateFormat("d", Locale.getDefault()).format(end)
                else
                    SimpleDateFormat("MMM d", Locale.getDefault()).format(end)
                dateText.text = "$fmtStart - $endFmt"
            }
        }

        budgetInput = view.findViewById(R.id.r3rxv1dm1581)
        val chip500 = view.findViewById<TextView>(R.id.rdn28kg5a5ln)
        val chip1000 = view.findViewById<TextView>(R.id.rdv457bw9t4e)
        val chip2000 = view.findViewById<TextView>(R.id.r8pj2xl6mt3x)

        budgetInput.setBackgroundResource(R.drawable.budget_input_box_selector)
        budgetInput.keyListener = DigitsKeyListener.getInstance("0123456789")
        budgetInput.filters = arrayOf(InputFilter.LengthFilter(12))

        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<String>("totalBudget")
            ?.observe(viewLifecycleOwner) { newBudget ->
                lastSavedBudget = newBudget
                budgetInput.setText(newBudget)
                budgetInput.setSelection(budgetInput.text.length)
            }

        fun addAmount(amount: Int) {
            val base = budgetInput.text.toString().filter { it.isDigit() }.toLongOrNull() ?: 0L
            val sum = base + amount
            budgetInput.setText(sum.toString())
            budgetInput.setSelection(budgetInput.text.length)
        }

        chip500.setOnClickListener { addAmount(500) }
        chip1000.setOnClickListener { addAmount(1000) }
        chip2000.setOnClickListener { addAmount(2000) }

        view.findViewById<View>(R.id.r4d0brug0pm).setOnClickListener {
            val totalBudget = budgetInput.text.toString().trim()
            val bundle = Bundle().apply {
                putString("tripName", tripName)
                putString("startDate", startDate)
                putString("endDate", endDate)
                putString("preference", preference)
                putString("totalBudget", totalBudget)
                putString("currentLocation", currentLocationArg)
                putString("destination", destinationArg)
            }
            findNavController().navigate(R.id.action_budgetFragment_to_budgetOverviewFragment, bundle)
        }
    }

    override fun onResume() {
        super.onResume()
        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        val restoredBudget = savedStateHandle?.get<String>("totalBudget")
        if (!restoredBudget.isNullOrEmpty() && restoredBudget != lastSavedBudget) {
            budgetInput.setText(restoredBudget)
            budgetInput.setSelection(budgetInput.text.length)
            lastSavedBudget = restoredBudget
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
