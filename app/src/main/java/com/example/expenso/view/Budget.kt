package com.example.expenso.view

data class Budget(
    val __v: Int,
    val _id: String,
    val amount: Int,
    val categoryId: CategoryIdX,
    val createdAt: String,
    val frequency: String,
    val updatedAt: String,
    val userId: String
)