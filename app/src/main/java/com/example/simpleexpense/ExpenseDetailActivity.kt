package com.example.simpleexpense

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class ExpenseDetailActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvTotal: TextView
    private lateinit var rvExpenses: RecyclerView
    private lateinit var fabAddExpense: FloatingActionButton
    private lateinit var adapter: ExpenseAdapter
    private lateinit var database: ExpenseDatabase
    private lateinit var monthYear: MonthYear
    private lateinit var emptyView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_detail)

        val month = intent.getIntExtra("month", 1)
        val year = intent.getIntExtra("year", 2025)
        monthYear = MonthYear(month, year)

        database = ExpenseDatabase(this)

        tvTitle = findViewById(R.id.tvTitle)
        tvTotal = findViewById(R.id.tvTotal)
        rvExpenses = findViewById(R.id.rvExpenses)
        fabAddExpense = findViewById(R.id.fabAddExpense)
        emptyView = findViewById(R.id.emptyView)

        tvTitle.text = monthYear.getDisplayName()

        setupRecyclerView()
        loadExpenses()

        fabAddExpense.setOnClickListener {
            showAddExpenseDialog()
        }

        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(
            emptyList(),
            onEditClick = { expense -> showEditExpenseDialog(expense) },
            onDeleteClick = { expense -> confirmDeleteExpense(expense) }
        )
        rvExpenses.layoutManager = LinearLayoutManager(this)
        rvExpenses.adapter = adapter
    }

    private fun loadExpenses() {
        val expenses = database.getExpenses(monthYear)
        adapter.updateData(expenses)

        val total = expenses.sumOf { it.amount }
        tvTotal.text = "Total: Rp ${String.format("%,.0f", total)}"

        if (expenses.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            rvExpenses.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            rvExpenses.visibility = View.VISIBLE
        }
    }

    private fun showAddExpenseDialog() {
        showAmountDialog { amount ->
            showPaymentMethodDialog { paymentMethod ->
                showDescriptionDialog { description ->
                    val expense = Expense(
                        id = UUID.randomUUID().toString(),
                        amount = amount,
                        paymentMethod = paymentMethod,
                        description = description
                    )
                    database.addExpense(expense, monthYear)
                    loadExpenses()
                }
            }
        }
    }

    private fun showAmountDialog(onComplete: (Double) -> Unit) {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "Contoh: 50000"
        }

        AlertDialog.Builder(this)
            .setTitle("Nominal Pengeluaran")
            .setView(input)
            .setPositiveButton("Lanjut") { _, _ ->
                val amountStr = input.text.toString()
                if (amountStr.isNotEmpty()) {
                    onComplete(amountStr.toDouble())
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showPaymentMethodDialog(onComplete: (String) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_payment_method, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerPaymentMethod)

        val methods = arrayOf("Cash", "Transfer Bank", "E-Wallet", "Kartu Debit", "Kartu Kredit")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, methods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        AlertDialog.Builder(this)
            .setTitle("Metode Pembayaran")
            .setView(dialogView)
            .setPositiveButton("Lanjut") { _, _ ->
                onComplete(spinner.selectedItem.toString())
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDescriptionDialog(onComplete: (String) -> Unit) {
        val input = EditText(this).apply {
            hint = "Contoh: Makan siang, Belanja bulanan, dll"
        }

        AlertDialog.Builder(this)
            .setTitle("Keterangan")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->
                val description = input.text.toString().ifEmpty { "Tanpa keterangan" }
                onComplete(description)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditExpenseDialog(expense: Expense) {
        showAmountDialog { amount ->
            showPaymentMethodDialog { paymentMethod ->
                showDescriptionDialog { description ->
                    val updatedExpense = expense.copy(
                        amount = amount,
                        paymentMethod = paymentMethod,
                        description = description,
                        timestamp = System.currentTimeMillis()
                    )
                    database.updateExpense(updatedExpense, monthYear)
                    loadExpenses()
                }
            }
        }
    }

    private fun confirmDeleteExpense(expense: Expense) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Pengeluaran")
            .setMessage("Yakin ingin menghapus pengeluaran ini?")
            .setPositiveButton("Hapus") { _, _ ->
                database.deleteExpense(expense.id)
                loadExpenses()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}