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
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.expenso.Data_Class.SpinnerDataClass

class CustomSpinnerAdapter(
    private val context: Context,
    private val categories: List<SpinnerDataClass>
) : BaseAdapter() {

    override fun getCount(): Int = categories.size
    override fun getItem(position: Int): Any = categories[position]
    override fun getItemId(position: Int): Long = position.toLong()

    private fun createView(position: Int, parent: ViewGroup?, layoutId: Int): View {
        val view = LayoutInflater.from(context).inflate(layoutId, parent, false)
        val icon = view.findViewById<ImageView>(R.id.icon_image)
        val name = view.findViewById<TextView>(R.id.spinner_text)
        val arrow = view.findViewById<ImageView>(R.id.arrow_down)

        val item = categories[position]
        name.text = item.name

        val iconUrl = item.imageUrl
        if (!iconUrl.isNullOrEmpty() && iconUrl != "null") {
            Glide.with(context)
                .load(iconUrl)
                .placeholder(generateCircleLetterDrawable(context, item.name.firstOrNull() ?: '?'))
                .error(generateCircleLetterDrawable(context, item.name.firstOrNull() ?: '?'))
                .into(icon)
        } else {
            icon.setImageDrawable(generateCircleLetterDrawable(context, item.name.firstOrNull() ?: '?'))
        }

        // Show arrow only for selected view (not dropdown)
        arrow?.visibility = if (layoutId == R.layout.custom_spinner_item) View.VISIBLE else View.GONE

        return view
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, parent, R.layout.custom_spinner_item)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, parent, R.layout.custom_spinner_dropdown_item)
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
        canvas.drawText(letter.toString().uppercase(), x, y, paint)

        return BitmapDrawable(context.resources, bitmap)
    }
}
