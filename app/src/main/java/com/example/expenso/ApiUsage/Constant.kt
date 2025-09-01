package com.example.expenso.ApiUsage

import android.content.Context
import com.example.expenso.Data_Class.Category
import com.example.expenso.Data_Class.Fetch_CategoryId
import com.example.expenso.Data_Class.ProfileX
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback

object Constant {
    private const val PREF_NAME = "ExpensooPref"
    private const val PROFILE_PREF_NAME = "user_profile"

    private const val KEY_EMAIL_USER = "Email_ID_User"
    private const val KEY_IS_LOGGED_IN = "Is_Logged_In"
    private const val KEY_TOKEN = "AuthToken"
    private const val KEY_PROFILE_PIC = "profilePic"
    private const val KEY_USER_NAME = "User_Name"
    private const val KEY_USER_PHONE = "User_Phone"
    private const val KEY_USER_ADDRESS = "User_Address"

    // Login Data
    fun saveLoginData(context: Context, email: String, token: String, isLoggedIn: Boolean) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(KEY_EMAIL_USER, email)
            putString(KEY_TOKEN, token)
            putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
            apply()
        }
    }
    fun clearProfileCreated(context: Context, email: String) {
        val prefs = context.getSharedPreferences(PROFILE_PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove("PROFILE_CREATED_$email").apply()
    }
    fun setProfileCreated(context: Context, email: String, created: Boolean) {
        val prefs = context.getSharedPreferences(PROFILE_PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("PROFILE_CREATED_$email", created).apply()
    }

    fun isProfileCreated(context: Context, email: String): Boolean {
        val prefs = context.getSharedPreferences(PROFILE_PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("PROFILE_CREATED_$email", false)
    }


    fun clearLoginData(context: Context) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }
    }

    // Separate Save Methods
    fun saveUserName(context: Context, name: String) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().putString(KEY_USER_NAME, name).apply()
    }

    fun saveUserPhone(context: Context, phone: String) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().putString(KEY_USER_PHONE, phone).apply()
    }

    fun saveUserAddress(context: Context, address: String) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().putString(KEY_USER_ADDRESS, address).apply()
    }

    // Getters
    fun getSavedUserName(context: Context): String? {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_USER_NAME, null)
    }

    fun getSavedUserPhone(context: Context): String? {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_USER_PHONE, null)
    }

    fun getSavedUserAddress(context: Context): String? {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_USER_ADDRESS, null)
    }

    fun getSavedToken(context: Context): String? {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_TOKEN, null)
    }

    fun getSavedEmail(context: Context): String? {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_EMAIL_USER, null)
    }

    fun isLoggedIn(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPref.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // Profile Data
    fun saveProfilePic(context: Context, profilePic: String) {
        val prefs = context.getSharedPreferences(PROFILE_PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PROFILE_PIC, profilePic).apply()
    }

    fun getProfilePic(context: Context): String? {
        val prefs = context.getSharedPreferences(PROFILE_PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PROFILE_PIC, null)
    }

    fun saveCategories(context: Context, categories: List<Fetch_CategoryId>, key: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(categories)
        prefs.edit().putString(key, json).apply()
    }

    fun getCategories(context: Context, key: String): List<Fetch_CategoryId> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(key, null)
        return if (json != null) {
            val type = object : com.google.gson.reflect.TypeToken<List<Fetch_CategoryId>>() {}.type
            Gson().fromJson(json, type)
        } else {
            emptyList()
        }
    }
    private const val KEY_TOTAL_EXPENSE_AMOUNT = "Total_Expense_Amount"

    // Save total amount
    fun saveTotalExpenseAmount(context: Context, amount: Double) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_TOTAL_EXPENSE_AMOUNT, amount.toString()).apply()
    }

    // Get total amount
    fun getTotalExpenseAmount(context: Context): Double {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_TOTAL_EXPENSE_AMOUNT, "0.0")?.toDoubleOrNull() ?: 0.0
    }
    private const val KEY_TOTAL_BUDGET_AMOUNT = "Total_Budget_Amount"

    fun saveTotalBudgetAmount(context: Context, amount: Double) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_TOTAL_BUDGET_AMOUNT, amount.toString()).apply()
    }

    fun getTotalBudgetAmount(context: Context): Double {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_TOTAL_BUDGET_AMOUNT, "0.0")?.toDoubleOrNull() ?: 0.0
    }







}
