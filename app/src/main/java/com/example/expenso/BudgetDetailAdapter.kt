package com.example.expenso

import android.R.attr.category
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
import com.example.expenso.Data_Class.Get_Budget

class BudgetDetailAdapter( var arr: List<com.example.expenso.Data_Class.Budget>,val month:String,val frequency:String,
                           private val onItemClick: (com.example.expenso.Data_Class.Budget) -> Unit) : RecyclerView.Adapter<BudgetDetailAdapter.MyHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyHolder {
        val convertedview = LayoutInflater.from(parent.context).inflate(R.layout.budget_category_recyclerview,parent,false)
        return MyHolder(convertedview)
    }

    override fun onBindViewHolder(
        holder: MyHolder,
        position: Int
    ) {
        val currentPosition = arr[position]
        holder.bindview(currentPosition)
        holder.itemView.setOnClickListener {
            onItemClick(arr[position])
        }

    }

    override fun getItemCount(): Int {
        return arr.size
    }

    inner class MyHolder(item:View): RecyclerView.ViewHolder(item){
        val image: ImageView = item.findViewById(R.id.category_iconn)
        val title : TextView = item.findViewById(R.id.category_title_re)
        val frq : TextView = item.findViewById(R.id.frequency_text_re)
        val amt : TextView = item.findViewById(R.id.amount_text_re)

        fun bindview(display: com.example.expenso.Data_Class.Budget){
            val category = display.categoryId
            title.text=category.name
            frq.text=display.frequency
            amt.text= display.amount.toString()

            val imagee =display.categoryId.icon
            val cleanIconPath = imagee?.replace("\\", "/")?.replace(Regex("/+"), "/") ?: ""
            val url="https://expensio-nkvc.onrender.com/$cleanIconPath"
            Log.d("ImageUrl",url)
            if(!imagee.isNullOrEmpty() && imagee!="null"){
                Glide.with(itemView.context)
                    .load(url)
                    .placeholder(generateCircleLetterDrawable(itemView.context, category.name.firstOrNull() ?: '?'))
                    .error(generateCircleLetterDrawable(itemView.context, category.name.firstOrNull() ?: '?'))
                    .into(image)
            }else {
                image.setImageDrawable(
                    generateCircleLetterDrawable(
                        itemView.context,
                        category.name.firstOrNull() ?: '?'
                    )
                )
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
}