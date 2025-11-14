package com.example.tripsync

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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

        val backBtn = root.findViewById<ImageButton>(R.id.btn_back)
        backBtn?.setOnClickListener {
            try {
                findNavController().popBackStack()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Unable to go back", Toast.LENGTH_SHORT).show()
            }
        }

        val etSearch = root.findViewById<EditText>(R.id.et_search)
        etSearch?.let { edit ->
            val filters = arrayOf<InputFilter>(EmojiAndBlankFilter())
            edit.filters = filters

            // Extra: ensure user cannot leave only whitespace — clear if trimmed empty
            edit.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    // if current text is only whitespace, clear it to avoid a "only-spaces" state
                    val str = s?.toString() ?: ""
                    if (str.isNotEmpty() && str.trim().isEmpty()) {
                        // remove whitespace-only content
                        edit.setText("")
                    }
                }
            })
        }

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
            avatarDrawable = R.drawable.ic_avatar_placeholder
        )
        val connections = listOf(defaultConnection, defaultConnection, defaultConnection, defaultConnection)
        rvConnections.adapter = ConnectionAdapter(connections)

        val defaultTripmate = TripmateItem(
            id = "1",
            name = "Sara Lee",
            meta = "More Details",
            imageDrawable = R.drawable.ic_avatar_placeholder
        )
        val tripmates = List(10) { defaultTripmate }
        rvTripmates.adapter = TripmateAdapter(tripmates)
    }


    private class EmojiAndBlankFilter : InputFilter {
        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: android.text.Spanned,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            val out = StringBuilder()
            var i = start
            while (i < end) {
                val ch = source[i]
                val codePoint = ch.code
                if (isEmojiCodepoint(codePoint)) {
                    i++
                    continue
                }
                out.append(ch)
                i++
            }

            val filteredSource = out.toString()
            if (filteredSource.isEmpty()) {
                return ""
            }

            val newText = StringBuilder()
                .append(dest.subSequence(0, dstart))
                .append(filteredSource)
                .append(dest.subSequence(dend, dest.length))
                .toString()

            // Prevent the resulting text being only whitespace (or leading space when dest is empty)
            if (newText.trim().isEmpty()) {
                return ""
            }

            if (dest.isEmpty() && filteredSource.startsWith(" ")) {
                return filteredSource.trimStart()
            }

            return filteredSource
        }

        companion object {
            private fun isEmojiCodepoint(cp: Int): Boolean {
                return (cp in 0x1F600..0x1F64F) ||
                        (cp in 0x1F300..0x1F5FF) ||
                        (cp in 0x1F680..0x1F6FF) ||
                        (cp in 0x2600..0x26FF) ||
                        (cp in 0x2700..0x27BF) ||
                        (cp in 0xFE00..0xFE0F) ||
                        (cp in 0x1F900..0x1F9FF) ||
                        (cp in 0x1F1E6..0x1F1FF)
            }
        }
    }
}
