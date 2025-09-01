package com.example.expenso.Data_Class

data class BudgetDetail(
    val budgetDetails: List<YourBudgetItemType>
)

data class YourBudgetItemType(
    val _id: String,
    val categoryId: String,
    val amount: String,
    val frequency: String
)
