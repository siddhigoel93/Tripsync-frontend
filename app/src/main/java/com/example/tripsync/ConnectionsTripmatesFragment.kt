package com.example.tripsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ConnectionsTripmatesFragment : Fragment() {

    private lateinit var rvConnections: RecyclerView
    private lateinit var rvTripmates: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_connections_tripmates, container, false)
        rvConnections = root.findViewById(R.id.rv_connections)
        rvTripmates = root.findViewById(R.id.rv_tripmates)
        setupRecyclerViews()
        return root
    }

    private fun setupRecyclerViews() {
        rvConnections.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rvConnections.isNestedScrollingEnabled = false

        rvTripmates.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rvTripmates.isNestedScrollingEnabled = false

        val defaultConnection = ConnectionItem(
            id = "1",
            name = "Alex Johnson",
            meta = "NYC · 2 trips",
            avatarDrawable = R.drawable.mumbai
        )
        val connections = listOf(defaultConnection, defaultConnection, defaultConnection)
        rvConnections.adapter = ConnectionAdapter(connections)

        val defaultTripmate = TripmateItem(
            id = "1",
            name = "Sara Lee",
            meta = "More Details",
            imageDrawable = R.drawable.ic_trip_placeholder
        )
        val tripmates = List(10) { defaultTripmate }
        rvTripmates.adapter = TripmateAdapter(tripmates)
    }
}
