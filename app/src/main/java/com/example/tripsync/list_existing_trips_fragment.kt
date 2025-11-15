package com.example.tripsync

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.SessionManager
import com.example.tripsync.network.ListApiService
import com.example.tripsync.network.models.TripDetail
import com.example.tripsync.network.models.TripItem
import com.example.tripsync.network.models.ApiResponse
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.File

class ListExistingTripsFragment : Fragment() {

    private lateinit var rv: RecyclerView
    private lateinit var heading: TextView
    private lateinit var adapter: ListTripsAdapter
    private lateinit var listApi: ListApiService

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.list_existing_trips, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        heading = view.findViewById(R.id.tv_heading_existing_trips)
        rv = view.findViewById(R.id.rv_trips)
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = ListTripsAdapter(emptyList()) { item -> onTripClicked(item) }
        rv.adapter = adapter
        heading.text = "Existing Trips"

        Log.i("TOKEN_CHECK", "SessionManager token=${SessionManager.getAuthToken(requireContext())}")

        val retrofit = try {
            ApiClient.getRetrofitInstance(requireContext(), secure = true)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "API client init failed: ${e.message}", Toast.LENGTH_LONG).show()
            return
        }

        listApi = retrofit.create(ListApiService::class.java)
        loadTrips()
    }

    private fun loadTrips() {
        lifecycleScope.launch {
            val items = withContext(Dispatchers.IO) {
                try {
                    val resp = listApi.getTrips()
                    if (resp.success && resp.data != null) resp.data else emptyList()
                } catch (e: HttpException) {
                    emptyList<TripItem>()
                } catch (e: Exception) {
                    emptyList<TripItem>()
                }
            }
            if (items.isNotEmpty()) adapter.updateItems(items) else Toast.makeText(requireContext(), "No trips found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onTripClicked(item: TripItem) {
        lifecycleScope.launch {
            val detail: TripDetail? = withContext(Dispatchers.IO) {
                try {
                    listApi.getTrip(item.id)
                } catch (e: Exception) {
                    null
                }
            }
            if (detail != null) {
                val gson = GsonBuilder().setPrettyPrinting().create()
                val pretty = gson.toJson(detail)
                val fileName = "trip_${item.id}_${sanitizeFileName(item.tripname)}.txt"
                val saved = saveTextToFile(requireContext(), fileName, pretty)
                if (saved) Toast.makeText(requireContext(), "Saved $fileName", Toast.LENGTH_SHORT).show()
                else Toast.makeText(requireContext(), "Failed to save file", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to download details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sanitizeFileName(name: String?): String {
        if (name.isNullOrBlank()) return "unknown"
        return name.replace(Regex("[^A-Za-z0-9_.-]"), "_")
    }

    private fun saveTextToFile(context: Context, fileName: String, content: String): Boolean {
        return try {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            file.writeText(content)
            true
        } catch (e: Exception) {
            false
        }
    }
}
