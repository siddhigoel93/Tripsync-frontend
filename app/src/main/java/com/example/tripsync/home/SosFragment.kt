package com.example.tripsync.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.AuthService
import com.example.tripsync.api.models.SosRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.ImageView
import com.example.tripsync.R

class EmergencySosFragment : Fragment() {

    private lateinit var sosButton: CardView
    private lateinit var authService: AuthService
    lateinit var close : ImageView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sos, container, false)

        val sharedPrefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val contact_name = view.findViewById<TextView>(R.id.contact_name)
        val contact_number = view.findViewById<TextView>(R.id.contact_number)
        contact_name.text = sharedPrefs.getString("ename", "Relation")
        contact_number.text = sharedPrefs.getString("enumber", "1234567890")

         close = view.findViewById(R.id.icon_close)






        authService = ApiClient.getTokenService(requireContext())

        sosButton = view.findViewById(R.id.button_sos)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sosButton.setOnLongClickListener {
            sendSosAlert()
            true
        }

        sosButton.setOnClickListener {
            Toast.makeText(requireContext(), "Press and HOLD the SOS button to send the alert.", Toast.LENGTH_SHORT).show()
        }
        close.setOnClickListener {
            requireActivity().onBackPressed()
        }

    }

    private fun sendSosAlert() {
        val currentTime = SimpleDateFormat("HH:mm:ss dd-MMM-yyyy", Locale.getDefault()).format(Date())

        val finalMessage = "EMERGENCY: I need urgent help right now! Alert initiated at $currentTime."

        val finalLocation = "Current GPS Location: [Fetching...]"
        Log.d("SOS_FRAGMENT", "Attempting to send: $finalMessage | Location: $finalLocation")

        sosButton.isEnabled = false

        lifecycleScope.launch {
            try {
                val request = SosRequest(finalMessage, finalLocation)
                val response = authService.sendEmergencySos(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Toast.makeText(
                            context,
                            "EMERGENCY ALERT SENT to ${body.data?.contact_name}!",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(context, body?.message ?: "SOS failed due to server logic.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    handleErrorResponse(response.code())
                }
            } catch (e: HttpException) {
                handleHttpError(e)
            } catch (e: IOException) {
                Toast.makeText(context, "Network error. Check your connection.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "An unknown error occurred.", Toast.LENGTH_LONG).show()
            } finally {
                sosButton.isEnabled = true
            }
        }
    }

    private fun handleErrorResponse(code: Int) {
        val message = when (code) {
            400 -> "Validation failed or no emergency contact is set up. Check your profile."
            403 -> "Phone number not verified. Please verify your phone number."
            404 -> "User profile not found. Please create your profile."
            500 -> "Server error. Please try again later."
            else -> "Failed to send SOS (Code: $code)."
        }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun handleHttpError(e: HttpException) {
        Toast.makeText(context, "API Error: ${e.code()}. Check logs for details.", Toast.LENGTH_LONG).show()
        Log.e("SOS_FRAGMENT", "HttpException: ${e.code()}", e)
    }
}