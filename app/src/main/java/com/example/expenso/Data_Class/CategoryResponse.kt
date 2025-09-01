package com.example.expenso.Data_Class

data class CategoryResponse(
    val message: String,
    val category: List<ApiCategory>?
)

data class UserCategoryResponse(
    val message: String,
    val categories: List<ApiCategory>
)

data class ApiCategory(
    val _id: String,
    val name: String,
    val isDefault: Boolean,
    val userId: String?,
    val icon: String? // URL or null
)
data class ApiCategoryPostResponse(
    val message: String,
    val category: ApiCategory
)