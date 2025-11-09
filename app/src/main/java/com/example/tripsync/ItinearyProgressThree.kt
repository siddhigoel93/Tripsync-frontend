package com.example.tripsync

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.tripsync.api.ApiClient
import com.example.tripsync.itinerary.ActivityItem
import com.example.tripsync.itinerary.CreateTripRequest
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

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

    private var dynamicTabsScrollView: HorizontalScrollView? = null
    private var dynamicTabsContainer: LinearLayout? = null
    private var dynamicTabViews: MutableList<TextView> = mutableListOf()

    private var days: List<LocalDay> = emptyList()
    private var selectedDayIndex = 0

    private var loadingOverlay: View? = null

    private var argTripName: String = ""
    private var argStartDate: String = ""
    private var argEndDate: String = ""
    private var argPreference: String = ""
    private var argBudget: String = ""
    private var argCurrentLoc: String = ""
    private var argDestination: String = ""

    private val createDocLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri == null) {
            Toast.makeText(requireContext(), "Download cancelled", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        try {
            requireContext().contentResolver.openOutputStream(uri)?.use { out ->
                OutputStreamWriter(out, Charsets.UTF_8).use { writer ->
                    writer.write(buildItineraryText())
                    writer.flush()
                }
            }
            Toast.makeText(requireContext(), "Itinerary saved", Toast.LENGTH_SHORT).show()
        } catch (t: Throwable) {
            Toast.makeText(requireContext(), "Could not save file", Toast.LENGTH_SHORT).show()
        }
    }

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

        tab1.visibility = View.GONE
        tab2.visibility = View.GONE
        tab3.visibility = View.GONE
        tab4.visibility = View.GONE

        tripDate.setTextColor(0xFF000000.toInt())

        val tripCard = view.findViewById<LinearLayout>(R.id.tripCard)
        tripCard?.setPadding(dpToPx(requireContext(), 16), dpToPx(requireContext(), 12), dpToPx(requireContext(), 16), dpToPx(requireContext(), 12))

        argTripName = arguments?.getString("tripName").orEmpty()
        argStartDate = arguments?.getString("startDate").orEmpty()
        argEndDate = arguments?.getString("endDate").orEmpty()
        argPreference = arguments?.getString("preference").orEmpty()
        argBudget = arguments?.getString("totalBudget").orEmpty()
        argCurrentLoc = arguments?.getString("currentLocation").orEmpty()
        argDestination = arguments?.getString("destination").orEmpty()

        if (argTripName.isNotBlank()) tripTitle.text = argTripName

        if (argStartDate.isNotBlank() && argEndDate.isNotBlank()) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val start = runCatching { sdf.parse(argStartDate) }.getOrNull()
            val end = runCatching { sdf.parse(argEndDate) }.getOrNull()
            if (start != null && end != null) {
                val diffDays = (abs(end.time - start.time) / (1000L * 60 * 60 * 24)).toInt() + 1
                val pref = if (argPreference.isBlank()) "Adventure" else argPreference.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
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

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        view.findViewById<View>(R.id.btnCreateTrip).setOnClickListener {
            CongratsDialogFragment().show(parentFragmentManager, "congrats")
        }

        view.findViewById<View>(R.id.btnDownload).setOnClickListener {
            val fileName = (if (argTripName.isNotBlank()) argTripName else "Trip Itinerary")
                .replace(Regex("""[\\/:*?"<>|]"""), "_")
                .plus(".txt")
            createDocLauncher.launch(fileName)
        }

        tab1.setOnClickListener { selectDay(0) }
        tab2.setOnClickListener { selectDay(1) }
        tab3.setOnClickListener { selectDay(2) }
        tab4.setOnClickListener { selectDay(3) }

        insertDynamicTabsHolderIfMissing(view)
        ensureLoadingOverlay(view as ViewGroup)
        showLoading(true)

        fetchItineraryFromApi(
            argTripName,
            argStartDate,
            argEndDate,
            argPreference,
            argBudget,
            argCurrentLoc,
            argDestination
        )
    }

    private fun ensureLoadingOverlay(root: ViewGroup) {
        if (loadingOverlay != null) return
        val overlay = FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundColor(0x66FFFFFF)
            isClickable = true
            isFocusable = true
            isFocusableInTouchMode = true
            val spinner = ProgressBar(requireContext())
            addView(spinner, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER))
            visibility = View.GONE
        }
        root.addView(overlay)
        loadingOverlay = overlay
    }

    private fun showLoading(show: Boolean) {
        loadingOverlay?.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun insertDynamicTabsHolderIfMissing(rootView: View) {
        if (dynamicTabsScrollView != null && dynamicTabsContainer != null) return
        val parent = rootView as? ViewGroup ?: return
        val hsv = HorizontalScrollView(requireContext()).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(dpToPx(requireContext(), 12))
        }
        val ll = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            gravity = Gravity.CENTER_VERTICAL
        }
        hsv.addView(ll)
        val rv = parent.findViewById<View>(R.id.rvItinerary)
        var inserted = false
        if (rv != null && rv.parent is ViewGroup) {
            val rvParent = rv.parent as ViewGroup
            val index = rvParent.indexOfChild(rv)
            if (index >= 0) {
                rvParent.addView(hsv, index)
                inserted = true
            }
        }
        if (!inserted) parent.addView(hsv)
        dynamicTabsScrollView = hsv
        dynamicTabsContainer = ll
    }

    private fun toIsoDate(ddMMyyyy: String): String {
        val inFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val outFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return runCatching { outFmt.format(inFmt.parse(ddMMyyyy)!!) }.getOrNull() ?: ddMMyyyy
    }

    private fun fetchItineraryFromApi(
        tripName: String,
        startDate: String,
        endDate: String,
        preference: String,
        budgetStr: String?,
        currentLocArg: String,
        destinationArg: String
    ) {
        val TAG = "ItineraryAPI"
        if (tripName.isBlank() || startDate.isBlank() || endDate.isBlank()) {
            days = listOf(buildEmptyDay(1), buildEmptyDay(2), buildEmptyDay(3), buildEmptyDay(4))
            renderDynamicDayTabs(days.size)
            selectDay(0)
            showLoading(false)
            return
        }
        lifecycleScope.launch {
            try {
                val service = ApiClient.getItineraryService(requireContext())
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val start = runCatching { sdf.parse(startDate) }.getOrNull()
                val end = runCatching { sdf.parse(endDate) }.getOrNull()
                val daysCount = if (start != null && end != null)
                    ((abs(end.time - start.time) / (1000L * 60 * 60 * 24)).toInt() + 1).coerceAtLeast(1)
                else 1

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
                } catch (_: Throwable) { }

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
                        trip_preferences = if (preference.isBlank()) "general" else preference.lowercase(Locale.getDefault()),
                        budget = budgetStr?.toDoubleOrNull() ?: 0.0
                    )
                    val createResp = try { service.createTrip(createBody) } catch (_: Throwable) { null }
                    if (createResp == null || !createResp.isSuccessful) {
                        days = listOf(buildEmptyDay(1), buildEmptyDay(2), buildEmptyDay(3), buildEmptyDay(4))
                        renderDynamicDayTabs(days.size)
                        selectDay(0)
                        showLoading(false)
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
                        } catch (_: Throwable) { }
                    }
                    if (id == null || id == 0) {
                        days = listOf(buildEmptyDay(1), buildEmptyDay(2), buildEmptyDay(3), buildEmptyDay(4))
                        renderDynamicDayTabs(days.size)
                        selectDay(0)
                        showLoading(false)
                        return@launch
                    }
                    id
                }

                val fetchedDays = mutableListOf<LocalDay>()
                val targetDays = max(1, if (start != null && end != null)
                    ((abs(end.time - start.time) / (1000L * 60 * 60 * 24)).toInt() + 1) else 1
                )

                for (d in 1..targetDays) {
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
                            fetchedDays.add(LocalDay(d, "Day $d", emptyList()))
                        }
                    } catch (_: Throwable) {
                        fetchedDays.add(LocalDay(d, "Day $d", emptyList()))
                    }
                }

                days = fetchedDays
                renderDynamicDayTabs(days.size)
                selectDay(0)
            } catch (_: Throwable) {
                days = listOf(buildEmptyDay(1), buildEmptyDay(2), buildEmptyDay(3), buildEmptyDay(4))
                renderDynamicDayTabs(days.size)
                selectDay(0)
            } finally {
                showLoading(false)
            }
        }
    }

    private fun renderDynamicDayTabs(count: Int) {
        val container = dynamicTabsContainer ?: return
        dynamicTabViews.clear()
        container.removeAllViews()
        if (count <= 0) return
        for (i in 1..count) {
            val tv = TextView(requireContext()).apply {
                text = "Day $i"
                textSize = 14f
                setTypeface(typeface, Typeface.BOLD)
                setPadding(dpToPx(requireContext(), 10))
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    rightMargin = dpToPx(requireContext(), 8)
                }
                try { setBackgroundResource(R.drawable.day_tab_inactive) } catch (_: Exception) {
                    val bg = GradientDrawable().apply {
                        cornerRadius = dpToPx(requireContext(), 8).toFloat()
                        setColor(0xFFEFEFEF.toInt())
                    }
                    background = bg
                }
                setTextColor(0xFF000000.toInt())
                tag = i - 1
                setOnClickListener { selectDay((it.tag as? Int) ?: 0) }
            }
            dynamicTabViews.add(tv)
            container.addView(tv)
        }
        if (selectedDayIndex < 0 || selectedDayIndex >= dynamicTabViews.size) selectedDayIndex = 0
        updateDynamicTabSelection()
    }

    private fun updateDynamicTabSelection() {
        for ((idx, tv) in dynamicTabViews.withIndex()) {
            if (idx == selectedDayIndex) {
                try {
                    tv.setBackgroundResource(R.drawable.day_tab_active)
                    tv.setTextColor(0xFFFFFFFF.toInt())
                } catch (_: Exception) {
                    val bg = GradientDrawable().apply {
                        cornerRadius = dpToPx(requireContext(), 8).toFloat()
                        setColor(0xFF007F6A.toInt())
                    }
                    tv.background = bg
                    tv.setTextColor(0xFFFFFFFF.toInt())
                }
                dynamicTabsScrollView?.post {
                    val left = tv.left - dpToPx(requireContext(), 12)
                    dynamicTabsScrollView?.smoothScrollTo(left, 0)
                }
            } else {
                try {
                    tv.setBackgroundResource(R.drawable.day_tab_inactive)
                    tv.setTextColor(0xFF000000.toInt())
                } catch (_: Exception) {
                    val bg = GradientDrawable().apply {
                        cornerRadius = dpToPx(requireContext(), 8).toFloat()
                        setColor(0xFFEFEFEF.toInt())
                    }
                    tv.background = bg
                    tv.setTextColor(0xFF000000.toInt())
                }
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
        if (index < 0) return
        if (index >= days.size) return
        selectedDayIndex = index
        val d = days[index]
        dayTitle.text = d.title
        updateDynamicTabSelection()
        setTabActive(tab1, index == 0)
        setTabActive(tab2, index == 1)
        setTabActive(tab3, index == 2)
        setTabActive(tab4, index == 3)
        rvContainer.removeAllViews()
        for (s in d.sections) {
            val outerRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            val leftCol = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(dpToPx(requireContext(), 44), ViewGroup.LayoutParams.MATCH_PARENT)
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            }
            val iconView = ImageView(requireContext()).apply {
                val size = dpToPx(requireContext(), 28)
                layoutParams = LinearLayout.LayoutParams(size, size).apply { topMargin = dpToPx(requireContext(), 6) }
                val res = resIdOrZero(s.iconName)
                if (res != 0) setImageResource(res) else setImageResource(0)
            }
            val lineView = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(requireContext(), 2), ViewGroup.LayoutParams.MATCH_PARENT).apply {
                    topMargin = dpToPx(requireContext(), 6)
                    gravity = Gravity.CENTER_HORIZONTAL
                }
                val dash = GradientDrawable().apply {
                    setSize(dpToPx(requireContext(), 2), dpToPx(requireContext(), 1))
                    setColor(0x00000000)
                    setStroke(dpToPx(requireContext(), 2), 0xFFE0E0E0.toInt(), dpToPx(requireContext(), 4).toFloat(), dpToPx(requireContext(), 4).toFloat())
                }
                background = dash
            }
            leftCol.addView(iconView)
            leftCol.addView(lineView)
            val rightCol = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            val label = TextView(requireContext()).apply {
                text = s.label
                textSize = 14f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(0xFF6D6D6D.toInt())
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    bottomMargin = dpToPx(requireContext(), 6)
                }
            }
            val sectionBox = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = dpToPx(requireContext(), 0)
                    bottomMargin = dpToPx(requireContext(), 12)
                }
                val bg = GradientDrawable().apply {
                    cornerRadius = dpToPx(requireContext(), 8).toFloat()
                    setColor(0xFFFFFFFF.toInt())
                }
                background = bg
                setPadding(dpToPx(requireContext(), 14), dpToPx(requireContext(), 12), dpToPx(requireContext(), 14), dpToPx(requireContext(), 12))
            }
            rightCol.addView(label)
            rightCol.addView(sectionBox)
            var first = true
            for (c in s.cards) {
                val card = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        if (!first) topMargin = dpToPx(requireContext(), 10)
                    }
                }
                val bgc = GradientDrawable().apply {
                    cornerRadius = dpToPx(requireContext(), 10).toFloat()
                    setColor(0xFFDAF5F8.toInt())
                }
                card.background = bgc
                card.setPadding(dpToPx(requireContext(), 12), dpToPx(requireContext(), 10), dpToPx(requireContext(), 12), dpToPx(requireContext(), 10))
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
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                }
                card.addView(t)
                card.addView(sub)
                sectionBox.addView(card)
                first = false
            }
            outerRow.addView(leftCol)
            outerRow.addView(rightCol)
            rvContainer.addView(outerRow)
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

    private fun dpToPx(ctx: android.content.Context, dp: Int) =
        (dp * ctx.resources.displayMetrics.density).toInt()


    private fun buildItineraryText(): String {
        val sb = StringBuilder()
        val nf = NumberFormat.getInstance(Locale("en", "IN"))
        val budgetLong = argBudget.toLongOrNull()

        sb.appendLine("==== AI Itinerary ====")
        sb.appendLine("Trip: ${if (argTripName.isBlank()) "Untitled Trip" else argTripName}")
        sb.appendLine("From: $argCurrentLoc")
        sb.appendLine("To: $argDestination")
        sb.appendLine("Dates: ${argStartDate} to ${argEndDate}")
        sb.appendLine("Preference: ${if (argPreference.isBlank()) "Adventure" else argPreference}")
        sb.appendLine("Budget: ${budgetLong?.let { "₹" + nf.format(it) } ?: (if (argBudget.isBlank()) "—" else argBudget)}")
        sb.appendLine()

        if (days.isEmpty()) {
            sb.appendLine("No itinerary available.")
            return sb.toString()
        }

        for (day in days) {
            sb.appendLine("Day ${day.dayNumber}: ${day.title}")
            if (day.sections.isEmpty()) {
                sb.appendLine("  (No activities)")
            } else {
                for (section in day.sections) {
                    val label = section.label.ifBlank { "Activities" }
                    sb.appendLine("  $label")
                    if (section.cards.isEmpty()) {
                        sb.appendLine("    - (No items)")
                    } else {
                        for (card in section.cards) {
                            val title = card.title.ifBlank { "Untitled activity" }
                            val sub = card.subtitle.ifBlank { "" }
                            if (sub.isBlank()) {
                                sb.appendLine("    • $title")
                            } else {
                                sb.appendLine("    • $title")
                                sb.appendLine("      $sub")
                            }
                        }
                    }
                }
            }
            sb.appendLine()
        }
        sb.appendLine("Generated by TripSync")
        return sb.toString()
    }
}
