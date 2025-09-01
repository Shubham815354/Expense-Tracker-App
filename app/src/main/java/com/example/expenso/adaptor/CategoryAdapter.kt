package com.example.expenso.adaptor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.expenso.Data_Class.CategoryItem
import com.example.expenso.R

class CategoryAdapter(
    private val items: MutableList<CategoryItem>,
    private val onCategoryClick: (CategoryItem) -> Unit // <-- Add this
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.category_icon)
        val title: TextView = view.findViewById(R.id.category_title)
    }

    fun addAll(newItems: List<CategoryItem>) {
        val startPosition = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(startPosition, newItems.size)
    }

    fun addItem(item: CategoryItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title

        val iconUrl = item.iconUrl
        if (!iconUrl.isNullOrEmpty() && iconUrl != "null") {
            Glide.with(holder.itemView.context)
                .load(iconUrl)
                .placeholder(generateCircleLetterDrawable(holder.itemView.context, item.firstLetter))
                .into(holder.icon)
        } else {
            holder.icon.setImageDrawable(
                generateCircleLetterDrawable(holder.itemView.context, item.firstLetter)
            )
        }

        // Handle category click
        holder.itemView.setOnClickListener {
            onCategoryClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

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

    // Remove item by id and return the index
    fun removeItemById(id: String): Int {
        val index = items.indexOfFirst { it.id == id }
        if (index != -1) {
            items.removeAt(index)
        }
        return index
    }
}
