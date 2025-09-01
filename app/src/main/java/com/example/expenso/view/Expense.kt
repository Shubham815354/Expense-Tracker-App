package com.example.expenso.view

data class Expense(
    val __v: Int,
    val _id: String,
    val amount: Int,
    val categoryId: CategoryId,
    val createdAt: String,
    val date: String,
    val note: String,
    val updatedAt: String,
    val userId: String
)