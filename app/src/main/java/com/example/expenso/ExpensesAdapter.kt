package com.example.expenso

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.expenso.Data_Class.Expense
import com.example.expenso.Data_Class.ExpenseResponse
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpensesAdapter(
    private val expenses: List<Expense>,
    private val onItemClick: (Expense) -> Unit
) : RecyclerView.Adapter<ExpensesAdapter.ExpenseViewHolder>() {

    inner class ExpenseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvExpenseName: TextView = view.findViewById(R.id.tvExpenseName)
        private val tvExpenseAmount: TextView = view.findViewById(R.id.tvExpenseAmount)
        private val colorBar: View = view.findViewById(R.id.colorBar)
        private val percentageText: TextView = view.findViewById(R.id.percentageText)

        fun bind(expense: Expense) {
            tvExpenseName.text = expense.name
            tvExpenseAmount.text = expense.amount
            percentageText.text = expense.percentage

            // Clean percentage and convert it to a float
            val percentageValue = expense.percentage.replace("%", "").toFloatOrNull()?.takeIf { it > 0 } ?: 1f // Fallback to 1% if 0%

            Log.d("Expense", "Expense Name: ${expense.name}, Percentage: ${expense.percentage}")

            // Set color bar width based on percentage
            colorBar.post {
                val parent = colorBar.parent as? View ?: return@post
                parent.post {
                    val parentWidth = parent.width
                    if (parentWidth > 0) {
                        val calculatedWidth = maxOf((percentageValue / 100f) * parentWidth, 1f) // Ensure at least 1px width

                        val layoutParams = colorBar.layoutParams
                        layoutParams.width = calculatedWidth.toInt()
                        colorBar.layoutParams = layoutParams

                        // Validate and set color
                        val colorString = expense.color
                        if (isValidHexColor(colorString)) {
                            try {
                                colorBar.setBackgroundColor(Color.parseColor(colorString))
                            } catch (e: IllegalArgumentException) {
                                Log.e("ExpensesAdapter", "Invalid color (parse failure): $colorString", e)
                                colorBar.setBackgroundColor(Color.GRAY) // Default color on error
                            }
                        } else {
                            Log.e("ExpensesAdapter", "Invalid color format: $colorString")
                            colorBar.setBackgroundColor(Color.GRAY) // Default color on invalid color
                        }
                    }
                }
            }

            itemView.setOnClickListener {
                onItemClick(expense)
            }
        }



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.expenses_item, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(expenses[position])
    }

    override fun getItemCount(): Int = expenses.size

    private fun isValidHexColor(color: String?): Boolean {
        return color != null && Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$").matches(color)
    }

}

