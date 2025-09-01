package com.example.expenso.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.expenso.R

import kotlin.math.min

class SemiCircularProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var progress: Float = 0f
        set(value) {
            animateProgress(field, value)
            field = value.coerceIn(0f, 100f)
        }

    private var animatedProgress = 0f
    private val strokeWidth = 60f
    private val arcPadding = 30f

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = this@SemiCircularProgressBar.strokeWidth
        strokeCap = Paint.Cap.ROUND
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        // ✅ This makes the progress arc green using resource color
        color = ContextCompat.getColor(context, R.color.progress_green)
        style = Paint.Style.STROKE
        strokeWidth = this@SemiCircularProgressBar.strokeWidth
        strokeCap = Paint.Cap.ROUND
    }

    private val valueTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 72f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val labelTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 36f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val left = arcPadding
        val top = arcPadding
        val right = width - arcPadding
        val bottom = height * 2 - arcPadding

        val arcRect = RectF(left, top, right, bottom)

        // Draw background arc
        canvas.drawArc(arcRect, 180f, 180f, false, backgroundPaint)

        // Draw animated green progress arc
        canvas.drawArc(arcRect, 180f, 180f * (animatedProgress / 100f), false, progressPaint)

        // Draw center text and label
        val centerX = width / 2
        val centerY = height / 1.6f
        canvas.drawText("475", centerX, centerY, valueTextPaint)
        canvas.drawText("Safe-to-spend", centerX, centerY + 60f, labelTextPaint)
    }

    private fun animateProgress(from: Float, to: Float) {
        ValueAnimator.ofFloat(from, to).apply {
            duration = 1000
            addUpdateListener {
                animatedProgress = it.animatedValue as Float
                invalidate()
            }
        }.start()
    }
}
