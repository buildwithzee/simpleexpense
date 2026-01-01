package com.example.simpleexpense

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import androidx.core.graphics.toColorInt

@Suppress("DEPRECATION")
class ExpenseDetailActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvTotal: TextView
    private lateinit var tvBudgetInfo: TextView
    private lateinit var progressBudget: ProgressBar
    private lateinit var rvExpenses: RecyclerView
    private lateinit var fabAddExpense: FloatingActionButton
    private lateinit var btnSetBudget: Button
    private lateinit var btnViewChart: Button
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
        tvBudgetInfo = findViewById(R.id.tvBudgetInfo)
        progressBudget = findViewById(R.id.progressBudget)
        rvExpenses = findViewById(R.id.rvExpenses)
        fabAddExpense = findViewById(R.id.fabAddExpense)
        btnSetBudget = findViewById(R.id.btnSetBudget)
        btnViewChart = findViewById(R.id.btnViewChart)
        emptyView = findViewById(R.id.emptyView)

        tvTitle.text = monthYear.getDisplayName()

        setupRecyclerView()
        loadExpenses()

        fabAddExpense.setOnClickListener {
            showAddExpenseDialog()
        }

        btnSetBudget.setOnClickListener {
            showSetBudgetDialog()
        }

        btnViewChart.setOnClickListener {
            showChartDialog()
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

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun loadExpenses() {
        val expenses = database.getExpenses(monthYear)
        adapter.updateData(expenses)

        val total = expenses.sumOf { it.amount }
        tvTotal.text = "Total: Rp ${String.format("%,.0f", total)}"

        // Update budget info
        val budget = database.getBudget(monthYear)
        if (budget != null) {
            val percentage = ((total / budget) * 100).toInt()
            progressBudget.progress = percentage.coerceAtMost(100)

            val remaining = budget - total
            val status = when {
                total > budget -> "⚠️ Melebihi budget Rp ${String.format("%,.0f", total - budget)}"
                remaining < budget * 0.2 -> "⚠️ Sisa Rp ${String.format("%,.0f", remaining)}"
                else -> "✅ Sisa Rp ${String.format("%,.0f", remaining)}"
            }

            tvBudgetInfo.text = "Budget: Rp ${String.format("%,.0f", budget)} • $status"
            tvBudgetInfo.visibility = View.VISIBLE
            progressBudget.visibility = View.VISIBLE

            // Set color based on percentage
            val color = when {
                percentage >= 100 -> "#F44336".toColorInt()
                percentage >= 80 -> "#FF9800".toColorInt()
                else -> "#4CAF50".toColorInt()
            }
            progressBudget.progressDrawable.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
        } else {
            tvBudgetInfo.visibility = View.GONE
            progressBudget.visibility = View.GONE
        }

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
            showCategoryDialog { category ->
                showPaymentMethodDialog { paymentMethod ->
                    showDescriptionDialog { description ->
                        val expense = Expense(
                            id = UUID.randomUUID().toString(),
                            amount = amount,
                            paymentMethod = paymentMethod,
                            category = category,
                            description = description
                        )
                        database.addExpense(expense, monthYear)
                        loadExpenses()
                    }
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

    private fun showCategoryDialog(onComplete: (String) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_category, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerCategory)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ExpenseCategory.categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        AlertDialog.Builder(this)
            .setTitle("Kategori")
            .setView(dialogView)
            .setPositiveButton("Lanjut") { _, _ ->
                onComplete(spinner.selectedItem.toString())
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
            showCategoryDialog { category ->
                showPaymentMethodDialog { paymentMethod ->
                    showDescriptionDialog { description ->
                        val updatedExpense = expense.copy(
                            amount = amount,
                            paymentMethod = paymentMethod,
                            category = category,
                            description = description,
                            timestamp = System.currentTimeMillis()
                        )
                        database.updateExpense(updatedExpense)
                        loadExpenses()
                    }
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

    private fun showSetBudgetDialog() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "Contoh: 5000000"

            val currentBudget = database.getBudget(monthYear)
            if (currentBudget != null) {
                setText(currentBudget.toInt().toString())
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Set Budget ${monthYear.getDisplayName()}")
            .setMessage("Masukkan target budget bulanan:")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->
                val budgetStr = input.text.toString()
                if (budgetStr.isNotEmpty()) {
                    database.setBudget(monthYear, budgetStr.toDouble())
                    loadExpenses()
                    Toast.makeText(this, "Budget berhasil disimpan!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun showChartDialog() {
        val summary = database.getSummary(monthYear)

        if (summary.byCategory.isEmpty()) {
            Toast.makeText(this, "Belum ada data pengeluaran", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_chart, null)
        val chartContainer = dialogView.findViewById<LinearLayout>(R.id.chartContainer)

        // Create simple bar chart for categories
        summary.byCategory.entries.sortedByDescending { it.value }.forEach { (category, amount) ->
            val percentage = (amount / summary.totalExpense * 100).toInt()

            val itemView = layoutInflater.inflate(R.layout.item_chart_bar, chartContainer, false)
            val tvCategoryName = itemView.findViewById<TextView>(R.id.tvCategoryName)
            val progressBar = itemView.findViewById<ProgressBar>(R.id.progressBar)
            val tvAmount = itemView.findViewById<TextView>(R.id.tvAmount)
            val tvPercentage = itemView.findViewById<TextView>(R.id.tvPercentage)

            tvCategoryName.text = category
            progressBar.progress = percentage
            tvAmount.text = "Rp ${String.format("%,.0f", amount)}"
            tvPercentage.text = "$percentage%"

            // Set color
            val color = ExpenseCategory.getCategoryColor(category).toColorInt()
            progressBar.progressDrawable.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)

            chartContainer.addView(itemView)
        }

        AlertDialog.Builder(this)
            .setTitle("📊 Grafik Pengeluaran per Kategori")
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show()
    }
}