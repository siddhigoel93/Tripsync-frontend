package com.example.tripsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.models.recieved_FriendRequestItem
import com.example.tripsync.api.recieved_TripmateReceivedService
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class recieved_RequestsFragment : Fragment() {

    private lateinit var rv: RecyclerView
    private lateinit var progress: ProgressBar
    private lateinit var toolbar: MaterialToolbar
    private lateinit var titleView: TextView
    private val adapter by lazy { recieved_FriendRequestAdapter(mutableListOf()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.recieved_fragment_received_requests, container, false)
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        rv = v.findViewById(R.id.recieved_rv_requests)
        progress = v.findViewById(R.id.recieved_progress)
        toolbar = v.findViewById(R.id.recieved_toolbar)
        titleView = v.findViewById(R.id.recieved_toolbar_title)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter
        toolbar.setNavigationIcon(R.drawable.greenback)
        toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        load()
    }

    private fun load() {
        progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val service = ApiClient.createService(requireContext(), recieved_TripmateReceivedService::class.java)
                val resp = withContext(Dispatchers.IO) { service.getReceivedRequests() }
                if (resp.isSuccessful) adapter.update(resp.body() ?: emptyList())
                else Toast.makeText(requireContext(), "Failed: ${resp.code()}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                progress.visibility = View.GONE
            }
        }
    }
}
