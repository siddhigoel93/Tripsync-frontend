package com.example.tripsync

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.api.ApiClient
import com.example.tripsync.itinerary.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class ItinearyProgressThree : Fragment(R.layout.fragment_ai_itinerary_step3) {

    private data class LocalCard(val iconRes: Int = 0, val title: String, val subtitle: String)
    private data class LocalSection(val label: String, val iconRes: Int = 0, val cards: List<LocalCard>)
    private data class LocalDay(val dayNumber: Int, val title: String, val sections: List<LocalSection>)

    private sealed class Item {
        data class SectionHeader(val label: String, val iconRes: Int) : Item()
        data class CardItem(val iconRes: Int, val title: String, val subtitle: String) : Item()
    }

    private lateinit var tripTitle: TextView
    private lateinit var tripMeta: TextView
    private lateinit var tripDate: TextView
    private lateinit var dayTitle: TextView
    private lateinit var rv: RecyclerView
    private val adapter = ItineraryListAdapter()
    private lateinit var tab1: TextView
    private lateinit var tab2: TextView
    private lateinit var tab3: TextView
    private lateinit var tab4: TextView
    private var days: List<LocalDay> = emptyList()
    private var selectedDayIndex = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tripTitle = view.findViewById(R.id.tripTitle)
        tripMeta = view.findViewById(R.id.tripMeta)
        tripDate = view.findViewById(R.id.tripDate)
        dayTitle = view.findViewById(R.id.dayTitle)
        rv = view.findViewById(R.id.rvItinerary)
        tab1 = view.findViewById(R.id.tabDay1)
        tab2 = view.findViewById(R.id.tabDay2)
        tab3 = view.findViewById(R.id.tabDay3)
        tab4 = view.findViewById(R.id.tabDay4)

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter
        rv.isNestedScrollingEnabled = false

        val tripName = arguments?.getString("tripName").orEmpty()
        val startDate = arguments?.getString("startDate").orEmpty()
        val endDate = arguments?.getString("endDate").orEmpty()
        val preference = arguments?.getString("preference").orEmpty()
        val maybeBudget = arguments?.getString("totalBudget").orEmpty()
        val currentLocArg = arguments?.getString("currentLocation").orEmpty()
        val destinationArg = arguments?.getString("destination").orEmpty()

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
                val sameMonth = SimpleDateFormat("MM", Locale.getDefault()).format(start) == SimpleDateFormat("MM", Locale.getDefault()).format(end)
                val fmtEnd = if (sameMonth) SimpleDateFormat("d", Locale.getDefault()).format(end) else SimpleDateFormat("MMM d", Locale.getDefault()).format(end)
                tripDate.text = "$fmtStart - $fmtEnd"
            }
        }

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        view.findViewById<View>(R.id.btnCreateTrip).setOnClickListener {
            CongratsDialogFragment().show(parentFragmentManager, "congrats")
        }
        view.findViewById<View>(R.id.btnSaveDraft).setOnClickListener { }
        view.findViewById<View>(R.id.btnDownload).setOnClickListener { }

        tab1.setOnClickListener { selectDay(0) }
        tab2.setOnClickListener { selectDay(1) }
        tab3.setOnClickListener { selectDay(2) }
        tab4.setOnClickListener { selectDay(3) }

        fetchItineraryFromApi(tripName, startDate, endDate, preference, maybeBudget, currentLocArg, destinationArg)
    }

    private fun toIsoDate(ddMMyyyy: String): String {
        val inFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val outFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return runCatching {
            val d = inFmt.parse(ddMMyyyy)
            outFmt.format(d)
        }.getOrNull() ?: ddMMyyyy
    }

    private fun fetchItineraryFromApi(tripName: String, startDate: String, endDate: String, preference: String, budgetStr: String?, currentLocArg: String, destinationArg: String) {
        val TAG = "ItineraryAPI"
        if (tripName.isBlank() || startDate.isBlank() || endDate.isBlank()) {
            days = listOf(buildEmptyDay(1), buildEmptyDay(2), buildEmptyDay(3), buildEmptyDay(4))
            selectDay(0)
            return
        }

        lifecycleScope.launch {
            try {
                Toast.makeText(requireContext(), "Fetching itinerary...", Toast.LENGTH_SHORT).show()
                val service = ApiClient.getItineraryService(requireContext())

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val start = runCatching { sdf.parse(startDate) }.getOrNull()
                val end = runCatching { sdf.parse(endDate) }.getOrNull()
                val daysCount = if (start != null && end != null) {
                    ((abs(end.time - start.time) / (1000L * 60 * 60 * 24)).toInt() + 1).coerceAtLeast(1)
                } else 1

                val listResp = try { service.listTrips() } catch (t: Throwable) {
                    Log.e(TAG, "listTrips exception", t)
                    null
                }

                var matchedTripId: Int? = null
                try {
                    val raw = listResp?.body()?.string()
                    if (!raw.isNullOrBlank()) {
                        val jo = JSONObject(raw)
                        val data = jo.opt("data")
                        if (data is JSONArray) {
                            for (i in 0 until data.length()) {
                                val item = data.optJSONObject(i)
                                if (item != null) {
                                    val name = item.optString("tripname")
                                    val id = if (item.has("id")) item.optInt("id") else 0
                                    if (!name.isNullOrBlank() && name.equals(tripName, true)) {
                                        matchedTripId = if (id != 0) id else null
                                        break
                                    }
                                }
                            }
                        } else if (data is JSONObject) {
                            if (data.has("results")) {
                                val results = data.optJSONArray("results")
                                if (results != null) {
                                    for (i in 0 until results.length()) {
                                        val item = results.optJSONObject(i)
                                        if (item != null) {
                                            val name = item.optString("tripname")
                                            val id = if (item.has("id")) item.optInt("id") else 0
                                            if (!name.isNullOrBlank() && name.equals(tripName, true)) {
                                                matchedTripId = if (id != 0) id else null
                                                break
                                            }
                                        }
                                    }
                                }
                            } else {
                                val name = data.optString("tripname")
                                val id = if (data.has("id")) data.optInt("id") else 0
                                if (!name.isNullOrBlank() && name.equals(tripName, true)) matchedTripId = if (id != 0) id else null
                            }
                        }
                    }
                } catch (t: Throwable) {
                    Log.e(TAG, "Exception parsing listTrips body", t)
                }

                val tripId: Int = if (matchedTripId != null) {
                    matchedTripId
                } else {
                    val createBody = CreateTripRequest(
                        tripname = tripName,
                        current_loc = currentLocArg,
                        destination = destinationArg,
                        start_date = toIsoDate(startDate),
                        end_date = toIsoDate(endDate),
                        days = daysCount,
                        trip_type = "General",
                        trip_preferences = if (preference.isBlank()) "general" else preference,
                        budget = budgetStr?.toDoubleOrNull() ?: 0.0
                    )
                    val createResp = try { service.createTrip(createBody) } catch (t: Throwable) {
                        Log.e(TAG, "createTrip exception", t)
                        null
                    }
                    if (createResp == null) {
                        Toast.makeText(requireContext(), "Failed to create trip", Toast.LENGTH_LONG).show()
                        days = listOf(buildEmptyDay(1), buildEmptyDay(2), buildEmptyDay(3), buildEmptyDay(4))
                        selectDay(0)
                        return@launch
                    }
                    if (!createResp.isSuccessful) {
                        val rawErr = try { createResp.errorBody()?.string() } catch (_: Exception) { null }
                        Log.e(TAG, "createTrip failed resp=${createResp.code()} body=$rawErr")
                        Toast.makeText(requireContext(), "Failed to create trip", Toast.LENGTH_LONG).show()
                        days = listOf(buildEmptyDay(1), buildEmptyDay(2), buildEmptyDay(3), buildEmptyDay(4))
                        selectDay(0)
                        return@launch
                    }
                    var id: Int? = createResp.body()?.data?.id
                    if (id == null || id == 0) {
                        try {
                            val raw = createResp.raw().body?.string()
                            if (!raw.isNullOrBlank()) {
                                val jo = JSONObject(raw)
                                if (jo.has("trip_id")) id = jo.optInt("trip_id")
                                if ((id == null || id == 0) && jo.has("data")) {
                                    val d = jo.opt("data")
                                    if (d is JSONObject) id = d.optInt("id")
                                }
                                if ((id == null || id == 0) && jo.has("data") && jo.opt("data") is JSONArray) {
                                    val arr = jo.optJSONArray("data")
                                    if (arr.length() > 0) id = arr.optJSONObject(0)?.optInt("id")
                                }
                                if ((id == null || id == 0) && jo.has("message")) {
                                    val message = jo.optString("message")
                                    val error = jo.optString("error")
                                    Log.e(TAG, "createTrip server message: $message error:$error")
                                    Toast.makeText(requireContext(), "$message ${if (error.isNotBlank()) ": $error" else ""}", Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (t: Throwable) {
                            Log.e(TAG, "Failed to parse createResp raw body", t)
                        }
                    }
                    if (id == null || id == 0) {
                        Log.e(TAG, "createTrip missing id in response")
                        Toast.makeText(requireContext(), "Failed to create trip", Toast.LENGTH_LONG).show()
                        days = listOf(buildEmptyDay(1), buildEmptyDay(2), buildEmptyDay(3), buildEmptyDay(4))
                        selectDay(0)
                        return@launch
                    }
                    id
                }

                val fetchedDays = mutableListOf<LocalDay>()
                for (d in 1..4) {
                    try {
                        val dayResp = service.getDayItinerary(tripId, d)
                        val rawReq = try { dayResp.raw().request } catch (_: Exception) { null }
                        rawReq?.let { Log.d(TAG, "Day $d request url=${it.url}") }
                        if (dayResp.isSuccessful) {
                            val b = dayResp.body()
                            val title = b?.data?.title ?: "Day $d"
                            val sections = mutableListOf<LocalSection>()
                            val dayData = b?.data
                            if (!dayData?.sections.isNullOrEmpty()) {
                                val sec = dayData!!.sections!!.map { s ->
                                    LocalSection(
                                        label = s.label ?: "",
                                        iconRes = resIdOrZero(s.icon),
                                        cards = s.cards.orEmpty().map { c ->
                                            LocalCard(iconRes = resIdOrZero(c.icon), title = c.title ?: "", subtitle = c.subtitle ?: "")
                                        }
                                    )
                                }
                                sections.addAll(sec)
                            } else if (!dayData?.activities.isNullOrEmpty()) {
                                val actList = dayData!!.activities!!.orEmpty()
                                if (actList.isNotEmpty()) {
                                    val cards = actList.map { a ->
                                        LocalCard(iconRes = 0, title = a.title ?: "", subtitle = a.description ?: "")
                                    }
                                    sections.add(LocalSection(label = "Activities", iconRes = 0, cards = cards))
                                }
                            }
                            fetchedDays.add(LocalDay(d, title, sections))
                        } else {
                            val code = dayResp.code()
                            val err = try { dayResp.errorBody()?.string() } catch (_: Exception) { null }
                            Log.e(TAG, "day $d failed code=$code body=$err")
                            fetchedDays.add(buildEmptyDay(d))
                        }
                    } catch (t: Throwable) {
                        Log.e(TAG, "Exception fetching day $d", t)
                        fetchedDays.add(buildEmptyDay(d))
                    }
                }

                days = fetchedDays
                selectDay(0)
            } catch (t: Throwable) {
                Log.e("ItineraryAPI", "Exception fetching itinerary", t)
                Toast.makeText(requireContext(), "Error fetching itinerary: ${t.localizedMessage ?: t.javaClass.simpleName}", Toast.LENGTH_LONG).show()
                days = listOf(buildEmptyDay(1), buildEmptyDay(2), buildEmptyDay(3), buildEmptyDay(4))
                selectDay(0)
            }
        }
    }

    private fun selectDay(index: Int) {
        if (index !in days.indices) return
        selectedDayIndex = index
        val d = days[index]
        dayTitle.text = d.title
        setTabActive(tab1, index == 0)
        setTabActive(tab2, index == 1)
        setTabActive(tab3, index == 2)
        setTabActive(tab4, index == 3)
        val items = mutableListOf<Item>()
        d.sections.forEach { s ->
            items += Item.SectionHeader(s.label, s.iconRes)
            s.cards.forEach { c ->
                items += Item.CardItem(c.iconRes, c.title, c.subtitle)
            }
        }
        adapter.submitList(items)
        rv.scrollToPosition(0)
    }

    private fun setTabActive(tv: TextView, active: Boolean) {
        if (active) {
            try { tv.setBackgroundResource(R.drawable.day_tab_active) } catch (_: Exception) { }
            tv.setTextColor(0xFFFFFFFF.toInt())
        } else {
            try { tv.setBackgroundResource(R.drawable.day_tab_inactive) } catch (_: Exception) { }
            tv.setTextColor(0xFF000000.toInt())
        }
    }

    private fun buildEmptyDay(n: Int) = LocalDay(n, "Day $n", emptyList())

    private fun resIdOrZero(name: String?): Int {
        if (name.isNullOrBlank()) return 0
        return try { resources.getIdentifier(name, "drawable", requireContext().packageName) } catch (_: Exception) { 0 }
    }

    private class ItineraryListAdapter : ListAdapter<Item, RecyclerView.ViewHolder>(Diff()) {
        private companion object {
            const val TYPE_SECTION = 1
            const val TYPE_CARD = 2
        }

        override fun getItemViewType(position: Int): Int {
            return when (getItem(position)) {
                is Item.SectionHeader -> TYPE_SECTION
                is Item.CardItem -> TYPE_CARD
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val ctx = parent.context
            return if (viewType == TYPE_SECTION) {
                val container = LinearLayout(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(dpToPx(ctx, 12))
                    gravity = Gravity.CENTER_VERTICAL
                }
                val iv = ImageView(ctx).apply {
                    layoutParams = LinearLayout.LayoutParams(dpToPx(ctx, 28), dpToPx(ctx, 28)).apply { marginEnd = dpToPx(ctx, 12) }
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }
                val tv = TextView(ctx).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    setTypeface(typeface, Typeface.BOLD)
                    textSize = 14f
                }
                container.addView(iv)
                container.addView(tv)
                SectionVH(container, iv, tv)
            } else {
                val container = LinearLayout(ctx).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    val pad = dpToPx(ctx, 12)
                    setPadding(pad, pad, pad, pad)
                }
                val inner = LinearLayout(ctx).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    gravity = Gravity.CENTER_VERTICAL
                }
                val iv = ImageView(ctx).apply {
                    layoutParams = LinearLayout.LayoutParams(dpToPx(ctx, 40), dpToPx(ctx, 40)).apply { marginEnd = dpToPx(ctx, 12) }
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                }
                val texts = LinearLayout(ctx).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                }
                val title = TextView(ctx).apply {
                    textSize = 14f
                    setTypeface(typeface, Typeface.BOLD)
                }
                val subtitle = TextView(ctx).apply { textSize = 12f }
                texts.addView(title)
                texts.addView(subtitle)
                inner.addView(iv)
                inner.addView(texts)
                val bg = GradientDrawable().apply {
                    cornerRadius = dpToPx(ctx, 8).toFloat()
                    setColor(0xFFF6F6F6.toInt())
                }
                container.background = bg
                container.setPadding(dpToPx(ctx, 12), dpToPx(ctx, 12), dpToPx(ctx, 12), dpToPx(ctx, 12))
                container.addView(inner)
                CardVH(container, iv, title, subtitle)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = getItem(position)
            when (holder) {
                is SectionVH -> {
                    val s = item as Item.SectionHeader
                    holder.label.text = s.label
                    if (s.iconRes != 0) holder.icon.setImageResource(s.iconRes) else holder.icon.setImageDrawable(null)
                }
                is CardVH -> {
                    val c = item as Item.CardItem
                    holder.title.text = c.title
                    holder.subtitle.text = c.subtitle
                    if (c.iconRes != 0) holder.icon.setImageResource(c.iconRes) else holder.icon.setImageDrawable(null)
                }
            }
        }

        private class SectionVH(itemView: View, val icon: ImageView, val label: TextView) : RecyclerView.ViewHolder(itemView)
        private class CardVH(itemView: View, val icon: ImageView, val title: TextView, val subtitle: TextView) : RecyclerView.ViewHolder(itemView)

        private class Diff : DiffUtil.ItemCallback<Item>() {
            override fun areItemsTheSame(oldItem: Item, newItem: Item) = oldItem == newItem
            override fun areContentsTheSame(oldItem: Item, newItem: Item) = oldItem == newItem
        }

        private fun dpToPx(ctx: android.content.Context, dp: Int) = (dp * ctx.resources.displayMetrics.density).toInt()
    }
}
