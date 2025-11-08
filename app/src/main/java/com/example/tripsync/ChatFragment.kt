package com.example.tripsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_chats, container, false)

        val recycler = root.findViewById<RecyclerView>(R.id.recycler_trips)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        val sample = sampleTrips()

        val adapter = TripsAdapter(
            sample,
            { trip ->
                Toast.makeText(requireContext(), "Clicked: ${trip.title}", Toast.LENGTH_SHORT).show()
            },
            { name, avatarRes ->
                // Temporarily disable navigation — show info only
                Toast.makeText(
                    requireContext(),
                    "Chat with $name (avatar id: $avatarRes)",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        recycler.adapter = adapter
        return root
    }

    private fun sampleTrips(): List<Trip> {
        val banner = R.drawable.img_delhi_banner

        return listOf(
            Trip(
                title = "Delhi Trip 2025",
                bannerRes = banner,
                avatarRes = listOf(R.drawable.avatar_1, R.drawable.avatar_2, R.drawable.avatar_3)
            ),
            Trip(
                title = "Mumbai Weekend",
                bannerRes = banner,
                avatarRes = listOf(R.drawable.avatar_1)
            ),
            Trip(
                title = "Goa Getaway",
                bannerRes = banner,
                avatarRes = listOf(R.drawable.avatar_2, R.drawable.avatar_3, R.drawable.avatar_1)
            ),
            Trip(
                title = "Himachal Trek",
                bannerRes = banner,
                avatarRes = listOf(R.drawable.avatar_3, R.drawable.avatar_1)
            )
        )
    }
}
