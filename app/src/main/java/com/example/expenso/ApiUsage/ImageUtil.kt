package com.example.expenso.ApiUsage

object ImageUtil {
    private const val BASE_URL = "https://expensio-nkvc.onrender.com:3010/"

    fun getFullImageUrl(imagePath: String): String {
        return if (imagePath.startsWith("http")) {
            imagePath  // Already a full URL, don't prepend
        } else {
            BASE_URL + imagePath.removePrefix("/")  // Prevent accidental "//"
        }
    }
}
