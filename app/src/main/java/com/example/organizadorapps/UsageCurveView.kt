package com.example.organizadorapps

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import kotlin.math.max

class UsageCurveView(context: Context) : View(context) {

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(80, 148, 163, 184)
        strokeWidth = 1.2f
        style = Paint.Style.STROKE
    }

    private val currentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = UiConstants.ACCENT
        strokeWidth = 4f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val previousPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(210, 56, 189, 248)
        strokeWidth = 3f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = UiConstants.TEXT_SECONDARY
        textSize = 24f
    }

    private var points: List<UsageCurvePoint> = emptyList()

    fun setData(newPoints: List<UsageCurvePoint>) {
        points = newPoints
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val contentLeft = paddingLeft + 8f
        val contentTop = paddingTop + 18f
        val contentRight = width - paddingRight - 8f
        val contentBottom = height - paddingBottom - 30f

        if (contentRight <= contentLeft || contentBottom <= contentTop) return

        canvas.drawLine(contentLeft, contentBottom, contentRight, contentBottom, axisPaint)
        canvas.drawLine(contentLeft, contentTop, contentLeft, contentBottom, axisPaint)

        if (points.isEmpty()) {
            canvas.drawText("Sin datos suficientes", contentLeft + 16f, (contentTop + contentBottom) / 2f, labelPaint)
            return
        }

        val maxValue = max(
            1L,
            points.maxOf { max(it.currentValue, it.previousValue) }
        )

        drawSeries(canvas, points.map { it.previousValue }, maxValue, contentLeft, contentTop, contentRight, contentBottom, previousPaint)
        drawSeries(canvas, points.map { it.currentValue }, maxValue, contentLeft, contentTop, contentRight, contentBottom, currentPaint)

        points.firstOrNull()?.let {
            canvas.drawText(it.label, contentLeft, height - paddingBottom - 4f, labelPaint)
        }
        points.lastOrNull()?.let {
            val text = it.label
            canvas.drawText(text, contentRight - labelPaint.measureText(text), height - paddingBottom - 4f, labelPaint)
        }
    }

    private fun drawSeries(
        canvas: Canvas,
        values: List<Long>,
        maxValue: Long,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        paint: Paint
    ) {
        if (values.isEmpty()) return

        val path = Path()
        val step = if (values.size == 1) 0f else (right - left) / (values.size - 1)

        values.forEachIndexed { index, value ->
            val x = left + (step * index)
            val normalized = value.toFloat() / maxValue.toFloat()
            val y = bottom - ((bottom - top) * normalized)

            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        canvas.drawPath(path, paint)
    }
}
