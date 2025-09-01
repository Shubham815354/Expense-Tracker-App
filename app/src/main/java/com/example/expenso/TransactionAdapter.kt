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
import com.example.expenso.view.Expense
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TransactionAdapter(private val items: List<Expense>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon = itemView.findViewById<ImageView>(R.id.icon_image)
        val name = itemView.findViewById<TextView>(R.id.spinner_text)
        val amount = itemView.findViewById<TextView>(R.id.arrow_down)
        val date = itemView.findViewById<TextView>(R.id.transaction_date)  // Added here
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction_item_list, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val item = items[position]

        // Set category and amount as before
        val category = item.categoryId
        if (category != null && !category.name.isNullOrBlank()) {
            holder.name.text = category.name
            holder.amount.text = item.amount.toString()

            val icon = category.icon?.takeIf { it != "null" && it.isNotBlank() }
            if (icon != null) {
                val cleanIconPath = icon.replace("\\", "/").replace(Regex("/+"), "/")
                val imageUrl = "https://expensio-nkvc.onrender.com/$cleanIconPath"
                Glide.with(holder.itemView.context)
                    .load(imageUrl)
                    .placeholder(generateCircleLetterDrawable(holder.itemView.context, category.name.firstOrNull() ?: '?'))
                    .error(generateCircleLetterDrawable(holder.itemView.context, category.name.firstOrNull() ?: '?'))
                    .into(holder.icon)
            } else {
                holder.icon.setImageDrawable(
                    generateCircleLetterDrawable(holder.itemView.context, category.name.firstOrNull() ?: '?')
                )
            }
        } else {
            holder.name.text = "Deleted"
            holder.amount.text = item.amount.toString()
            holder.icon.setImageResource(android.R.drawable.ic_delete)
        }

        // --- Parse and format date ---
        holder.date.text = formatBackendDate(item.date)
    }

    override fun getItemCount(): Int = items.size

    // Function to parse ISO 8601 date string and format it to "dd/MM/yyyy"
    private fun formatBackendDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""

        return try {
            // Parse ISO 8601 date string with timezone "Z"
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val parsedDate: Date = parser.parse(dateString)!!

            // Format to desired display format, e.g. "dd/MM/yyyy"
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            formatter.format(parsedDate)
        } catch (e: Exception) {
            // If parsing fails, return original string or empty
            dateString
        }
    }
}

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

