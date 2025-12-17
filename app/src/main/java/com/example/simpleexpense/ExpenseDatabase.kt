package com.example.simpleexpense

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ExpenseDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "expense.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_EXPENSES = "expenses"
        private const val COLUMN_ID = "id"
        private const val COLUMN_AMOUNT = "amount"
        private const val COLUMN_PAYMENT_METHOD = "payment_method"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_MONTH = "month"
        private const val COLUMN_YEAR = "year"
        private const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE $TABLE_EXPENSES (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_AMOUNT REAL,
                $COLUMN_PAYMENT_METHOD TEXT,
                $COLUMN_DESCRIPTION TEXT,
                $COLUMN_MONTH INTEGER,
                $COLUMN_YEAR INTEGER,
                $COLUMN_TIMESTAMP INTEGER
            )
        """.trimIndent()
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSES")
        onCreate(db)
    }

    fun addExpense(expense: Expense, monthYear: MonthYear): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, expense.id)
            put(COLUMN_AMOUNT, expense.amount)
            put(COLUMN_PAYMENT_METHOD, expense.paymentMethod)
            put(COLUMN_DESCRIPTION, expense.description)
            put(COLUMN_MONTH, monthYear.month)
            put(COLUMN_YEAR, monthYear.year)
            put(COLUMN_TIMESTAMP, expense.timestamp)
        }
        val result = db.insert(TABLE_EXPENSES, null, values)
        db.close()
        return result != -1L
    }

    fun getExpenses(monthYear: MonthYear): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_EXPENSES,
            null,
            "$COLUMN_MONTH = ? AND $COLUMN_YEAR = ?",
            arrayOf(monthYear.month.toString(), monthYear.year.toString()),
            null, null,
            "$COLUMN_TIMESTAMP DESC"
        )

        with(cursor) {
            while (moveToNext()) {
                val expense = Expense(
                    id = getString(getColumnIndexOrThrow(COLUMN_ID)),
                    amount = getDouble(getColumnIndexOrThrow(COLUMN_AMOUNT)),
                    paymentMethod = getString(getColumnIndexOrThrow(COLUMN_PAYMENT_METHOD)),
                    description = getString(getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    timestamp = getLong(getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                )
                expenses.add(expense)
            }
        }
        cursor.close()
        db.close()
        return expenses
    }

    fun updateExpense(expense: Expense, monthYear: MonthYear): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_AMOUNT, expense.amount)
            put(COLUMN_PAYMENT_METHOD, expense.paymentMethod)
            put(COLUMN_DESCRIPTION, expense.description)
            put(COLUMN_TIMESTAMP, expense.timestamp)
        }
        val result = db.update(
            TABLE_EXPENSES,
            values,
            "$COLUMN_ID = ?",
            arrayOf(expense.id)
        )
        db.close()
        return result > 0
    }

    fun deleteExpense(expenseId: String): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_EXPENSES, "$COLUMN_ID = ?", arrayOf(expenseId))
        db.close()
        return result > 0
    }

    fun deleteMonthYear(monthYear: MonthYear): Boolean {
        val db = writableDatabase
        val result = db.delete(
            TABLE_EXPENSES,
            "$COLUMN_MONTH = ? AND $COLUMN_YEAR = ?",
            arrayOf(monthYear.month.toString(), monthYear.year.toString())
        )
        db.close()
        return result > 0
    }

    fun getAllMonthYears(): List<MonthYear> {
        val monthYears = mutableListOf<MonthYear>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT DISTINCT $COLUMN_MONTH, $COLUMN_YEAR FROM $TABLE_EXPENSES ORDER BY $COLUMN_YEAR DESC, $COLUMN_MONTH DESC",
            null
        )

        with(cursor) {
            while (moveToNext()) {
                val monthYear = MonthYear(
                    month = getInt(getColumnIndexOrThrow(COLUMN_MONTH)),
                    year = getInt(getColumnIndexOrThrow(COLUMN_YEAR))
                )
                monthYears.add(monthYear)
            }
        }
        cursor.close()
        db.close()
        return monthYears
    }

    fun getSummary(monthYear: MonthYear): ExpenseSummary {
        val db = readableDatabase
        var totalExpense = 0.0
        var expenseCount = 0
        val byPaymentMethod = mutableMapOf<String, Double>()

        val cursor = db.query(
            TABLE_EXPENSES,
            arrayOf(COLUMN_AMOUNT, COLUMN_PAYMENT_METHOD),
            "$COLUMN_MONTH = ? AND $COLUMN_YEAR = ?",
            arrayOf(monthYear.month.toString(), monthYear.year.toString()),
            null, null, null
        )

        with(cursor) {
            while (moveToNext()) {
                val amount = getDouble(getColumnIndexOrThrow(COLUMN_AMOUNT))
                val method = getString(getColumnIndexOrThrow(COLUMN_PAYMENT_METHOD))

                totalExpense += amount
                expenseCount++

                byPaymentMethod[method] = (byPaymentMethod[method] ?: 0.0) + amount
            }
        }
        cursor.close()
        db.close()

        return ExpenseSummary(totalExpense, expenseCount, byPaymentMethod)
    }
}