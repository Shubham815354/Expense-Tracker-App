package com.example.expenso.Data_Class

data class Expense( val id: String,
                    val name: String,
                    val amount: String,
                    val color: String,
                    val percentage: String,
                    val date: String,
                    val category: Category?,
                    val note: String)