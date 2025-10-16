package com.example.tripsync.onboarding

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.tripsync.R
import com.google.android.material.button.MaterialButton
import androidx.navigation.findNavController

class OnboardingAdapter(
    private val activity: androidx.fragment.app.FragmentActivity,
    private val slides: List<OnboardingData>
) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        OnboardingViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.onboarding_slide_fragment, parent, false))

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) =
        holder.bind(slides[position], position)

    override fun getItemCount() = slides.size

    inner class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.title)
        private val heading: TextView = itemView.findViewById(R.id.tvHeading)
        private val description: TextView = itemView.findViewById(R.id.text)
        private val image: ImageView = itemView.findViewById(R.id.img)
        private val btnNext: MaterialButton = itemView.findViewById(R.id.btn)
        private val skip: TextView = itemView.findViewById(R.id.skip)

        private val progressBars: List<View> = listOf(
            itemView.findViewById(R.id.bar1),
            itemView.findViewById(R.id.bar2),
            itemView.findViewById(R.id.bar3),
            itemView.findViewById(R.id.bar4)
        )

        fun bind(data: OnboardingData, index: Int) {
            title.text = data.title
            heading.text = data.heading
            description.text = data.description
            image.setImageResource(data.imageRes)
            btnNext.text = data.buttonText

            skip.paintFlags = skip.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            skip.visibility = if (index == slides.lastIndex) View.INVISIBLE else View.VISIBLE

            updateProgress(index)

            btnNext.setOnClickListener {
                val viewPager = activity.findViewById<ViewPager2>(R.id.viewPager)
                if (index < slides.lastIndex) {
                    viewPager.setCurrentItem(index + 1, true)
                } else {
                    itemView.findNavController().navigate(R.id.action_onboarding_to_login)
                }
            }

            skip.setOnClickListener {
                itemView.findNavController().navigate(R.id.action_onboarding_to_login)
            }
        }

         fun updateProgress(index: Int) {
            progressBars.forEachIndexed { i, bar ->
                bar.setBackgroundResource(
                    if (i == index) R.drawable.progress_selected
                    else R.drawable.progress_element
                )
            }
        }
    }
}
