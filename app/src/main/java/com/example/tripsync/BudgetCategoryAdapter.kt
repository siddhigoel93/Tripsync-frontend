package com.example.tripsync

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

data class BudgetCategory(val title: String, val percent: Int, val iconRes: Int)

class BudgetCategoryAdapter : RecyclerView.Adapter<BudgetCategoryAdapter.VH>() {

    private val items = mutableListOf<BudgetCategory>()
    private var totalBudget: Long = 0L
    private val nf = NumberFormat.getInstance(Locale("en", "IN"))

    var onListChanged: (() -> Unit)? = null

    fun setTotalBudget(value: Long) {
        totalBudget = value
        notifyDataSetChanged()
        onListChanged?.invoke()
    }

    fun addCategory(cat: BudgetCategory) {
        items.add(cat)
        notifyItemInserted(items.size - 1)
        onListChanged?.invoke()
    }

    fun totalPercent(): Int = items.sumOf { it.percent }
    fun count(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_budget_category, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val titleShown = if (item.title.equals("Accomodation", true)) "Accommodation" else item.title
        holder.icon.setImageResource(item.iconRes)
        holder.title.text = titleShown
        holder.sub.text = "${item.percent}% of budget"
        val value = (totalBudget * item.percent / 100.0).roundToLong()
        holder.amount.text = "₹" + nf.format(value)
    }

    override fun getItemCount(): Int = items.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.ic_icon)
        val title: TextView = v.findViewById(R.id.tv_title)
        val sub: TextView = v.findViewById(R.id.tv_sub)
        val amount: TextView = v.findViewById(R.id.tv_amount)
    }
}
