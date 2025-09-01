package com.example.expenso.Data_Class

data class ExpenseListResponse(
    val expenses: List<ExpenseResponse>,
    val totalExpense: Int
)

data class ExpenseResponse(val _id: String,
                           val userId: String,
                           val amount: Int,
                           val categoryId: Category,
                           val note: String,
                           val date: String)
data class Category(
    val _id: String,
    val name: String,
    val icon: String
)
