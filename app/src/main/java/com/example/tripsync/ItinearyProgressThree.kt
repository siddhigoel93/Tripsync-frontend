package com.example.tripsync

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

class ItinearyProgressThree : Fragment(R.layout.fragment_ai_itinerary_step3) {

    private lateinit var tripTitle: TextView
    private lateinit var tripMeta: TextView
    private lateinit var tripDate: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tripTitle = view.findViewById(R.id.tripTitle)
        tripMeta = view.findViewById(R.id.tripMeta)
        tripDate = view.findViewById(R.id.tripDate)

        val tripName = arguments?.getString("tripName").orEmpty()
        val startDate = arguments?.getString("startDate").orEmpty()
        val endDate = arguments?.getString("endDate").orEmpty()
        val preference = arguments?.getString("preference").orEmpty()

        if (tripName.isNotBlank()) tripTitle.text = tripName

        if (startDate.isNotBlank() && endDate.isNotBlank()) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val start = runCatching { sdf.parse(startDate) }.getOrNull()
            val end = runCatching { sdf.parse(endDate) }.getOrNull()
            if (start != null && end != null) {
                val diffDays = (abs(end.time - start.time) / (1000L * 60 * 60 * 24)).toInt()
                val pref = if (preference.isBlank()) "Adventure" else preference
                tripMeta.text = "$diffDays days  •  $pref"

                val fmtStart = SimpleDateFormat("MMM d", Locale.getDefault()).format(start)
                val sameMonth = SimpleDateFormat("MM", Locale.getDefault()).format(start) ==
                        SimpleDateFormat("MM", Locale.getDefault()).format(end)
                val fmtEnd = if (sameMonth)
                    SimpleDateFormat("d", Locale.getDefault()).format(end)
                else
                    SimpleDateFormat("MMM d", Locale.getDefault()).format(end)
                tripDate.text = "$fmtStart - $fmtEnd"
            }
        }

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        view.findViewById<View>(R.id.btnCreateTrip).setOnClickListener {
            showSuccessDialog()
        }

        view.findViewById<View>(R.id.btnSaveDraft).setOnClickListener { }

        view.findViewById<View>(R.id.btnDownload).setOnClickListener { }
    }

    private fun showSuccessDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_trip_success)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.findViewById<View>(R.id.btnBackHome).setOnClickListener {
            dialog.dismiss()
            findNavController().navigate(R.id.homeFragment)
        }

        dialog.show()
    }
}
