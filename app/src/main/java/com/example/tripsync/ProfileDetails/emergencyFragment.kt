package com.example.tripsync.ProfileDetails

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tripsync.R
import com.google.android.material.button.MaterialButton

class EmergencyFragment : Fragment(R.layout.fragment_emergency) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val inputBloodGroup = view.findViewById<EditText>(R.id.inputBloodGroup)
        val inputRelationship = view.findViewById<EditText>(R.id.inputRelationship)
        val btn = view.findViewById<MaterialButton>(R.id.btn)
        val allergies = view.findViewById<EditText>(R.id.inputAllergies)

        btn.setOnClickListener {
            findNavController().navigate(R.id.action_emergencyFragment_to_preferencesFragment)
        }

        allergies.setOnClickListener {
            allergies.setBackgroundResource(R.drawable.edittext_selected)
        }
        inputBloodGroup.setOnClickListener {
            showSelectionDialog(
                optionsArrayId = R.array.blood_group_options,
                targetView = inputBloodGroup
            )
        }
        inputRelationship.setOnClickListener {
            showSelectionDialog(
                optionsArrayId = R.array.relationship_options,
                targetView = inputRelationship
            )
        }
    }

    private fun showSelectionDialog(
        optionsArrayId: Int,
        targetView: EditText
    ) {
        val options = resources.getStringArray(optionsArrayId)
        val currentSelectionIndex = options.indexOf(targetView.text.toString())
        AlertDialog.Builder(requireContext())
            .setSingleChoiceItems(options, currentSelectionIndex) { dialog, which ->
                targetView.setText(options[which])
                dialog.dismiss()
            }
            .show()
    }
}