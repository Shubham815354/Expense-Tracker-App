package com.example.expenso.Data_Class

data class CategoryItem(  val title: String,
                          val iconUrl: String?,       // Change from iconResId: Int
                          val firstLetter: Char,
                          val id: String?,
                          val isUserCreated: Boolean = false)
