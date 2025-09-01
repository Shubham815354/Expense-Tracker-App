package com.example.expenso.Data_Class

data class Budget(
    val __v: Int,
    val _id: String,
    val amount: Int,
    val categoryId: CategoryId,
    val createdAt: String,
    val frequency: String,
    val updatedAt: String,
    val userId: String
)