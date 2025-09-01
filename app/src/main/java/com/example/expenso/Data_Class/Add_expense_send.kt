package com.example.expenso.Data_Class

data class Add_expense_send(
    val amount: String,
    val categoryId: String,
    val date: String,
    val note: String
)