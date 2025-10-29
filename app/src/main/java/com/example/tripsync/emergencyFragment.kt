package com.example.tripsync
//
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//
//class emergencyFragment : Fragment() {
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_emergency, container, false)
//    }
//
//}

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.tripsync.R

// Assuming you are using ViewBinding or a similar mechanism to access views
// Replace 'R.layout.fragment_emergency' with your actual layout file name
class EmergencyFragment : Fragment(R.layout.fragment_emergency) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find your EditText views by ID from the layout
        val inputBloodGroup = view.findViewById<EditText>(R.id.inputBloodGroup)
        val inputRelationship = view.findViewById<EditText>(R.id.inputRelationship)

        // Set a click listener for the Blood Group field
        inputBloodGroup.setOnClickListener {
            showSelectionDialog(
                title = "Select Blood Group",
                optionsArrayId = R.array.blood_group_options,
                targetView = inputBloodGroup
            )
        }

        // Set a click listener for the Relationship field
        inputRelationship.setOnClickListener {
            showSelectionDialog(
                title = "Select Relationship",
                optionsArrayId = R.array.relationship_options,
                targetView = inputRelationship
            )
        }
    }

    /**
     * Shows a standard Android Single-Choice List Dialog.
     * This is the simplest way to get user selection.
     */
    private fun showSelectionDialog(
        title: String,
        optionsArrayId: Int,
        targetView: EditText
    ) {
        // 1. Get the list of options
        val options = resources.getStringArray(optionsArrayId)

        // 2. Determine the currently selected item for pre-selection
        val currentSelectionIndex = options.indexOf(targetView.text.toString())

        // 3. Build and show the dialog
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            // setSingleChoiceItems handles creating the list and radio buttons
            .setSingleChoiceItems(options, currentSelectionIndex) { dialog, which ->
                // This block executes when a user taps an item in the list
                targetView.setText(options[which]) // Update the EditText with the selected item
                dialog.dismiss() // Close the dialog immediately
            }
            .setNegativeButton("Cancel", null) // Add a standard Cancel button
            .show()
    }
}