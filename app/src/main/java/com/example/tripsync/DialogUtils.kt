package com.example.tripsync.utils

import android.app.DatePickerDialog
import android.content.Context
import android.widget.EditText
import com.example.tripsync.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar

object DialogUtils {

    /**
     * Show a confirmation dialog with custom styling
     */
    fun showConfirmationDialog(
        context: Context,
        title: String,
        message: String,
        positiveButtonText: String = "Confirm",
        negativeButtonText: String = "Cancel",
        onPositiveClick: () -> Unit,
        onNegativeClick: (() -> Unit)? = null
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonText) { dialog, _ ->
                onPositiveClick()
                dialog.dismiss()
            }
            .setNegativeButton(negativeButtonText) { dialog, _ ->
                onNegativeClick?.invoke()
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    /**
     * Show an options dialog (list of choices)
     */
    fun showOptionsDialog(
        context: Context,
        title: String,
        options: Array<String>,
        onOptionSelected: (Int) -> Unit
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setItems(options) { dialog, which ->
                onOptionSelected(which)
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    /**
     * Show an input dialog with EditText
     */
    fun showInputDialog(
        context: Context,
        title: String,
        hint: String = "",
        prefillText: String = "",
        positiveButtonText: String = "Save",
        negativeButtonText: String = "Cancel",
        onPositiveClick: (String) -> Unit,
        onNegativeClick: (() -> Unit)? = null
    ) {
        val input = EditText(context).apply {
            this.hint = hint
            setText(prefillText)
            setSelection(prefillText.length)
            setPadding(60, 40, 60, 40)
            textSize = 16f
        }

        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setView(input)
            .setPositiveButton(positiveButtonText) { dialog, _ ->
                val text = input.text.toString().trim()
                if (text.isNotEmpty()) {
                    onPositiveClick(text)
                    dialog.dismiss()
                }
            }
            .setNegativeButton(negativeButtonText) { dialog, _ ->
                onNegativeClick?.invoke()
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    /**
     * Show a simple alert dialog
     */
    fun showAlertDialog(
        context: Context,
        title: String,
        message: String,
        buttonText: String = "OK",
        onDismiss: (() -> Unit)? = null
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(buttonText) { dialog, _ ->
                onDismiss?.invoke()
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    /**
     * Show an error dialog
     */
    fun showErrorDialog(
        context: Context,
        title: String = "Error",
        message: String,
        buttonText: String = "OK",
        onDismiss: (() -> Unit)? = null
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(buttonText) { dialog, _ ->
                onDismiss?.invoke()
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Show a styled DatePickerDialog
     */

    fun showDatePicker(
        context: Context,
        initialCalendar: Calendar = Calendar.getInstance(),
        minDate: Long? = null,
        maxDate: Long? = null,
        onDateSelected: (year: Int, month: Int, dayOfMonth: Int) -> Unit
    ): DatePickerDialog {

        val activity = context as? android.app.Activity
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            return DatePickerDialog(context) // return dummy
        }

        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val act = context as? android.app.Activity
                if (act != null && !act.isFinishing && !act.isDestroyed) {
                    onDateSelected(year, month, dayOfMonth)
                }
            },
            initialCalendar.get(Calendar.YEAR),
            initialCalendar.get(Calendar.MONTH),
            initialCalendar.get(Calendar.DAY_OF_MONTH)
        )

        minDate?.let { datePickerDialog.datePicker.minDate = it }
        maxDate?.let { datePickerDialog.datePicker.maxDate = it }

        if (!activity.isFinishing && !activity.isDestroyed) {
            datePickerDialog.show()
        }

        return datePickerDialog
    }


    /**
     * Show a loading dialog (simple text-based)
     */
    fun showLoadingDialog(
        context: Context,
        message: String = "Loading..."
    ): com.google.android.material.dialog.MaterialAlertDialogBuilder {
        return MaterialAlertDialogBuilder(context)
            .setMessage(message)
            .setCancelable(false)
    }

    /**
     * Show a custom single choice dialog
     */
    fun showSingleChoiceDialog(
        context: Context,
        title: String,
        options: Array<String>,
        selectedIndex: Int = -1,
        positiveButtonText: String = "OK",
        negativeButtonText: String = "Cancel",
        onItemSelected: (Int) -> Unit
    ) {
        var selected = selectedIndex

        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setSingleChoiceItems(options, selectedIndex) { _, which ->
                selected = which
            }
            .setPositiveButton(positiveButtonText) { dialog, _ ->
                if (selected != -1) {
                    onItemSelected(selected)
                }
                dialog.dismiss()
            }
            .setNegativeButton(negativeButtonText) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    /**
     * Show a multi-choice dialog
     */
    fun showMultiChoiceDialog(
        context: Context,
        title: String,
        options: Array<String>,
        selectedIndices: BooleanArray,
        positiveButtonText: String = "OK",
        negativeButtonText: String = "Cancel",
        onItemsSelected: (List<Int>) -> Unit
    ) {
        val selected = selectedIndices.clone()

        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMultiChoiceItems(options, selected) { _, which, isChecked ->
                selected[which] = isChecked
            }
            .setPositiveButton(positiveButtonText) { dialog, _ ->
                val selectedList = mutableListOf<Int>()
                selected.forEachIndexed { index, isSelected ->
                    if (isSelected) {
                        selectedList.add(index)
                    }
                }
                onItemsSelected(selectedList)
                dialog.dismiss()
            }
            .setNegativeButton(negativeButtonText) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }
}