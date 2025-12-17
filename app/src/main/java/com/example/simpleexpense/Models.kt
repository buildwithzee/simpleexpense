package com.example.simpleexpense

data class Expense(
    val id: String,
    val amount: Double,
    val paymentMethod: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class MonthYear(
    val month: Int,
    val year: Int
) {
    fun getDisplayName(): String {
        val months = arrayOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        return "${months[month - 1]} $year"
    }

    fun getKey(): String = "$year-${month.toString().padStart(2, '0')}"
}

data class ExpenseSummary(
    val totalExpense: Double,
    val expenseCount: Int,
    val byPaymentMethod: Map<String, Double>
)