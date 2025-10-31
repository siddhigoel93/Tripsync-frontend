package com.example.tripsync.ProfileDetails

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tripsync.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class EmergencyFragment : Fragment(R.layout.fragment_emergency) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val inputBloodGroup = view.findViewById<EditText>(R.id.inputBloodGroup)
        val inputRelationship = view.findViewById<EditText>(R.id.inputRelationship)
        val btn = view.findViewById<MaterialButton>(R.id.btn)
        val btnPrev = view.findViewById<MaterialButton>(R.id.btnPrevious)
        val allergies = view.findViewById<EditText>(R.id.inputAllergies)

        btn.setOnClickListener {
            findNavController().navigate(R.id.action_emergencyFragment_to_preferencesFragment)
        }
        btnPrev.setOnClickListener {
            findNavController().navigate(R.id.action_emergencyFragment_to_fragment_personal_details)
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
        val currentSelection = targetView.text.toString()

        val builder = MaterialAlertDialogBuilder(requireContext())

        val listView = ListView(requireContext()).apply {
            adapter = ArrayAdapter(
                requireContext(),
                R.layout.list_choice,
                android.R.id.text1,
                options
            )
            choiceMode = ListView.CHOICE_MODE_SINGLE

            setBackgroundColor(resources.getColor(android.R.color.white, null))
            val currentSelectionIndex = options.indexOf(currentSelection)
            if (currentSelectionIndex != -1) {
                setItemChecked(currentSelectionIndex, true)
            }
        }

        val dialog = builder.setView(listView).create()
        listView.setOnItemClickListener { _, view, position, _ ->

            targetView.setText(options[position])

            for (i in 0 until listView.childCount) {
                listView.getChildAt(i).isActivated = (i == position)
            }
            dialog.dismiss()
        }

        dialog.show()
    }
}