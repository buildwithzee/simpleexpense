package com.example.simpleexpense

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MonthYearAdapter(
    private var monthYears: List<MonthYear>,
    private val onItemClick: (MonthYear) -> Unit
) : RecyclerView.Adapter<MonthYearAdapter.MonthYearViewHolder>() {

    class MonthYearViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMonthYear: TextView = view.findViewById(R.id.tvMonthYear)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthYearViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_month_year, parent, false)
        return MonthYearViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthYearViewHolder, position: Int) {
        val monthYear = monthYears[position]
        holder.tvMonthYear.text = monthYear.getDisplayName()
        holder.itemView.setOnClickListener { onItemClick(monthYear) }
    }

    override fun getItemCount() = monthYears.size

    fun updateData(newMonthYears: List<MonthYear>) {
        monthYears = newMonthYears
        notifyDataSetChanged()
    }
}