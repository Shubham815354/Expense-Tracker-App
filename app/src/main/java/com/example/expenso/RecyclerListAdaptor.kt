package com.example.expenso

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.expenso.Data_Class.ExpenseListResponse
import com.example.expenso.Data_Class.ExpenseResponse

class RecyclerListAdaptor( private val expenses: List<ExpenseResponse>
) : RecyclerView.Adapter<RecyclerListAdaptor.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon_image_home)
        val categoryName: TextView = itemView.findViewById(R.id.spinner_text_home)
        val amount: TextView = itemView.findViewById(R.id.arrow_down_home)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_recycler_list, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        val context = holder.itemView.context

        val category = expense.categoryId
        holder.amount.text = "₹${expense.amount ?: "0"}"

        if (category != null) {
            holder.categoryName.text = category.name

            val firstLetter = category.name.firstOrNull() ?: '?'
            val icon = category.icon
            val cleanIconPath = icon?.replace("\\", "/")?.replace(Regex("/+"), "/") ?: ""
            val imageUrl = "http://192.168.31.246:3010/$cleanIconPath"

            if (!icon.isNullOrEmpty() && icon != "null") {
                Glide.with(context)
                    .load(imageUrl)
                    .placeholder(generateCircleLetterDrawable(context, firstLetter))
                    .error(generateCircleLetterDrawable(context, firstLetter))
                    .into(holder.icon)
            } else {
                holder.icon.setImageDrawable(
                    generateCircleLetterDrawable(context, firstLetter)
                )
            }
        } else {
            holder.categoryName.text = "Deleted"
            holder.icon.setImageResource(android.R.drawable.ic_delete)
        }
    }

    override fun getItemCount(): Int = expenses.size

    private fun generateCircleLetterDrawable(context: Context, letter: Char): BitmapDrawable {
        val size = 100
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.submit_button)
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        paint.color = Color.WHITE
        paint.textSize = 40f
        paint.textAlign = Paint.Align.CENTER
        val fontMetrics = paint.fontMetrics
        val x = size / 2f
        val y = size / 2f - (fontMetrics.ascent + fontMetrics.descent) / 2
        canvas.drawText(letter.toString(), x, y, paint)

        return BitmapDrawable(context.resources, bitmap)
    }
}