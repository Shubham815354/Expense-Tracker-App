package com.example.expenso

import android.R.attr.data
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.expenso.ApiUsage.API
import com.example.expenso.ApiUsage.Constant
import com.example.expenso.ApiUsage.obj
import com.example.expenso.Data_Class.Get_Budget
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import retrofit2.Call
import androidx.appcompat.app.AlertDialog
import com.example.expenso.Data_Class.Send_AddBudget
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class Budget : AppCompatActivity() {
    private lateinit var pieChart1: PieChart
    private lateinit var budget_details_button: Button
    private lateinit var this_month:Button
    private lateinit var totalbudget: TextView
    lateinit var exp:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)
        pieChart1 = findViewById(R.id.pieChart1)
        budget_details_button=findViewById(R.id.budget_details_button_)
        this_month= findViewById(R.id.this_month)
        totalbudget = findViewById(R.id.totalbudget)
        exp = findViewById(R.id.btnExpense)
        val expenseSum = findViewById<TextView>(R.id.expene_tv)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_nav_report)
        bottomNavigationView.selectedItemId = R.id.nav_report

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
                    item.icon?.setTint(Color.parseColor("#0000FF"))
                    return@setOnItemSelectedListener true
                }

                else -> false
            }
        }

        exp.setOnClickListener {
            startActivity(Intent(this@Budget, Expenses::class.java))
        }

        val temp_expense= Constant.getTotalExpenseAmount(this)
        expenseSum.text = "Expense : "+temp_expense.toString()
        budget_details_button.setOnClickListener {
            startActivity(Intent(this@Budget, BudgetDetails::class.java))
        }
        val token= Constant.getSavedToken(this)
        this_month.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select a date")
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = selection
                val formattedDate = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
                this_month.text  = formattedDate
                setupBarChart(token.toString(),calendar)
            }

            datePicker.show(supportFragmentManager, "DATE_PICKER")
        }
        setupBarChart(token.toString())



    }

    private fun setupBarChart(token: String, selectedCalendar: Calendar? = null) {
        val retrofit = obj.createService(API::class.java)
        retrofit.get_budget(token).enqueue(object : Callback<Get_Budget> {
            override fun onResponse(call: Call<Get_Budget?>, response: Response<Get_Budget?>) {
                if (response.isSuccessful) {
                    val allBudget = response.body()?.budgets ?: emptyList()

                    val calendar = selectedCalendar ?: Calendar.getInstance()
                    val selectedMonth = calendar.get(Calendar.MONTH)
                    val selectedYear = calendar.get(Calendar.YEAR)

                    // Filter by selected month and year
                    val filteredBudget = allBudget.filter {
                        try {
                            val dateParts = it.createdAt.split("-")
                            val year = dateParts[0].toInt()
                            val month = dateParts[1].toInt() - 1
                            year == selectedYear && month == selectedMonth
                        } catch (e: Exception) {
                            false
                        }
                    }

                    // Sum all amounts
                    val totalAmount = filteredBudget.sumOf { it.amount }
                    totalbudget.text=totalAmount.toString()

                    // Create a single pie slice
                    val entries = listOf(
                        PieEntry(totalAmount.toFloat(), "Total Budget")
                    )

                    val dataSet = PieDataSet(entries, "")
                    dataSet.colors = listOf(Color.parseColor("#0074C7"))
                    dataSet.setDrawValues(false) // hides numeric values on slices

                    val pieData = PieData(dataSet)
                    pieChart1.data = pieData

                    pieChart1.description.isEnabled = false
                    pieChart1.setDrawEntryLabels(false) // <-- add this line to hide labels inside slices
                    pieChart1.setEntryLabelColor(Color.WHITE) // optional, ignored now
                    pieChart1.setHoleColor(Color.TRANSPARENT)
                    pieChart1.centerText = "Total Budget\n$totalAmount"
                    pieChart1.setCenterTextSize(18f)
                    pieChart1.setCenterTextColor(Color.BLACK)
                    pieChart1.legend.isEnabled = false
                    pieChart1.animateY(1000)

                    pieChart1.invalidate()

                }
            }

            override fun onFailure(call: Call<Get_Budget?>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }




}