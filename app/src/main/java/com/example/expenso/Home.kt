package com.example.expenso

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expenso.ApiUsage.API
import com.example.expenso.ApiUsage.Constant
import com.example.expenso.ApiUsage.obj
import com.example.expenso.Data_Class.ExpenseListResponse
import com.example.expenso.Data_Class.ExpenseResponse
import com.example.expenso.view.SemiCircularProgressBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class Home : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var semiProgressBar: SemiCircularProgressBar
    private lateinit var adapter: RecyclerListAdaptor
    private lateinit var tvMonthYear: TextView
    private lateinit var btnPrevMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton

    private var allExpenses: List<ExpenseResponse> = emptyList()

    // Use Calendar for selected date tracking
    private var selectedCalendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        recyclerView = findViewById(R.id.home_recycler)
        semiProgressBar = findViewById(R.id.semiProgressBar)

        tvMonthYear = findViewById(R.id.tv_month_year_home)
        btnPrevMonth = findViewById(R.id.btn_prev_month_home)
        btnNextMonth = findViewById(R.id.btn_next_month_home)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_nav_Home)
        bottomNavigationView.selectedItemId = R.id.nav_home

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home->{
                    startActivity(Intent(this, Home::class.java))
                    item.icon?.setTint(Color.parseColor("#0000FF"))
                    return@setOnItemSelectedListener true
                }
                R.id.nav_category -> {
                    // Open Budget Categories Activity
                    startActivity(Intent(this, Budget_Categories::class.java))

                    return@setOnItemSelectedListener true

                }
                R.id.nav_profile -> {
                    // Open Profile Activity
                    startActivity(Intent(this, Profile::class.java))
                    return@setOnItemSelectedListener true
                }
                R.id.nav_report->{
                    startActivity(Intent(this, Expenses::class.java))
                    return@setOnItemSelectedListener true
                }

                else -> false
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)

        btnPrevMonth.setOnClickListener {
            selectedCalendar.add(Calendar.MONTH, -1)
            updateUIForSelectedMonth()
        }
        btnNextMonth.setOnClickListener {
            selectedCalendar.add(Calendar.MONTH, 1)
            updateUIForSelectedMonth()
        }

        fetchAndDisplayRecyclerViewData()
    }

    private fun fetchAndDisplayRecyclerViewData() {
        val token = Constant.getSavedToken(this)
        val retrofit = obj.createService(API::class.java)

        retrofit.getExpenses(token.toString()).enqueue(object : Callback<ExpenseListResponse> {
            override fun onResponse(
                call: Call<ExpenseListResponse>,
                response: Response<ExpenseListResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val expenseResponse = response.body()!!
                    allExpenses = expenseResponse.expenses ?: emptyList()
                    updateUIForSelectedMonth()
                } else {
                    Log.e("HomeActivity", "API response unsuccessful or empty")
                }
            }

            override fun onFailure(call: Call<ExpenseListResponse>, t: Throwable) {
                Log.e("HomeActivity", "Failed to fetch expenses", t)
            }
        })
    }

    private fun updateUIForSelectedMonth() {
        val displayFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvMonthYear.text = displayFormat.format(selectedCalendar.time)

        val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        isoDateFormat.timeZone = TimeZone.getTimeZone("UTC")

        val filteredExpenses = allExpenses.filter { expense ->
            expense.date?.let {
                try {
                    val expenseDate = isoDateFormat.parse(it) ?: return@filter false
                    val expenseCal = Calendar.getInstance().apply { time = expenseDate }
                    expenseCal.get(Calendar.MONTH) == selectedCalendar.get(Calendar.MONTH) &&
                            expenseCal.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR)
                } catch (e: ParseException) {
                    false
                }
            } ?: false
        }

        adapter = RecyclerListAdaptor(filteredExpenses)
        recyclerView.adapter = adapter

        val totalSpent = filteredExpenses.sumOf { expense ->
            when (val amt = expense.amount) {
                is Number -> amt.toDouble()
                is String -> amt.toDoubleOrNull() ?: 0.0
                else -> 0.0
            }
        }

        val budgetLimit = 10000.0
        val progress = ((totalSpent / budgetLimit) * 100.0).coerceIn(0.0, 100.0)
        semiProgressBar.progress = progress.toFloat()
    }
}

