package com.example.tripsync

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

class BudgetOverviewFragment : Fragment(R.layout.fragment_budget_overview) {

    private lateinit var title: TextView
    private lateinit var sub: TextView
    private lateinit var dates: TextView
    private lateinit var amount: TextView
    private lateinit var subtitleAmount: TextView
    private lateinit var chipAllocation: TextView
    private lateinit var chipDivisions: TextView
    private lateinit var adapter: BudgetCategoryAdapter

    private var tripName: String = ""
    private var startDate: String = ""
    private var endDate: String = ""
    private var preference: String = ""
    private var totalBudget: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tripName = arguments?.getString("tripName").orEmpty()
        startDate = arguments?.getString("startDate").orEmpty()
        endDate = arguments?.getString("endDate").orEmpty()
        preference = arguments?.getString("preference").orEmpty()
        totalBudget = arguments?.getString("totalBudget").orEmpty()

        title = view.findViewById(R.id.bo_trip_title)
        sub = view.findViewById(R.id.bo_trip_sub)
        dates = view.findViewById(R.id.bo_trip_dates)
        amount = view.findViewById(R.id.bo_total_amount)
        subtitleAmount = view.findViewById(R.id.bo_total_subtitle)
        chipAllocation = view.findViewById(R.id.bo_chip_allocation)
        chipDivisions = view.findViewById(R.id.bo_chip_divisions)

        adapter = BudgetCategoryAdapter()
        view.findViewById<RecyclerView>(R.id.bo_breakdown_recycler).adapter = adapter
        adapter.setTotalBudget(totalBudget.toLongOrNull() ?: 0L)

        adapter.onListChanged = { updateChips() }

        view.findViewById<View>(R.id.bo_edit).setOnClickListener {
            BudgetEditDialogFragment.newInstance(totalBudget).show(parentFragmentManager, "edit_budget")
        }

        setFragmentResultListener("budget_update") { _, bundle ->
            totalBudget = bundle.getString("totalBudget").orEmpty()
            findNavController().previousBackStackEntry?.savedStateHandle?.set("totalBudget", totalBudget)
            renderTop()
            adapter.setTotalBudget(totalBudget.toLongOrNull() ?: 0L)
        }

        view.findViewById<View>(R.id.bo_add_category).setOnClickListener {
            AddCategoryDialogFragment.newInstance().show(parentFragmentManager, "add_category")
        }

        setFragmentResultListener("category_add_result") { _, bundle ->
            val pickedTitle = bundle.getString("title").orEmpty()
            val percent = bundle.getInt("percent")
            val iconRes = bundle.getInt("iconRes")
            adapter.addCategory(BudgetCategory(pickedTitle, percent, iconRes))
        }

        view.findViewById<View>(R.id.bo_back).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        renderTop()
        updateChips()
    }

    private fun updateChips() {
        chipAllocation.text = "${adapter.totalPercent()}% budget allocation"
        chipDivisions.text = "${adapter.count()} Division Categories"
    }

    private fun renderTop() {
        if (tripName.isNotBlank()) title.text = tripName

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val start = runCatching { sdf.parse(startDate) }.getOrNull()
        val end = runCatching { sdf.parse(endDate) }.getOrNull()
        var days = 0
        if (start != null && end != null) {
            days = (abs(end.time - start.time) / (1000L * 60 * 60 * 24)).toInt()
            val m1 = SimpleDateFormat("MMM", Locale.getDefault()).format(start)
            val d1 = SimpleDateFormat("d", Locale.getDefault()).format(start)
            val m2 = SimpleDateFormat("MMM", Locale.getDefault()).format(end)
            val d2 = SimpleDateFormat("d", Locale.getDefault()).format(end)
            dates.text = if (m1 == m2) "$m1 $d1 - $d2" else "$m1 $d1 - $m2 $d2"
        }

        val pref = if (preference.isBlank()) "Adventure" else preference
        sub.text = "$days days  •  $pref"

        val nf = NumberFormat.getInstance(Locale("en", "IN"))
        val amountLong = totalBudget.toLongOrNull() ?: 0L
        amount.text = "₹" + nf.format(amountLong)
        subtitleAmount.text = "for a solo trip • $days days"
    }
}
