package com.example.simpleexpense

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter(
    private var expenses: List<Expense>,
    private val onEditClick: (Expense) -> Unit,
    private val onDeleteClick: (Expense) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvPaymentMethod: TextView = view.findViewById(R.id.tvPaymentMethod)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val dateFormatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID"))

        holder.tvAmount.text = formatter.format(expense.amount)
        holder.tvPaymentMethod.text = expense.paymentMethod
        holder.tvDescription.text = expense.description
        holder.tvDate.text = dateFormatter.format(Date(expense.timestamp))

        holder.btnEdit.setOnClickListener { onEditClick(expense) }
        holder.btnDelete.setOnClickListener { onDeleteClick(expense) }
    }

    override fun getItemCount() = expenses.size

    fun updateData(newExpenses: List<Expense>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }
}