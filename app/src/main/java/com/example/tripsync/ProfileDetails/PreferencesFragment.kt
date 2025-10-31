package com.example.tripsync.ProfileDetails

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import com.example.tripsync.R
import com.google.android.material.button.MaterialButton
import android.widget.Toast // Import Toast for validation feedback

class PreferencesFragment : Fragment() {
    private var selectedCardId: Int? = null

    private val preferenceMap = mapOf(
        R.id.cardAdventure to "Adventure",
        R.id.cardRelaxation to "Relaxation",
        R.id.cardNature to "Nature",
        R.id.cardExplore to "Explore",
        R.id.cardSpiritual to "Spiritual",
        R.id.cardHistoric to "Historic"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_preferences, container, false)
        val prev_btn = view.findViewById<MaterialButton>(R.id.btnPrevious)
        val btn = view.findViewById<MaterialButton>(R.id.btn)

        val cards = listOf(
            view.findViewById<ConstraintLayout>(R.id.cardAdventure),
            view.findViewById<ConstraintLayout>(R.id.cardRelaxation),
            view.findViewById<ConstraintLayout>(R.id.cardNature),
            view.findViewById<ConstraintLayout>(R.id.cardExplore),
            view.findViewById<ConstraintLayout>(R.id.cardSpiritual),
            view.findViewById<ConstraintLayout>(R.id.cardHistoric)
        )

        cards.forEach { card ->
            card.setOnClickListener {
                selectedCardId?.let { prevId ->
                    if (prevId != card.id) {
                        view.findViewById<ConstraintLayout>(prevId).isSelected = false
                    }
                }
                val isSelected = !card.isSelected
                card.isSelected = isSelected

                selectedCardId = if (isSelected) card.id else null
            }
        }

        btn.setOnClickListener {
            val selectedPreference = selectedCardId?.let { id ->
                preferenceMap[id]
            }

            if (selectedPreference != null) {
                val bundle = Bundle().apply {
                    putString("travel_preference", selectedPreference)
                }
                findNavController().navigate(R.id.action_preferencesFragment_to_contactVerifyFragment, bundle)
            } else {
                Toast.makeText(context, "Please select one preference.", Toast.LENGTH_SHORT).show()
            }
        }


        prev_btn.setOnClickListener {
            findNavController().navigate(R.id.action_preferencesFragment_to_emergencyFragment)
        }

        return view
    }

}