package com.example.tripsync.travelStories

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.models.TrendingPlace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.UnknownHostException

class TrendingDestinationFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val destinations = mutableListOf<TrendingPlace>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trending_destination, container, false)
        recyclerView = view.findViewById(R.id.destinations_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = TrendingDestinationAdapter(destinations) { destination ->
            if (isAdded) {
                Toast.makeText(requireContext(), "Clicked: ${destination.name}", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchTrendingPlaces()
    }

    private fun fetchTrendingPlaces() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = ApiClient.getAuthService(requireContext())
                val places = api.getTrendingPlaces()

                Log.d("TrendingDestinationFragment", "Places fetched: ${places.size}")

                withContext(Dispatchers.Main) {
                    if (!isAdded) return@withContext  // fragment not attached anymore
                    destinations.clear()
                    destinations.addAll(places)
                    recyclerView.adapter?.notifyDataSetChanged()
                }

            } catch (e: HttpException) {
                Log.e("TrendingDestinationFragment", "HTTP error", e)
                withContext(Dispatchers.Main) {
                    if (isAdded) {
                        val message = when (e.code()) {
                            502 -> "Server temporarily unavailable. Try again later."
                            404 -> "Data not found."
                            else -> "Server error (${e.code()})"
                        }
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: UnknownHostException) {
                Log.e("TrendingDestinationFragment", "No internet", e)
                withContext(Dispatchers.Main) {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                Log.e("TrendingDestinationFragment", "Error fetching trending places", e)
                withContext(Dispatchers.Main) {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}