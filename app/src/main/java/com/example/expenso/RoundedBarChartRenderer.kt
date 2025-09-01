package com.example.expenso

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.buffer.BarBuffer
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler

class RoundedBarChartRenderer(
    chart: BarDataProvider,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : BarChartRenderer(chart, animator, viewPortHandler) {

    override fun drawDataSet(c: Canvas, dataSet: IBarDataSet, index: Int) {
        val buffer: BarBuffer = mBarBuffers[index]
        val barRadius = 30f

        for (i in 0 until buffer.size() step 4) {
            val left = buffer.buffer[i]
            val top = buffer.buffer[i + 1] * mAnimator.phaseY
            val right = buffer.buffer[i + 2]
            val bottom = buffer.buffer[i + 3]

            if (top >= bottom) continue  // Avoid upside-down bars

            val rect = RectF(left, top, right, bottom)
            val path = Path().apply {
                addRoundRect(
                    rect,
                    floatArrayOf(barRadius, barRadius, barRadius, barRadius, 0f, 0f, 0f, 0f),
                    Path.Direction.CW
                )
            }

            mRenderPaint.color = dataSet.getColor(i / 4)
            c.drawPath(path, mRenderPaint)
        }
    }
}
