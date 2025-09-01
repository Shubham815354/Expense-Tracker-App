package com.example.expenso.Data_Class

data class Update_Expense(
    val expense: ExpenseY,
    val message: String
)
data class ExpenseY(
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