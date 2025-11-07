package com.example.tripsync

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Space
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.tripsync.api.ApiClient
import com.example.tripsync.itinerary.ActivityItem
import com.example.tripsync.itinerary.CreateTripRequest
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class ItinearyProgressThree : Fragment(R.layout.fragment_ai_itinerary_step3) {

    private data class LocalCard(val iconRes: Int = 0, val title: String, val subtitle: String)
    private data class LocalSection(val label: String, val iconName: String? = null, val cards: List<LocalCard>)
    private data class LocalDay(val dayNumber: Int, val title: String, val sections: List<LocalSection>)

    private lateinit var tripTitle: TextView
    private lateinit var tripMeta: TextView
    private lateinit var tripDate: TextView
    private lateinit var dayTitle: TextView
    private lateinit var rvContainer: LinearLayout
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
        rvContainer = view.findViewById(R.id.rvItinerary)
        tab1 = view.findViewById(R.id.tabDay1)
        tab2 = view.findViewById(R.id.tabDay2)
        tab3 = view.findViewById(R.id.tabDay3)
        tab4 = view.findViewById(R.id.tabDay4)

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
                        days = listOf(buildEmptyDay(1), buildEmptyDay(2), buildEmptyDay(3), buildEmptyDay(4))
                        selectDay(0)
                        return@launch
                    }
                    if (!createResp.isSuccessful) {
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
                            }
                        } catch (t: Throwable) {
                            Log.e(TAG, "Failed to parse createResp raw body", t)
                        }
                    }
                    if (id == null || id == 0) {
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
                        if (dayResp.isSuccessful) {
                            val b = dayResp.body()
                            val title = b?.data?.title ?: "Day $d"
                            val sections = mutableListOf<LocalSection>()
                            val dayData = b?.data
                            if (!dayData?.sections.isNullOrEmpty()) {
                                val sec = dayData!!.sections!!.map { s ->
                                    LocalSection(
                                        label = s.label ?: "",
                                        iconName = s.icon,
                                        cards = s.cards.orEmpty().map { c ->
                                            LocalCard(iconRes = 0, title = c.title ?: "", subtitle = c.subtitle ?: "")
                                        }
                                    )
                                }
                                sections.addAll(sec)
                            } else if (!dayData?.activities.isNullOrEmpty()) {
                                val actList = dayData!!.activities!!.orEmpty()
                                val grouped = groupActivitiesByTime(actList)
                                val order = listOf("Morning", "Afternoon", "Evening", "Night")
                                for (key in order) {
                                    val list = grouped[key].orEmpty()
                                    if (list.isNotEmpty()) {
                                        val cards = list.map { a -> LocalCard(iconRes = 0, title = a.title ?: "", subtitle = a.description ?: "") }
                                        val iconName = when (key) {
                                            "Morning" -> "morning"
                                            "Afternoon" -> "afternoon"
                                            "Evening" -> "afternoon"
                                            "Night" -> "night"
                                            else -> null
                                        }
                                        sections.add(LocalSection(label = key, iconName = iconName, cards = cards))
                                    }
                                }
                            }
                            fetchedDays.add(LocalDay(d, title, sections))
                        } else {
                            fetchedDays.add(buildEmptyDay(d))
                        }
                    } catch (t: Throwable) {
                        fetchedDays.add(buildEmptyDay(d))
                    }
                }

                days = fetchedDays
                selectDay(0)
            } catch (t: Throwable) {
                days = listOf(buildEmptyDay(1), buildEmptyDay(2), buildEmptyDay(3), buildEmptyDay(4))
                selectDay(0)
            }
        }
    }

    private fun groupActivitiesByTime(list: List<ActivityItem>): Map<String, List<ActivityItem>> {
        val map = mutableMapOf<String, MutableList<ActivityItem>>()
        map["Morning"] = mutableListOf()
        map["Afternoon"] = mutableListOf()
        map["Evening"] = mutableListOf()
        map["Night"] = mutableListOf()
        for (a in list) {
            val t = (a.time ?: "").trim().lowercase(Locale.getDefault())
            when {
                t.contains("mor") || t.contains("breakfast") -> map["Morning"]!!.add(a)
                t.contains("aft") || t.contains("lunch") -> map["Afternoon"]!!.add(a)
                t.contains("eve") -> map["Evening"]!!.add(a)
                t.contains("night") || t.contains("dinner") -> map["Night"]!!.add(a)
                else -> map["Morning"]!!.add(a)
            }
        }
        return map
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

        rvContainer.removeAllViews()

        for (s in d.sections) {
            val headerRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setPadding(dpToPx(requireContext(), 6))
                gravity = Gravity.CENTER_VERTICAL
            }

            val iconView = ImageView(requireContext()).apply {
                val size = dpToPx(requireContext(), 28)
                layoutParams = LinearLayout.LayoutParams(size, size).apply { marginEnd = dpToPx(requireContext(), 10) }
                val res = resIdOrZero(s.iconName)
                if (res != 0) setImageResource(res) else setImageDrawable(null)
            }
            val label = TextView(requireContext()).apply {
                text = s.label
                textSize = 14f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(0xFF6D6D6D.toInt())
            }
            headerRow.addView(iconView)
            headerRow.addView(label)
            rvContainer.addView(headerRow)

            val sectionBox = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = dpToPx(requireContext(), 6)
                    bottomMargin = dpToPx(requireContext(), 12)
                }
                val bg = GradientDrawable().apply {
                    cornerRadius = dpToPx(requireContext(), 8).toFloat()
                    setColor(0xFFFFFFFF.toInt())
                }
                background = bg
                setPadding(dpToPx(requireContext(), 12), dpToPx(requireContext(), 12), dpToPx(requireContext(), 12), dpToPx(requireContext(), 12))
            }

            var first = true
            for (c in s.cards) {
                val card = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        if (!first) topMargin = dpToPx(requireContext(), 14)
                    }
                    val bgc = GradientDrawable().apply {
                        cornerRadius = dpToPx(requireContext(), 10).toFloat()

                        setColor(0xFFDAF5F8.toInt())
                    }
                    background = bgc
                    setPadding(dpToPx(requireContext(), 18), dpToPx(requireContext(), 14), dpToPx(requireContext(), 18), dpToPx(requireContext(), 14))
                }

                val t = TextView(requireContext()).apply {
                    text = c.title
                    textSize = 16f
                    setTypeface(typeface, Typeface.BOLD)

                    setTextColor(0xFF007F6A.toInt())
                }
                val sub = TextView(requireContext()).apply {
                    text = c.subtitle
                    textSize = 13f

                    setTextColor(0xFF2E2E2E.toInt())
                    val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    layoutParams = lp
                }
                card.addView(t)
                card.addView(sub)
                sectionBox.addView(card)
                first = false
            }

            rvContainer.addView(sectionBox)

            val spacer = Space(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(requireContext(), 10))
            }
            rvContainer.addView(spacer)
        }

        val bottomSpacer = Space(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(requireContext(), 24))
        }
        rvContainer.addView(bottomSpacer)

        val parentScroll = view?.findViewById<ScrollView>(R.id.pageScroll)
        parentScroll?.post { parentScroll.fullScroll(View.FOCUS_UP) }
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

    private fun dpToPx(ctx: android.content.Context, dp: Int) = (dp * ctx.resources.displayMetrics.density).toInt()
}
