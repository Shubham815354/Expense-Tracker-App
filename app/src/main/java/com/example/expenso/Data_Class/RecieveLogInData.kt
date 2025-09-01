package com.example.expenso.Data_Class

data class RecieveLogInData(
    val message: String,
    val success: Boolean,
    val token: String,
    val user: User
)