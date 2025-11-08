package com.example.tripsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class ChatThreadFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_chat_thread, container, false)

        val back = v.findViewById<ImageView>(R.id.toolbar_back)
        val profileImg = v.findViewById<ImageView>(R.id.toolbar_profile)
        val nameTv = v.findViewById<TextView>(R.id.toolbar_name)

        val args = requireArguments()
        val name = args.getString("name", "")
        val avatarRes = args.getInt("avatarRes", -1)

        nameTv.text = name

        if (avatarRes != -1) {
            profileImg.visibility = View.VISIBLE
            profileImg.setImageResource(avatarRes)
        } else {
            profileImg.visibility = View.GONE
        }

        back.setOnClickListener {
            findNavController().navigateUp()
        }

        return v
    }
}
