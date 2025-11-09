package com.example.tripsync

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputFilter
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment

class BudgetEditDialogFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        return inflater.inflate(R.layout.dialog_budget_edit, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val input = view.findViewById<EditText>(R.id.de_input)
        val chip500 = view.findViewById<TextView>(R.id.de_chip_500)
        val chip1000 = view.findViewById<TextView>(R.id.de_chip_1000)
        val chip2000 = view.findViewById<TextView>(R.id.de_chip_2000)
        val done = view.findViewById<TextView>(R.id.de_done)
        val cancel = view.findViewById<TextView>(R.id.de_cancel)

        val prefill = arguments?.getString("amount").orEmpty()
        if (prefill.isNotBlank()) {
            input.setText(prefill.filter { it.isDigit() })
            input.setSelection(input.text?.length ?: 0)
        }

        input.setBackgroundResource(R.drawable.budget_input_box_selector)
        input.keyListener = DigitsKeyListener.getInstance("0123456789")
        input.filters = arrayOf(InputFilter.LengthFilter(12))

        fun add(amount: Int) {
            val base = input.text.toString().filter { it.isDigit() }.toLongOrNull() ?: 0L
            val sum = base + amount
            input.setText(sum.toString())
            input.setSelection(input.text.length)
        }

        chip500.setOnClickListener { add(500) }
        chip1000.setOnClickListener { add(1000) }
        chip2000.setOnClickListener { add(2000) }

        done.setOnClickListener {
            val value = input.text.toString().filter { it.isDigit() }
            parentFragmentManager.setFragmentResult("budget_update", bundleOf("totalBudget" to value))
            dismiss()
        }

        cancel.setOnClickListener { dismiss() }
    }

    companion object {
        fun newInstance(currentAmount: String): BudgetEditDialogFragment {
            val dg = BudgetEditDialogFragment()
            dg.arguments = bundleOf("amount" to currentAmount)
            return dg
        }
    }
}
