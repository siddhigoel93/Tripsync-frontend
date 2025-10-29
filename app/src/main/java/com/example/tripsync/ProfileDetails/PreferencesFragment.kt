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

class PreferencesFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_preferences, container, false)
        val prev_btn = view.findViewById<MaterialButton>(R.id.btnPrevious)
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
                card.isSelected = !card.isSelected
            }
        }



        prev_btn.setOnClickListener {
            findNavController().navigate(R.id.action_preferencesFragment_to_emergencyFragment)
        }

        return view
    }

}