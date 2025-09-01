package com.example.expenso

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expenso.ApiUsage.API
import com.example.expenso.ApiUsage.Constant
import com.example.expenso.ApiUsage.obj
import com.example.expenso.Data_Class.TransactionBudget
import com.example.expenso.view.Expense
import com.example.expenso.view.TransactionExpense
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Transaction : AppCompatActivity() {
    lateinit var budgetamount: TextView
    lateinit var expenseamount: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    lateinit var sett: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)
        val transaction = findViewById<TextView>(R.id.transaction)
        val greet = findViewById<TextView>(R.id.greeting_transaction)
        val name = findViewById<TextView>(R.id.name_transaction)
        sett = findViewById(R.id.setting_trans)
         budgetamount= findViewById(R.id.transaction_budget_textview)
        expenseamount = findViewById(R.id.transaction_expense_textview)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_nav_Analysis)
        bottomNavigationView.selectedItemId = R.id.nav_home

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home->{
                    startActivity(Intent(this, Transaction::class.java))
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

        transaction.setOnClickListener {
            startActivity(Intent(this@Transaction, Home::class.java))
        }
        sett.setOnClickListener {
            startActivity(Intent(this@Transaction, Settings::class.java))
        }

        greet.text=getGreeting()
        name.text= Constant.getSavedUserName(this)
        val token=Constant.getSavedToken(this)
        setupbudget(token.toString())
        val monthBtn = findViewById<MaterialButton>(R.id.this_month_transaction)
        val now = Calendar.getInstance()
        fetchAndDisplayData(now.get(Calendar.MONTH), now.get(Calendar.YEAR))
        monthBtn.setOnClickListener {
            showMonthPicker { selectedMonth, selectedYear ->
                fetchAndDisplayData(selectedMonth, selectedYear)
            }
        }
        recyclerView = findViewById(R.id.transaction_recycler_view)
        adapter = TransactionAdapter(emptyList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

    }
    fun updateRecyclerView(expenses: List<Expense>) {
        adapter = TransactionAdapter(expenses)
        recyclerView.adapter = adapter
    }

    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        return when (hour) {
            in 0..11 -> "Good Morning"
            in 12..17 -> "Good Afternoon"
            in 18..20 -> "Good Evening"
            else -> "Good Night"
        }
    }
    fun setupbudget(token:String){
        val retrofit = obj.createService(API::class.java)
        retrofit.get_budget_transaction(token).enqueue(object :Callback<TransactionBudget>{
            override fun onResponse(
                call: Call<TransactionBudget?>,
                response: Response<TransactionBudget?>
            ) {
                if(response.isSuccessful){
                    budgetamount.text= response.body()?.totalBudget.toString()
                }else{
                    Toast.makeText(this@Transaction,"Error from server",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(
                call: Call<TransactionBudget?>,
                t: Throwable
            ) {
                budgetamount.text = "Error"
                Toast.makeText(this@Transaction,t.localizedMessage,Toast.LENGTH_SHORT).show()
            }
        })
    }
    fun showMonthPicker(onMonthSelected: (Int, Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, _ ->
                onMonthSelected(selectedMonth, selectedYear)
            },
            year, month, 1
        )
        datePickerDialog.datePicker.findViewById<View>(
            resources.getIdentifier("day", "id", "android")
        )?.visibility = View.GONE
        datePickerDialog.show()
    }
    fun fetchAndDisplayData(month: Int, year: Int) {
        val token = Constant.getSavedToken(this).toString()
        val retrofit = obj.createService(API::class.java)

        retrofit.get_Expenses_transaction(token).enqueue(object : Callback<TransactionExpense> {
            override fun onResponse(call: Call<TransactionExpense>, response: Response<TransactionExpense>) {
                if (response.isSuccessful && response.body() != null) {
                    val allExpenses = response.body()!!.expenses

                    val filteredExpenses = filterLastFourMonthsExpenses(allExpenses, month, year)

                    if (filteredExpenses.isEmpty()) {
                        Toast.makeText(
                            this@Transaction,
                            "No transactions for selected month. Showing current month instead.",
                            Toast.LENGTH_SHORT
                        ).show()

                        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
                        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                        val currentMonthExpenses = filterLastFourMonthsExpenses(allExpenses, currentMonth, currentYear)

                        // Update RecyclerView and BarChart with current month data
                        val sortedCurrentExpenses = currentMonthExpenses.sortedByDescending { expense ->
                            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                            sdf.parse(expense.date) ?: Date(0)
                        }
                        updateRecyclerView(sortedCurrentExpenses)
                        updateBarChart(sortedCurrentExpenses)
                        expenseamount.text = sortedCurrentExpenses.sumOf { it.amount }.toString()


                    } else {
                        // Update RecyclerView and BarChart with filtered data
                        val sortedExpenses = filteredExpenses.sortedByDescending { expense ->
                            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                            sdf.parse(expense.date) ?: Date(0)
                        }
                        updateRecyclerView(sortedExpenses)
                        updateBarChart(sortedExpenses)
                        expenseamount.text = sortedExpenses.sumOf { it.amount }.toString()

                    }
                }
            }

            override fun onFailure(call: Call<TransactionExpense>, t: Throwable) {
                Toast.makeText(this@Transaction, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun updateBarChart(expenses: List<Expense>) {
        if (expenses.isEmpty()) return

        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val fullDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Group and sum expenses by year-month
        val grouped = expenses.groupBy {
            val cal = Calendar.getInstance()
            cal.time = fullDateFormat.parse(it.date)!!
            "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}"
        }.mapValues { entry ->
            entry.value.sumOf { it.amount }
        }

        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        grouped.entries.sortedBy { it.key }.forEachIndexed { index, entry ->
            val cal = Calendar.getInstance()
            val parts = entry.key.split("-")
            cal.set(Calendar.YEAR, parts[0].toInt())
            cal.set(Calendar.MONTH, parts[1].toInt())
            labels.add(monthFormat.format(cal.time)) // e.g., "Jan"
            entries.add(BarEntry(index.toFloat(), entry.value.toFloat()))
        }

        val dataSet = BarDataSet(entries, "Expenses")
        dataSet.setColors(Color.parseColor("#4CD7D0"))
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.BLACK
        dataSet.setDrawValues(true)

        val data = BarData(dataSet)
        data.barWidth = 0.4f

        val barChart = findViewById<BarChart>(R.id.barChart1)
        barChart.data = data

        barChart.setVisibleXRangeMaximum(4f)

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.labelCount = labels.size
        xAxis.textColor = Color.GRAY

        barChart.axisRight.isEnabled = false
        val leftAxis = barChart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.granularity = 1f
        leftAxis.setDrawGridLines(true)
        leftAxis.textColor = Color.GRAY

        barChart.legend.isEnabled = false
        barChart.description.isEnabled = false
        barChart.setTouchEnabled(false)
        barChart.setScaleEnabled(false)
        barChart.animateY(1000)
        barChart.invalidate()
    }
    fun filterLastFourMonthsExpenses(expenses: List<Expense>, endMonth: Int, endYear: Int): List<Expense> {
        val result = mutableListOf<Expense>()
        val endCal = Calendar.getInstance()
        endCal.set(Calendar.MONTH, endMonth)
        endCal.set(Calendar.YEAR, endYear)

        val startCal = endCal.clone() as Calendar
        startCal.add(Calendar.MONTH, -3) // Go back 3 months

        for (expense in expenses) {
            val expCal = Calendar.getInstance()
            expCal.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(expense.date)!!

            if (!expCal.before(startCal) && !expCal.after(endCal)) {
                result.add(expense)
            }
        }
        return result
    }








}