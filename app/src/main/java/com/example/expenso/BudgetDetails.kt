package com.example.expenso

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import java.util.Calendar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.example.expenso.ApiUsage.API
import com.example.expenso.ApiUsage.Constant
import com.example.expenso.ApiUsage.Constant.getSavedToken
import com.example.expenso.ApiUsage.Constant.saveCategories
import com.example.expenso.ApiUsage.obj
import com.example.expenso.Data_Class.CategoryResponse
import com.example.expenso.Data_Class.Fetch_CategoryId
import com.example.expenso.Data_Class.Get_Budget
import com.example.expenso.Data_Class.Send_AddBudget
import com.example.expenso.Data_Class.SpinnerDataClass
import com.example.expenso.Data_Class.UserCategoryResponse
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.expenso.Data_Class.Budget
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.bottomnavigation.BottomNavigationView


class BudgetDetails : AppCompatActivity() {
    lateinit var selectMonth:Button
    lateinit var selectFrequency: Button
    lateinit var budget_recycler_view: RecyclerView
    lateinit var pieChart: com.github.mikephil.charting.charts.PieChart
    lateinit var add:Button
    var currentData=""
    var frequency = "daily"
    lateinit var  adapter: BudgetDetailAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_details)
        selectMonth = findViewById(R.id.btn_select_month_det)
        selectFrequency = findViewById(R.id.btn_select_freq)
        budget_recycler_view = findViewById(R.id.budget_recycler_view)
        add = findViewById(R.id.budget_details_button)
        pieChart = findViewById(R.id.pieChart2)
        selectMonth.text = "Select Month"

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_nav_re_)
        bottomNavigationView.selectedItemId = R.id.nav_report
        selectMonthandFrequency()
        showCurrentData()
        add.setOnClickListener {
            addBudget()
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, Transaction::class.java))
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

                R.id.nav_report -> {
                    startActivity(Intent(this, Expenses::class.java))
                    item.icon?.setTint(Color.parseColor("#0000FF"))
                    return@setOnItemSelectedListener true
                }

                else -> false
            }

        }
    }


    private fun showUpdateBudgetDialog(budget: Budget) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.update_budget_popup, null)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogView)

        val categorySpinner = dialogView.findViewById<Spinner>(R.id.category_spinner_update)
        val amountInput = dialogView.findViewById<EditText>(R.id.amountEditText_update)
        val frequencyInput = dialogView.findViewById<EditText>(R.id.budgetFrequencyEditText_update)
        val cancelBtn = dialogView.findViewById<MaterialButton>(R.id.cancel_update)
        val saveBtn = dialogView.findViewById<MaterialButton>(R.id.save_update)

        val predefinedCategories = Constant.getCategories(this, "predefined_categories")
        val userDefinedCategories = Constant.getCategories(this, "user_defined_categories")
        val allCategories = (predefinedCategories + userDefinedCategories).map {
            SpinnerDataClass(it._id, it.name, it.icon ?: "")
        }

        val spinnerAdapter = Add_Budget_Spinner_Adaptor(this, allCategories)
        categorySpinner.adapter = spinnerAdapter

        // Prefill data
        amountInput.setText(budget.amount.toString())
        frequencyInput.setText(budget.frequency.replaceFirstChar { it.uppercase() })

        // Prefill data
        amountInput.setText(budget.amount.toString())
        frequencyInput.setText(budget.frequency.replaceFirstChar { it.uppercase() })

        val selectedIndex = budget.categoryId?.let { category ->
            allCategories.indexOfFirst { it.name == category.name }
        } ?: -1

        if (selectedIndex != -1) categorySpinner.setSelection(selectedIndex)


        // Frequency picker
        val frequencyOptions = arrayOf("Daily", "Weekly", "Monthly")
        frequencyInput.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Select Frequency")
                .setItems(frequencyOptions) { _, which ->
                    frequencyInput.setText(frequencyOptions[which])
                }
                .show()
        }

        cancelBtn.setOnClickListener {
            dialog.dismiss()
            showDeleteConfirmationDialog(budget._id)
        }

        saveBtn.setOnClickListener {
            val newCategory = (categorySpinner.selectedItem as SpinnerDataClass).id
            val newAmount = amountInput.text.toString().trim()
            val newFrequency = frequencyInput.text.toString().trim().lowercase()

            if (newAmount.isEmpty() || newFrequency.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val retrofit = obj.createService(API::class.java)
            val token = Constant.getSavedToken(this)

            retrofit.update_budget(token.toString(), budget._id, newCategory, newAmount, newFrequency)
                .enqueue(object : Callback<Send_AddBudget> {
                    override fun onResponse(call: Call<Send_AddBudget>, response: Response<Send_AddBudget>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@BudgetDetails, "Budget updated", Toast.LENGTH_SHORT).show()
                            showCurrentData()
                            dialog.dismiss()
                        } else {
                            Toast.makeText(this@BudgetDetails, "Failed to update", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Send_AddBudget>, t: Throwable) {
                        Toast.makeText(this@BudgetDetails, "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        dialog.show()
    }
    private fun showDeleteConfirmationDialog(budgetId: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.delete_budget_popup, null)
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setView(dialogView)
        alertDialog.setCancelable(false) // Prevent dismissing on outside touch

        val deleteButton = dialogView.findViewById<MaterialButton>(R.id.delete_budget)
        val cancelButton = dialogView.findViewById<MaterialButton>(R.id.Update_budget) // Fixed wrong ID here

        deleteButton.setOnClickListener {
            val retrofit = obj.createService(API::class.java)
            val token = Constant.getSavedToken(this)

            retrofit.delete_budget(token.toString(), budgetId)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@BudgetDetails, "Budget deleted", Toast.LENGTH_SHORT).show()
                            alertDialog.dismiss()
                           showCurrentData()
                        } else {
                            Toast.makeText(this@BudgetDetails, "Failed to delete", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@BudgetDetails, "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }


    fun addBudget(){
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_add_budget, null)

        val categoryInput = view.findViewById<Spinner>(R.id.category_spinner_)
        val amountInput = view.findViewById<EditText>(R.id.amountEditText)
        val frequencyInput = view.findViewById<EditText>(R.id.budgetFrequencyEditText)
        val cancelBtn = view.findViewById<MaterialButton>(R.id.cancel)
        val saveBtn = view.findViewById<MaterialButton>(R.id.add_budget)
         fetchAndSaveUserCategories(this@BudgetDetails)
        fetchAndSaveDefaultCategories(this@BudgetDetails)
        val predefinedCategories = Constant.getCategories(this, "predefined_categories")
        val userDefinedCategories = Constant.getCategories(this, "user_defined_categories")

        val allCategories: List<SpinnerDataClass> = (predefinedCategories + userDefinedCategories).map {
            SpinnerDataClass(
                id = it._id,
                name = it.name,
                imageUrl = it.icon ?: ""
            )
        }
        val spinnerAdapter = Add_Budget_Spinner_Adaptor(this, allCategories)
        categoryInput.adapter = spinnerAdapter
        val frequencyOptions = arrayOf("Daily", "Weekly", "Monthly")
        frequencyInput.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Select Frequency")
                .setItems(frequencyOptions) { _, which ->
                    frequencyInput.setText(frequencyOptions[which])
                }
                .show()
        }

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        saveBtn.setOnClickListener {
            val categoryName = (categoryInput.selectedItem as SpinnerDataClass).name
            val amount = amountInput.text.toString().trim()
            val frequency = frequencyInput.text.toString().trim().lowercase()

            if (categoryName.isEmpty() || amount.isEmpty() || frequency.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val predefinedCategories = Constant.getCategories(this, "predefined_categories")
            val userCategories = Constant.getCategories(this, "user_defined_categories")
            val allCategories = predefinedCategories + userCategories

            val categoryId = allCategories.find { it.name.equals(categoryName, ignoreCase = true) }?._id

            if (categoryId == null) {
                Toast.makeText(this, "No category found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val retrofit = obj.createService(API::class.java)
            val token = Constant.getSavedToken(this)

            retrofit.add_budget(token.toString(), categoryId, amount, frequency)
                .enqueue(object : Callback<Send_AddBudget> {
                    override fun onResponse(call: Call<Send_AddBudget>, response: Response<Send_AddBudget>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@BudgetDetails, "Budget Added Successfully", Toast.LENGTH_SHORT).show()
                            showCurrentData() // Refresh data after adding
                        } else {
                            Log.e("BudgetAdd", "Error: ${response.code()} - ${response.message()}")
                            response.errorBody()?.let { Log.e("BudgetAdd", "Body: ${it.string()}") }
                            Toast.makeText(this@BudgetDetails, "Server error", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Send_AddBudget>, t: Throwable) {
                        Toast.makeText(this@BudgetDetails, t.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                })

            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }




    fun showCurrentData() {
        val token = Constant.getSavedToken(this)
        val retrofit = obj.createService(API::class.java)

        retrofit.get_budget(token.toString()).enqueue(object : Callback<Get_Budget> {
            override fun onResponse(call: Call<Get_Budget?>, response: Response<Get_Budget?>) {
                if (response.isSuccessful && response.body() != null) {
                    val budgetResponse = response.body()!!
                    val allBudgets = budgetResponse.budgets

                    val monthNumber = when (currentData) {
                        "January" -> "01"
                        "February" -> "02"
                        "March" -> "03"
                        "April" -> "04"
                        "May" -> "05"
                        "June" -> "06"
                        "July" -> "07"
                        "August" -> "08"
                        "September" -> "09"
                        "October" -> "10"
                        "November" -> "11"
                        "December" -> "12"
                        else -> ""
                    }

                    // Filter by selected month and frequency
                    val filteredBudgets = allBudgets.filter {
                        val createdMonth = it.createdAt.substring(5, 7)
                        val matchesMonth = createdMonth == monthNumber
                        val matchesFrequency = it.frequency.equals(frequency, ignoreCase = true)
                        matchesMonth && matchesFrequency
                    }

                    Log.d("FilteredBudgets", filteredBudgets.toString())

                    // Adapter setup
                    adapter = BudgetDetailAdapter(filteredBudgets, currentData, frequency) { clickedBudget ->
                        showUpdateBudgetDialog(clickedBudget)
                    }

                    budget_recycler_view.layoutManager = GridLayoutManager(this@BudgetDetails, 3)
                    budget_recycler_view.adapter = adapter
                    adapter.notifyDataSetChanged()

                    // UI References (TextViews and PieChart)
                    val budgetSumText = findViewById<TextView>(R.id.budgetsum)
                    val expenseDetailText = findViewById<TextView>(R.id.expense_detail)
                    val remainingText = findViewById<TextView>(R.id.remaining)
                    val pieChart = findViewById<com.github.mikephil.charting.charts.PieChart>(R.id.pieChart2)

                    // Calculations
                    val totalAmount = filteredBudgets.sumOf { it.amount }
                    budgetSumText.text = totalAmount.toString()

                    val temp_budget = totalAmount.toDouble()
                    val temp_expense = Constant.getTotalExpenseAmount(this@BudgetDetails)
                    val remainingAmount = temp_budget - temp_expense

                    Log.d("Temp_budget", temp_budget.toString())
                    Log.d("Temp_expense", temp_expense.toString())
                    Log.d("Remaining", remainingAmount.toString())

                    expenseDetailText.text = temp_expense.toString()
                    remainingText.text = remainingAmount.toString()

                    // PieChart Setup
                    val entries = listOf(
                        PieEntry(totalAmount.toFloat(), "Total Budget")
                    )

                    val dataSet = PieDataSet(entries, "")
                    dataSet.colors = listOf(Color.parseColor("#0074C7"))
                    dataSet.setDrawValues(false)

                    val pieData = PieData(dataSet)
                    pieChart.data = pieData

                    pieChart.description.isEnabled = false
                    pieChart.setDrawEntryLabels(false)
                    pieChart.setEntryLabelColor(Color.WHITE)
                    pieChart.setHoleColor(Color.TRANSPARENT)
                    pieChart.centerText = "Total Budget\n$totalAmount"
                    pieChart.setCenterTextSize(7f)
                    pieChart.setCenterTextColor(Color.BLACK)
                    pieChart.legend.isEnabled = false
                    pieChart.animateY(1000)
                    pieChart.invalidate()
                }
            }

            override fun onFailure(call: Call<Get_Budget?>, t: Throwable) {
                Toast.makeText(this@BudgetDetails, "Failed to fetch budget", Toast.LENGTH_SHORT).show()
                Log.e("BudgetFetchError", t.localizedMessage ?: "Unknown error")
            }
        })
    }

    fun selectMonthandFrequency(){
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)+1
        selectMonth.setOnClickListener {
            val months = listOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )
            val builder = AlertDialog.Builder(this)
            builder.setItems(months.toTypedArray()) {_,which->
                currentData = months[which]
                selectMonth.text=currentData
                showCurrentData()
            }
            builder.show()
        }
        if (currentData.isEmpty()) {
            val months = listOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )
            val currentMonthIndex = calendar.get(Calendar.MONTH) // 0-based
            currentData = months[currentMonthIndex]
            selectMonth.text=currentData
        }

        selectFrequency.setOnClickListener {
            val freq = listOf("daily","weekly","monthly")
            val checkfreq = AlertDialog.Builder(this)
            checkfreq.setItems(freq.toTypedArray()) {_,which->
                frequency = freq[which]
                selectFrequency.text=frequency
                showCurrentData()
            }
            checkfreq.show()
        }

    }
    fun fetchAndSaveUserCategories(context: Context) {
        val retrofit = obj.createService(API::class.java)
        val token = getSavedToken(context) ?: return

        retrofit.get_user_categories(token).enqueue(object : Callback<UserCategoryResponse> {
            override fun onResponse(call: Call<UserCategoryResponse>, response: Response<UserCategoryResponse>) {
                if (response.isSuccessful) {
                    val userCategories = response.body()?.categories ?: emptyList()
                    val categoryIdList = userCategories.map {
                        Fetch_CategoryId(it._id, it.name, it.icon ?: "")
                    }
                    saveCategories(context, categoryIdList, "user_defined_categories")
                }
            }

            override fun onFailure(call: Call<UserCategoryResponse>, t: Throwable) {
                // Optional: Log or Toast error if needed
            }
        })
    }

    fun fetchAndSaveDefaultCategories(context: Context) {
        val retrofit = obj.createService(API::class.java)

        retrofit.get_default_categories().enqueue(object : Callback<CategoryResponse> {
            override fun onResponse(call: Call<CategoryResponse>, response: Response<CategoryResponse>) {
                if (response.isSuccessful) {
                    val categoryList = response.body()?.category ?: emptyList()
                    val baseUrl = "https://expensio-nkvc.onrender.com/"
                    val categoryIdList = categoryList.map {
                        Fetch_CategoryId(
                            _id = it._id,
                            name = it.name,
                            icon = baseUrl + (it.icon?.replace("\\", "/") ?: "")
                        )
                    }
                    saveCategories(context, categoryIdList, "predefined_categories")
                }
            }

            override fun onFailure(call: Call<CategoryResponse>, t: Throwable) {
                // Optional: Log or Toast error if needed
            }
        })
    }

}
