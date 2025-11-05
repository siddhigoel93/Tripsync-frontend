package com.example.tripsync

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputFilter
import android.text.method.DigitsKeyListener
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.setPadding
import androidx.fragment.app.DialogFragment

class AddCategoryDialogFragment : DialogFragment() {

    private var selectedTitle: String? = null
    private var selectedIconRes: Int = 0

    override fun onCreateView(inflater: android.view.LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return buildContent()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private fun dp(v: Int): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

    private fun buildContent(): View {
        val root = LinearLayout(requireContext()).apply {
            layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(dp(16))
            orientation = LinearLayout.VERTICAL
        }

        val card = LinearLayout(requireContext()).apply {
            layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            background = requireContext().getDrawable(R.drawable.budget_inner_card_white)
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16))
        }

        val titleTv = TextView(requireContext()).apply {
            text = "Add Category"
            setTextColor(Color.parseColor("#101010"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTypeface(typeface, Typeface.BOLD)
        }

        val picker = TextView(requireContext()).apply {
            text = ""
            hint = "Enter a Category"
            setTextColor(Color.parseColor("#101010"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            gravity = Gravity.CENTER_VERTICAL
            background = requireContext().getDrawable(R.drawable.budget_input_box)
            setPadding(dp(14), 0, dp(14), 0)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)).apply {
                topMargin = dp(12)
            }
            isClickable = true
            isFocusable = true
        }

        val percentLabel = TextView(requireContext()).apply {
            text = "Percentage Share"
            setTextColor(Color.parseColor("#101010"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTypeface(typeface, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(12)
            }
        }

        val percentEt = EditText(requireContext()).apply {
            hint = "Enter percentage"
            setTextColor(Color.parseColor("#101010"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            background = requireContext().getDrawable(R.drawable.budget_input_box)
            setPadding(dp(14), 0, dp(14), 0)
            keyListener = DigitsKeyListener.getInstance("0123456789")
            filters = arrayOf(InputFilter.LengthFilter(3))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)).apply {
                topMargin = dp(12)
            }
        }

        val addBtn = TextView(requireContext()).apply {
            text = "Add Category"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            background = requireContext().getDrawable(R.drawable.budget_button_primary)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)).apply {
                topMargin = dp(16)
            }
        }

        val cancelBtn = TextView(requireContext()).apply {
            text = "Cancel"
            setTextColor(Color.parseColor("#00C896"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            background = requireContext().getDrawable(R.drawable.budget_button_outlined)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)).apply {
                topMargin = dp(12)
            }
        }

        val items = arrayOf("Accomodation", "Food & Dining", "Activity", "Shopping")

        fun resIdOr0(name: String) = resources.getIdentifier(name, "drawable", requireContext().packageName)

        fun iconFor(which: Int): Int = when (which) {
            0 -> listOf("accomodation", "accomadation").firstNotNullOfOrNull { n -> resIdOr0(n).takeIf { it != 0 } } ?: 0
            1 -> resIdOr0("food_and_dining")
            2 -> resIdOr0("activity")
            else -> resIdOr0("shopping")
        }

        picker.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Select Category")
                .setItems(items) { _, which ->
                    selectedTitle = items[which]
                    selectedIconRes = iconFor(which)
                    picker.text = selectedTitle
                }
                .show()
        }

        addBtn.setOnClickListener {
            val p = percentEt.text.toString().toIntOrNull() ?: -1
            val ok = p in 1..100 && !selectedTitle.isNullOrBlank() && selectedIconRes != 0
            if (ok) {
                parentFragmentManager.setFragmentResult(
                    "category_add_result",
                    bundleOf(
                        "title" to selectedTitle,
                        "percent" to p,
                        "iconRes" to selectedIconRes
                    )
                )
                dismiss()
            }
        }

        cancelBtn.setOnClickListener { dismiss() }

        card.addView(titleTv)
        card.addView(picker)
        card.addView(percentLabel)
        card.addView(percentEt)
        card.addView(addBtn)
        card.addView(cancelBtn)
        root.addView(card)
        return root
    }

    companion object {
        fun newInstance(): AddCategoryDialogFragment = AddCategoryDialogFragment()
    }
}
