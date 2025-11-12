package com.example.tripsync

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.api.models.FunFact

class FunFactsAdapter(private var items: List<FunFact>) : RecyclerView.Adapter<FunFactsAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val indexView: TextView = itemView.findViewById(R.id.item_index)
        val textView: TextView = itemView.findViewById(R.id.item_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_fun_fact, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val fact = items[position]
        holder.indexView.text = "${position + 1}."
        holder.textView.text = fact.title ?: fact.desc ?: ""
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<FunFact>) {
        items = newItems
        notifyDataSetChanged()
    }
}
