package com.example.expenso.Data_Class

data class Get_Budget(
    val budgets: List<Budget>,
    val message: String,
    val totalBudget: Int
)