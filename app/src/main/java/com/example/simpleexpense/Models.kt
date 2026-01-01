package com.example.simpleexpense

data class Expense(
    val id: String,
    val amount: Double,
    val paymentMethod: String,
    val category: String,
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

}

data class ExpenseSummary(
    val totalExpense: Double,
    val expenseCount: Int,
    val byPaymentMethod: Map<String, Double>,
    val byCategory: Map<String, Double>
)

object ExpenseCategory {
    val categories = arrayOf(
        "🍔 Makanan & Minuman",
        "🚗 Transport",
        "🛒 Belanja",
        "🎮 Hiburan",
        "💡 Tagihan",
        "🏥 Kesehatan",
        "📚 Pendidikan",
        "👔 Fashion",
        "🏠 Rumah Tangga",
        "💼 Lainnya"
    )

    fun getCategoryColor(category: String): String {
        return when {
            category.contains("Makanan") -> "#FF6B6B"
            category.contains("Transport") -> "#4ECDC4"
            category.contains("Belanja") -> "#45B7D1"
            category.contains("Hiburan") -> "#FFA07A"
            category.contains("Tagihan") -> "#F39C12"
            category.contains("Kesehatan") -> "#E74C3C"
            category.contains("Pendidikan") -> "#9B59B6"
            category.contains("Fashion") -> "#E91E63"
            category.contains("Rumah") -> "#795548"
            else -> "#95A5A6"
        }
    }
}