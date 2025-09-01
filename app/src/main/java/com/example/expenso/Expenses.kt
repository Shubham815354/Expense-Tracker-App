package com.example.expenso

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expenso.ApiUsage.API
import com.example.expenso.ApiUsage.Constant
import com.example.expenso.ApiUsage.obj
import com.example.expenso.Budget_Categories
import com.example.expenso.Data_Class.Add_expense_response
import com.example.expenso.Data_Class.Budget
import com.example.expenso.Data_Class.Category
import com.example.expenso.Data_Class.Expense
import com.example.expenso.Data_Class.ExpenseListResponse
import com.example.expenso.Data_Class.ExpenseResponse
import com.example.expenso.Data_Class.Fetch_CategoryId
import com.example.expenso.Data_Class.Get_Budget
import com.example.expenso.Data_Class.SpinnerDataClass
import com.example.expenso.Data_Class.Update_Expense
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date
import java.util.TimeZone

class Expenses : AppCompatActivity() {
    private lateinit var pieChart: PieChart
    private lateinit var recyclerView: RecyclerView
    private lateinit var expensesAdapter: ExpensesAdapter
    private lateinit var Add_Expense: Button
    private lateinit var btn_select_month: Button
    private lateinit var btnBudget: Button
    private var currentBudget: Double =1.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenses)
        pieChart = findViewById(R.id.pieChart)
        recyclerView = findViewById(R.id.recyclerViewExpenses)
        btn_select_month = findViewById(R.id.btn_select_month)
        Add_Expense = findViewById(R.id.Add_Expense)
        btnBudget = findViewById(R.id.btnBudget)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navExp)
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




        val token = Constant.getSavedToken(this)
        BudgetRepository.getBudget(token.toString()) { budget ->
            if (budget == null) {
                Toast.makeText(this@Expenses, "Failed to fetch budget", Toast.LENGTH_SHORT).show()
                return@getBudget
            }else{
                currentBudget = budget.toDouble()
            }
        }

        btnBudget.setOnClickListener {
            startActivity(Intent(this@Expenses, com.example.expenso.Budget::class.java))
        }

        btn_select_month.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select a date")
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = selection
                val formattedDate = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
                btn_select_month.text  = formattedDate
                setupBarChart(calendar)
            }

            datePicker.show(supportFragmentManager, "DATE_PICKER")
        }

        // Ensure showAddExpensePopup is correctly defined
        Add_Expense.setOnClickListener {
            showAddExpensePopup()
        }

        setupBarChart()
    }

    // Make sure showAddExpensePopup is defined here
    private fun showAddExpensePopup() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.add_expense_popup, null)

        val expenseNameInput = view.findViewById<EditText>(R.id.expense_name)
        val amountInput = view.findViewById<EditText>(R.id.amount)
        val categorySpinner = view.findViewById<Spinner>(R.id.category_spinner)
        val dateInput = view.findViewById<EditText>(R.id.date_input)
        val cancelBtn = view.findViewById<MaterialButton>(R.id.cancel)
        val saveBtn = view.findViewById<MaterialButton>(R.id.save)

        // Load categories from shared prefs (predefined + user defined)
        // After getting predefined and userDefined categories
        val predefinedCategories = Constant.getCategories(this, "predefined_categories")
        val userDefinedCategories = Constant.getCategories(this, "user_defined_categories")

        val allCategories: List<SpinnerDataClass> = (predefinedCategories + userDefinedCategories).map {
            SpinnerDataClass(
                id = it._id,
                name = it.name,
                imageUrl = it.icon ?: ""  // Use icon or empty string if null
            )
        }


        val customAdapter = CustomSpinnerAdapter(this, allCategories)
        categorySpinner.adapter = customAdapter


        // Date picker setup
        dateInput.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Expense Date")
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Date(selection))
                dateInput.setText(selectedDate)
            }

            datePicker.show(supportFragmentManager, "DATE_PICKER_EXPENSE")
        }

        cancelBtn.setOnClickListener { dialog.dismiss() }

        saveBtn.setOnClickListener {
            val name = expenseNameInput.text.toString().trim()
            val amount = amountInput.text.toString().trim()
            val date = dateInput.text.toString().trim()
            val selectedCategoryName = (categorySpinner.selectedItem as SpinnerDataClass).name

            if (name.isEmpty() || amount.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val categoryId = allCategories.find { it.name.equals(selectedCategoryName, ignoreCase = true) }?.id

            if (categoryId == null) {
                Toast.makeText(this, "No category found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val retrofit = obj.createService(API::class.java)
            val token = Constant.getSavedToken(this)

            if (token.isNullOrEmpty()) {
                Toast.makeText(this, "User token missing, please login again", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            retrofit.add_expense(token, amount, categoryId, date, name)
                .enqueue(object : Callback<Add_expense_response> {
                    override fun onResponse(
                        call: Call<Add_expense_response?>,
                        response: Response<Add_expense_response?>
                    ) {
                        if (response.isSuccessful) {
                            setupBarChart()
                            Toast.makeText(this@Expenses, "Expense Added Successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("ExpenseAdd", "Error: ${response.code()} - ${response.message()}")
                            response.errorBody()?.let {
                                Log.e("ExpenseAdd", "Error Body: ${it.string()}")
                            }
                            Toast.makeText(this@Expenses, "Error from server", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Add_expense_response?>, t: Throwable) {
                        Toast.makeText(this@Expenses, t.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                })

            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun setupBarChart(selectedCalendar: Calendar? = null) {
        val token = Constant.getSavedToken(this)
        val retrofit = obj.createService(API::class.java)

        retrofit.getExpenses(token.toString()).enqueue(object : Callback<ExpenseListResponse> {
            override fun onResponse(
                call: Call<ExpenseListResponse>,
                response: Response<ExpenseListResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val allExpenses = response.body()!!.expenses

                    Log.d("AllExpenses", allExpenses.toString())

                    // Determine selected or current month and year
                    val calendar = selectedCalendar ?: Calendar.getInstance()
                    val selectedMonth = calendar.get(Calendar.MONTH)
                    val selectedYear = calendar.get(Calendar.YEAR)

                    // Filter expenses matching the selected month and year
                    val filteredExpenses = allExpenses.filter {
                        Log.d("DateFormatDebug", "Raw date: ${it.date}")
                        try {
                            val dateParts = it.date.split("-")
                            val year = dateParts[0].toInt()
                            val month = dateParts[1].toInt() - 1 // Calendar.MONTH is 0-based
                            year == selectedYear && month == selectedMonth
                        } catch (e: Exception) {
                            Log.e("DateParseError", "Failed to parse date: ${it.date}", e)
                            false
                        }
                    }

                    // Log filtered expenses for debugging
                    Log.d("FilteredExpenses", filteredExpenses.toString())

                    // Check if no expenses were found
                    if (filteredExpenses.isEmpty()) {
                        Toast.makeText(this@Expenses, "No expenses for the selected month", Toast.LENGTH_SHORT).show()
                    }

                    // Continue with your code to populate the chart and expenses RecyclerView
                    // Calculate total amount
                    val totalAmount = filteredExpenses.sumOf {
                        it.amount.toString().toDoubleOrNull() ?: 0.0
                    }
                    Constant.saveTotalExpenseAmount(this@Expenses,totalAmount)
                    Log.d("Total Amount =",totalAmount.toString())

                    val tvCenterValue: TextView = findViewById(R.id.tvCenterValue)
                    tvCenterValue.text = "$%.2f".format(totalAmount)

                    val pieEntries = ArrayList<PieEntry>()
                    val colorList = ArrayList<Int>()
                    val expenses = ArrayList<Expense>()

                    for (expense in filteredExpenses) {
                        val amount = expense.amount.toString().toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            val categoryName = expense.categoryId?.name ?: "Deleted Category"
                            val rawColor = categoryName.hashCode() and 0xFFFFFF
                            var colorHex = String.format("#%06X", rawColor)

                            if (categoryName == "Deleted Category") {
                                colorHex = "#A9A9A9" // Default gray
                            }

                            try {
                                var color = Color.parseColor(colorHex)
                                val red = Color.red(color)
                                val green = Color.green(color)
                                val blue = Color.blue(color)

                                if (red + green + blue < 100) {
                                    color = Color.rgb(
                                        (red + 100).coerceAtMost(255),
                                        (green + 100).coerceAtMost(255),
                                        (blue + 100).coerceAtMost(255)
                                    )
                                    colorHex = String.format("#%06X", 0xFFFFFF and color)
                                }

                                colorList.add(color)
                            } catch (e: IllegalArgumentException) {
                                colorList.add(Color.GRAY)
                                colorHex = "#808080"
                            }

                            pieEntries.add(PieEntry(amount.toFloat(), categoryName))
                            val amountDouble = expense.amount.toInt()
                            val percentage = if (currentBudget != 0.0) {
                                ((amountDouble * 100) / currentBudget).toInt()
                            } else {
                                0
                            }

                            val expenseObj = Expense(
                                id = expense._id,
                                name = expense.categoryId?.name ?: "Deleted Category",
                                amount = "$${expense.amount}",
                                color = colorHex,
                                percentage = "$percentage%"
                                ,//percentage = "${(amount * 100 / totalAmount).toInt()}%"
                                date = expense.date,
                                category = expense.categoryId ?: Category("deleted", "Deleted Category", ""),
                                note = expense.note
                            )
                            Log.d("Total aaamount = ",amount.toString())
                            Log.d("TempBudget = ",currentBudget.toString())




                            expenses.add(expenseObj)
                        }

                    }

                    val dataSet = PieDataSet(pieEntries, "").apply {
                        colors = colorList
                        sliceSpace = 0f
                        selectionShift = 0f
                    }

                    val pieData = PieData(dataSet).apply {
                        setDrawValues(false)
                    }

                    pieChart.apply {
                        data = pieData
                        setDrawEntryLabels(false)
                        description.isEnabled = false
                        isRotationEnabled = true
                        setUsePercentValues(false)
                        setDrawCenterText(false)
                        holeRadius = 70f
                        transparentCircleRadius = 74f
                        pieChart.animateY(1000)
                        legend.isEnabled = false
                        invalidate()
                    }

                    expensesAdapter = ExpensesAdapter(expenses) { expense ->
                        showCenterDialog(expense)  // Trigger dialog from activity
                    }
                    recyclerView.layoutManager = LinearLayoutManager(this@Expenses)
                    recyclerView.adapter = expensesAdapter
                    expensesAdapter.notifyDataSetChanged()  // Notify adapter of the changes
                }
            }

            override fun onFailure(call: Call<ExpenseListResponse>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }






    private fun showCenterDialog(expense: Expense) {
        val mainDialog = Dialog(this).apply {
            setContentView(R.layout.update_expense_popup)
            window?.apply {
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setBackgroundDrawableResource(android.R.color.white)
                setGravity(Gravity.CENTER)
            }
        }

        // Initialize views
        val spinner = mainDialog.findViewById<Spinner>(R.id.spinner)
        val amountEditText = mainDialog.findViewById<EditText>(R.id.amount)
        val nameEditText = mainDialog.findViewById<EditText>(R.id._name)
        val dateEditText = mainDialog.findViewById<EditText>(R.id.date_input)
        val deleteButton = mainDialog.findViewById<MaterialButton>(R.id.cancell)
        val updateButton = mainDialog.findViewById<MaterialButton>(R.id.savee)

        // 1. POPULATE ALL FIELDS WITH CURRENT EXPENSE DATA
        amountEditText.setText(expense.amount.toString())
        nameEditText.setText(expense.note)
        val tempdate=formatDate(expense.date)
        dateEditText.setText(tempdate)
        // Combine both predefined and user-defined ApiCategory lists
        val predefinedCategories = Constant.getCategories(this, "predefined_categories")
        val userDefinedCategories = Constant.getCategories(this, "user_defined_categories")

        val allCategories: List<SpinnerDataClass> = (predefinedCategories + userDefinedCategories).map {
            SpinnerDataClass(
                id = it._id,
                name = it.name,
                imageUrl = it.icon ?: ""
            )
        }


// Set up the spinner adapter
        val adapter = CustomSpinnerAdapter(this, allCategories)
        spinner.adapter = adapter

// Set current selected category
        val currentPosition = allCategories.indexOfFirst { it.id == expense.category?._id }
        val safePosition = if (currentPosition != -1) currentPosition else 0  // or -1 if you want to show nothing


        if (currentPosition >= 0) {
            spinner.setSelection(currentPosition)
        }

        // 3. SETUP DATE PICKER
        dateEditText.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select a date")
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Date(selection))
                dateEditText.setText(selectedDate)
            }

            datePicker.show(supportFragmentManager, "DATE_PICKER")
        }


        // 4. SETUP DELETE BUTTON
        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(expense, mainDialog)
        }

        // 5. SETUP UPDATE BUTTON
        updateButton.setOnClickListener {
            val amountText = amountEditText.text.toString().trim()
            val note = nameEditText.text.toString().trim()
            val selectedDate = dateEditText.text.toString().trim()

            if (amountText.isEmpty() || note.isEmpty() || selectedDate.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sanitizedAmount = amountText.replace("[^\\d.]".toRegex(), "")
            if (sanitizedAmount.isEmpty()) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedCategory = spinner.selectedItem as SpinnerDataClass
            val categoryId = selectedCategory.id
            val token = Constant.getSavedToken(this)
            val retrofit = obj.createService(API::class.java)

            retrofit.updateExpense(token = token.toString(), id = expense.id, amount = sanitizedAmount, category = categoryId, date = selectedDate, note = note )
                .enqueue(object : Callback<Update_Expense> { override fun onResponse(call: Call<Update_Expense>, response: Response<Update_Expense>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@Expenses, "Expense updated successfully", Toast.LENGTH_SHORT).show()
                        setupBarChart()
                        mainDialog.dismiss()
                    } else {
                        Toast.makeText(this@Expenses, "Failed to update expense", Toast.LENGTH_SHORT).show()
                        Log.e("UpdateExpense", "Error: ${response.code()} - ${response.message()}")
                        Log.e("UpdateExpense", "Body: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<Update_Expense>, t: Throwable) {
                    Toast.makeText(this@Expenses, t.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            })
        }


        mainDialog.show()
    }

    private fun showDeleteConfirmationDialog(expense: Expense, parentDialog: Dialog) {
        val deleteDialog = Dialog(this).apply {
            setContentView(R.layout.delete_expense_popup)
            window?.apply {
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setBackgroundDrawableResource(android.R.color.white)
                setGravity(Gravity.CENTER)
            }
        }

        deleteDialog.findViewById<Button>(R.id.delete).setOnClickListener {
            deleteExpense(expense, parentDialog, deleteDialog)
        }

        deleteDialog.findViewById<Button>(R.id.Update).setOnClickListener {
            deleteDialog.dismiss()
        }

        deleteDialog.show()
    }

    private fun deleteExpense(expense: Expense, vararg dialogs: Dialog) {
        val token = Constant.getSavedToken(this) ?: return
        Log.d("DELETE", "Expense ID: ${expense.id}, Token: $token")
        obj.createService(API::class.java).delete_expense(token, expense.id)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    dialogs.forEach { it.dismiss() }
                    if (response.isSuccessful) {
                        Log.d("DELETE", "Expense ID: ${expense.id}, Token: $token")
                        Log.d("response.errorBody()", response.errorBody().toString())
                        Log.d("response.code()", response.code().toString())
                        Log.d("response.message()", response.message().toString())
                        Toast.makeText(this@Expenses, "Expense deleted successfully", Toast.LENGTH_SHORT).show()
                        setupBarChart()
                    } else {
                        Log.e("DELETE", "Failed to delete. Code: ${response.code()}")
                        Log.e("DELETE", "Error body: ${response.errorBody()?.string()}")
                        Toast.makeText(this@Expenses, "Error deleting expense", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("DELETE", "Network failure: ${t.message}", t)
                    Toast.makeText(this@Expenses, t.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            })
    }


    fun formatDate(isoDate: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(isoDate)

            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formatter.format(date ?: Date())
        } catch (e: Exception) {
            isoDate // fallback to original if parsing fails
        }
    }
    object BudgetRepository {
        private var cachedBudget: Double? = null
        private var lastFetched: Long = 0

        fun getBudget(token: String, callback: (Double?) -> Unit) {
            val now = System.currentTimeMillis()
            // Fetch fresh if cache is old or empty (e.g., older than 5 min)
            if (cachedBudget != null && now - lastFetched < 5 * 60 * 1000) {
                callback(cachedBudget)
                return
            }

            val retrofit = obj.createService(API::class.java)
            retrofit.get_budget(token).enqueue(object : Callback<Get_Budget> {
                override fun onResponse(call: Call<Get_Budget>, response: Response<Get_Budget>) {
                    if (response.isSuccessful) {
                        cachedBudget = response.body()?.totalBudget?.toDouble()
                        lastFetched = now
                        callback(cachedBudget)
                    } else {
                        callback(null)
                    }
                }

                override fun onFailure(call: Call<Get_Budget>, t: Throwable) {
                    callback(null)
                }
            })
        }
    }

}
