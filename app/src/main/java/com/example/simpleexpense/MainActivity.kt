package com.example.simpleexpense

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var rvMonthYears: RecyclerView
    private lateinit var fabAddMonth: FloatingActionButton
    private lateinit var adapter: MonthYearAdapter
    private lateinit var database: ExpenseDatabase
    private lateinit var emptyView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = ExpenseDatabase(this)

        rvMonthYears = findViewById(R.id.rvMonthYears)
        fabAddMonth = findViewById(R.id.fabAddMonth)
        emptyView = findViewById(R.id.emptyView)

        setupRecyclerView()
        loadMonthYears()

        fabAddMonth.setOnClickListener {
            showAddMonthDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = MonthYearAdapter(emptyList()) { monthYear ->
            showMonthOptionsDialog(monthYear)
        }
        rvMonthYears.layoutManager = LinearLayoutManager(this)
        rvMonthYears.adapter = adapter
    }

    private fun loadMonthYears() {
        val monthYears = database.getAllMonthYears()
        adapter.updateData(monthYears)

        if (monthYears.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            rvMonthYears.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            rvMonthYears.visibility = View.VISIBLE
        }
    }

    private fun showAddMonthDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_month, null)
        val spinnerMonth = dialogView.findViewById<Spinner>(R.id.spinnerMonth)
        val spinnerYear = dialogView.findViewById<Spinner>(R.id.spinnerYear)

        val months = arrayOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonth.adapter = monthAdapter

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 5..currentYear + 5).map { it.toString() }
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYear.adapter = yearAdapter
        spinnerYear.setSelection(years.indexOf(currentYear.toString()))

        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        spinnerMonth.setSelection(currentMonth)

        AlertDialog.Builder(this)
            .setTitle("Tambah Bulan Baru")
            .setView(dialogView)
            .setPositiveButton("Tambah") { _, _ ->
                val month = spinnerMonth.selectedItemPosition + 1
                val year = spinnerYear.selectedItem.toString().toInt()
                val monthYear = MonthYear(month, year)

                val existingMonths = database.getAllMonthYears()
                val exists = existingMonths.any { it.month == month && it.year == year }

                if (!exists) {
                    val intent = Intent(this, ExpenseDetailActivity::class.java)
                    intent.putExtra("month", month)
                    intent.putExtra("year", year)
                    startActivity(intent)
                } else {
                    AlertDialog.Builder(this)
                        .setMessage("Bulan ${monthYear.getDisplayName()} sudah ada!")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showMonthOptionsDialog(monthYear: MonthYear) {
        val options = arrayOf(
            "Tambah Pengeluaran",
            "Lihat Daftar Pengeluaran",
            "Lihat Rangkuman",
            "Hapus Bulan Ini"
        )

        AlertDialog.Builder(this)
            .setTitle(monthYear.getDisplayName())
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openExpenseDetail(monthYear)
                    1 -> openExpenseDetail(monthYear)
                    2 -> showSummary(monthYear)
                    3 -> confirmDeleteMonth(monthYear)
                }
            }
            .show()
    }

    private fun openExpenseDetail(monthYear: MonthYear) {
        val intent = Intent(this, ExpenseDetailActivity::class.java)
        intent.putExtra("month", monthYear.month)
        intent.putExtra("year", monthYear.year)
        startActivity(intent)
    }

    private fun showSummary(monthYear: MonthYear) {
        val summary = database.getSummary(monthYear)
        val message = buildString {
            append("Total Pengeluaran:\n")
            append("Rp ${String.format("%,.0f", summary.totalExpense)}\n\n")
            append("Jumlah Transaksi: ${summary.expenseCount}\n\n")

            if (summary.byPaymentMethod.isNotEmpty()) {
                append("Per Metode Pembayaran:\n")
                summary.byPaymentMethod.forEach { (method, amount) ->
                    append("• $method: Rp ${String.format("%,.0f", amount)}\n")
                }
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Rangkuman ${monthYear.getDisplayName()}")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun confirmDeleteMonth(monthYear: MonthYear) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Bulan")
            .setMessage("Yakin ingin menghapus semua data ${monthYear.getDisplayName()}?")
            .setPositiveButton("Hapus") { _, _ ->
                database.deleteMonthYear(monthYear)
                loadMonthYears()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadMonthYears()
    }
}