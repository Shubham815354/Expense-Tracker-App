package com.example.expenso

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.expenso.ApiUsage.API
import com.example.expenso.ApiUsage.Constant
import com.example.expenso.ApiUsage.obj
import com.example.expenso.Data_Class.ExpenseListResponse
import com.example.expenso.Data_Class.Get_Budget
import com.example.expenso.view.AnalysisData
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class Analysis : AppCompatActivity() {
    private val selectedCalendar = Calendar.getInstance()
    private var selectedFrequency = "daily"
    private lateinit var tvMonthYear: TextView
    private lateinit var btnPrevMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton
    private lateinit var  statistic: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)
        tvMonthYear = findViewById(R.id.tv_month_year)
        btnPrevMonth = findViewById(R.id.btn_prev_month)
        btnNextMonth = findViewById(R.id.btn_next_month)

        val daily_data = findViewById<Button>(R.id.daily_tv)
        val weekly_data = findViewById<Button>(R.id.weekly_tv)
        val monthly_data = findViewById<Button>(R.id.monthly_tv)
        statistic = findViewById(R.id.statistic_textview)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_nav_Analy)
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


        updateMonthYearDisplay()

        fun resetButtonStyles() {
            daily_data.setBackgroundResource(R.drawable.rounded_rectangle)
            weekly_data.setBackgroundResource(R.drawable.rounded_rectangle)
            monthly_data.setBackgroundResource(R.drawable.rounded_rectangle)

            daily_data.setTextColor(Color.BLACK)
            weekly_data.setTextColor(Color.BLACK)
            monthly_data.setTextColor(Color.BLACK)
        }

        daily_data.setOnClickListener {
            selectedFrequency = "daily"
            resetButtonStyles()
            daily_data.setBackgroundResource(R.drawable.analysisdailybg)
            daily_data.setTextColor(Color.WHITE)
            setupAnalysisData(selectedFrequency, selectedCalendar.time)
        }

        weekly_data.setOnClickListener {
            selectedFrequency = "weekly"
            resetButtonStyles()
            weekly_data.setBackgroundResource(R.drawable.analysisdailybg)
            weekly_data.setTextColor(Color.WHITE)
            setupAnalysisData(selectedFrequency, selectedCalendar.time)
        }

        monthly_data.setOnClickListener {
            selectedFrequency = "monthly"
            resetButtonStyles()
            monthly_data.setBackgroundResource(R.drawable.analysisdailybg)
            monthly_data.setTextColor(Color.WHITE)
            setupAnalysisData(selectedFrequency, selectedCalendar.time)
        }
        btnPrevMonth.setOnClickListener {
            selectedCalendar.add(Calendar.MONTH, -1)  // go to previous month
            updateMonthYearDisplay()
            setupAnalysisData(selectedFrequency, selectedCalendar.time)
        }

        btnNextMonth.setOnClickListener {
            selectedCalendar.add(Calendar.MONTH, 1)  // go to next month
            updateMonthYearDisplay()
            setupAnalysisData(selectedFrequency, selectedCalendar.time)
        }


        setupAnalysisData(selectedFrequency, selectedCalendar.time)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_report -> {
                    startActivity(Intent(this, Expenses::class.java))
                    item.icon?.setTint(Color.parseColor("#0000FF"))
                    true
                }
                R.id.nav_category -> {
                    startActivity(Intent(this, Budget_Categories::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, Profile::class.java))
                    true
                }
                else -> false
            }
        }

    }

    private fun setupSampleChart(data: List<Pair<String, Float>>) {
        val barChart = findViewById<BarChart>(R.id.barChart)

        val entries = data.mapIndexed { index, item ->
            BarEntry(index.toFloat(), item.second)
        }

        val barDataSet = BarDataSet(entries, "Monthly Income")
        barDataSet.setColors(data.mapIndexed { index, _ ->
            if (index == 3) Color.parseColor("#2C9C99") else Color.parseColor("#4CD7D0")
        })

        val barData = BarData(barDataSet)
        barData.barWidth = 0.9f
        barData.setDrawValues(false)

        barChart.data = barData

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(data.map { it.first })
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.labelCount = data.size

        val leftAxis: YAxis = barChart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.granularity = 1f
        leftAxis.setDrawGridLines(true)
        leftAxis.textColor = Color.GRAY

        barChart.axisRight.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.description.isEnabled = false
        barChart.setTouchEnabled(false)
        barChart.setScaleEnabled(false)
        barChart.animateY(1000)
        barChart.invalidate()
    }


    private fun setupAnalysisData(frequency: String, selectedDate: Date) {
        if(frequency=="daily"){
            statistic.text="Daily Statistic"
        }else if(frequency=="weekly"){
            statistic.text="Weekly Statistic"
        }else{
            statistic.text="Monthly Statistic"
        }
        val token = Constant.getSavedToken(this)
        val retrofit = obj.createService(API::class.java)

        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
        val selectedMonth = calendar.get(Calendar.MONTH) + 1
        val selectedYear = calendar.get(Calendar.YEAR)

        retrofit.get_budget(token.toString()).enqueue(object : Callback<Get_Budget> {
            override fun onResponse(call: Call<Get_Budget>, response: Response<Get_Budget>) {
                if (response.isSuccessful) {
                    val analysisData = response.body()
                    if (analysisData != null) {
                        val allBudgets = analysisData.budgets ?: emptyList()
                        val targetMonthYear = String.format("%04d-%02d", selectedYear, selectedMonth)

                        val relevantBudgets = allBudgets.filter {
                            it.frequency == frequency && it.createdAt.startsWith(targetMonthYear)
                        }

                        val filteredBudgets = allBudgets.filter {
                            it.createdAt.startsWith(targetMonthYear)
                        }

                        val groupedBudgets = filteredBudgets.groupBy { it.frequency }

                        val totalDaily = groupedBudgets["daily"]?.sumOf { it.amount } ?: 0
                        val totalWeekly = groupedBudgets["weekly"]?.sumOf { it.amount } ?: 0
                        val totalMonthly = groupedBudgets["monthly"]?.sumOf { it.amount } ?: 0

                        val totalBudget = when (frequency) {
                            "daily" -> totalDaily
                            "weekly" -> totalWeekly
                            "monthly" -> totalMonthly
                            else -> 0
                        }

                        Log.d("Budget Totals", "Daily: $totalDaily, Weekly: $totalWeekly, Monthly: $totalMonthly")

                        retrofit.get_Expenses_Analysis(token.toString())
                            .enqueue(object : Callback<ExpenseListResponse> {
                                override fun onResponse(call: Call<ExpenseListResponse>, response: Response<ExpenseListResponse>) {
                                    if (response.isSuccessful) {
                                        val expenses = response.body()?.expenses ?: emptyList()
                                        val currentMonthExpenses = expenses.filter {
                                            it.date.startsWith(targetMonthYear)
                                        }
                                        val totalExpenses = currentMonthExpenses.sumOf { it.amount }
                                        val balance = totalBudget - totalExpenses
                                        findViewById<TextView>(R.id.budget_tv).text = "$totalBudget"
                                        findViewById<TextView>(R.id.expense_tv).text = "$totalExpenses"
                                        findViewById<TextView>(R.id.balance_tv).text = "$balance"
                                        // Assuming you're in the selected month/year context
                                        val calendar = Calendar.getInstance()
                                        calendar.time = selectedDate

                                        val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

// Example: Show budgets for last 5 months
                                        val chartData = mutableListOf<Pair<String, Float>>()

                                        for (i in 4 downTo 0) {
                                            val tempCal = calendar.clone() as Calendar
                                            tempCal.add(Calendar.MONTH, -i)

                                            val month = tempCal.get(Calendar.MONTH)
                                            val year = tempCal.get(Calendar.YEAR)
                                            val monthYear = String.format("%04d-%02d", year, month + 1)

                                            val budgetForMonth = allBudgets
                                                .filter { it.createdAt.startsWith(monthYear) && it.frequency == frequency }
                                                .sumOf { it.amount }

                                            chartData.add(monthNames[month] to budgetForMonth.toFloat())
                                        }

// Now add actual spent (for selected month)
                                        chartData.add("Spent" to totalExpenses.toFloat())

                                        setupSampleChart(chartData)


                                    } else {
                                        Toast.makeText(this@Analysis, "Failed to load expenses", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onFailure(call: Call<ExpenseListResponse>, t: Throwable) {
                                    Log.d("Expense Error", t.localizedMessage ?: "Unknown error")
                                    Toast.makeText(this@Analysis, "Expense Error: ${t.message}", Toast.LENGTH_SHORT).show()
                                }
                            })
                    } else {
                        Toast.makeText(this@Analysis, "No budget data", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@Analysis, "Budget fetch failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Get_Budget>, t: Throwable) {
                Log.d("Budget Error", t.localizedMessage ?: "Unknown error")
                Toast.makeText(this@Analysis, "Budget Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateMonthYearDisplay() {
        val format = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvMonthYear.text = format.format(selectedCalendar.time)
    }

    fun parseDateMonthYear(dateString: String): Pair<Int, Int> {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)
        val calendar = Calendar.getInstance()
        calendar.time = date!!
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        return Pair(month, year)
    }

}
