package com.example.expenso.Data_Class

data class Add_expense_response(
    val budgetExceeded: Boolean,
    val budgetLimit: Int,
    val currentTotal: Int,
    val expense: ExpenseZ,
    val frequency: String,
    val message: String
)
data class ExpenseZ(
    val __v: Int,
    val _id: String,
    val amount: Int,
    val categoryId: String,
    val createdAt: String,
    val date: String,
    val note: String,
    val updatedAt: String,
    val userId: String
)