package com.example.tripsync

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tripsync.api.models.TrendingPlace

class FunFactPagerAdapter(
    private val place: TrendingPlace
) : RecyclerView.Adapter<FunFactPagerAdapter.PageViewHolder>() {

    private val pages: List<PageData>

    init {
        val list = ArrayList<PageData>()
        list.add(PageData(place.main, place.name ?: "", null))
        place.fun_facts?.let {
            for (f in it) {
                list.add(PageData(f.photo, f.title, f.desc))
            }
        }
        pages = list
    }

    data class PageData(val imageUrl: String?, val title: String?, val desc: String?)

    inner class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.slide_image)
        private val title: TextView = itemView.findViewById(R.id.slide_title)
        private val desc: TextView = itemView.findViewById(R.id.slide_desc)

        fun bind(data: PageData, position: Int) {
            Glide.with(itemView.context)
                .load(data.imageUrl)
                .centerCrop()
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(image)

            val num = if (position > 0) "${position}. " else ""
            val rawTitle = data.title ?: ""
            title.text = "$num$rawTitle"

            val cleaned = data.desc
                ?.replace(Regex("[\\p{C}]"), " ")
                ?.replace("\r\n", "\n")
                ?.trim()
                ?: ""
            desc.text = cleaned
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_fun_fact_slide, parent, false)
        return PageViewHolder(v)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.bind(pages[position], position)
    }

    override fun getItemCount(): Int = pages.size
}
