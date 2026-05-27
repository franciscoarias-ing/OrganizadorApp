package com.example.organizadorapps

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import kotlin.math.ceil
import kotlin.math.max

class UsageCurveView(context: Context) : View(context) {

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(90, 148, 163, 184)
        strokeWidth = 1.2f
        style = Paint.Style.STROKE
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(42, 148, 163, 184)
        strokeWidth = 1f
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

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = UiConstants.TEXT_SECONDARY
        textSize = 22f
    }

    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = UiConstants.TEXT_PRIMARY
        textSize = 22f
        textAlign = Paint.Align.RIGHT
    }

    private var points: List<UsageCurvePoint> = emptyList()
    private var isDuration: Boolean = true

    fun setData(newPoints: List<UsageCurvePoint>, durationValues: Boolean = true) {
        points = newPoints
        isDuration = durationValues
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val contentLeft = paddingLeft + 58f
        val contentTop = paddingTop + 22f
        val contentRight = width - paddingRight - 16f
        val contentBottom = height - paddingBottom - 42f

        if (contentRight <= contentLeft || contentBottom <= contentTop) return

        if (points.isEmpty()) {
            canvas.drawText("Sin datos suficientes", contentLeft, (contentTop + contentBottom) / 2f, labelPaint)
            return
        }

        val maxValue = calculateAxisMax(points.maxOf { max(it.currentValue, it.previousValue) })
        drawGridAndYAxis(canvas, maxValue, contentLeft, contentTop, contentRight, contentBottom)
        drawXAxis(canvas, contentLeft, contentRight, contentBottom)

        drawSeries(canvas, points.map { it.previousValue }, maxValue, contentLeft, contentTop, contentRight, contentBottom, previousPaint, drawPoints = false)
        drawSeries(canvas, points.map { it.currentValue }, maxValue, contentLeft, contentTop, contentRight, contentBottom, currentPaint, drawPoints = true)
        drawLastValueLabel(canvas, maxValue, contentLeft, contentTop, contentRight, contentBottom)
    }

    private fun drawGridAndYAxis(
        canvas: Canvas,
        maxValue: Long,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ) {
        val ticks = listOf(0f, 0.5f, 1f)
        ticks.forEach { ratio ->
            val y = bottom - ((bottom - top) * ratio)
            canvas.drawLine(left, y, right, y, if (ratio == 0f) axisPaint else gridPaint)

            val rawValue = (maxValue * ratio).toLong()
            val label = formatAxisValue(rawValue)
            canvas.drawText(label, left - 10f, y + 7f, valuePaint)
        }

        canvas.drawLine(left, top, left, bottom, axisPaint)
    }

    private fun drawXAxis(canvas: Canvas, left: Float, right: Float, bottom: Float) {
        val step = calculateDateStep(points.size)
        val pointStep = if (points.size <= 1) 0f else (right - left) / (points.size - 1)
        val y = bottom + 28f

        points.forEachIndexed { index, point ->
            val shouldDraw = index == 0 || index == points.lastIndex || index % step == 0
            if (!shouldDraw) return@forEachIndexed

            val x = left + (pointStep * index)
            val textWidth = labelPaint.measureText(point.label)
            val safeX = when (index) {
                0 -> x
                points.lastIndex -> x - textWidth
                else -> x - textWidth / 2f
            }
            canvas.drawText(point.label, safeX.coerceIn(left, right - textWidth), y, labelPaint)
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
        paint: Paint,
        drawPoints: Boolean
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

        if (drawPoints) {
            pointPaint.color = paint.color
            val importantIndexes = setOf(0, values.lastIndex, values.indexOf(values.maxOrNull() ?: 0L))
            importantIndexes.forEach { index ->
                if (index < 0 || index >= values.size) return@forEach
                val x = left + (step * index)
                val normalized = values[index].toFloat() / maxValue.toFloat()
                val y = bottom - ((bottom - top) * normalized)
                canvas.drawCircle(x, y, 4.5f, pointPaint)
            }
        }
    }

    private fun drawLastValueLabel(
        canvas: Canvas,
        maxValue: Long,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ) {
        val lastIndex = points.indexOfLast { it.currentValue > 0L }
        if (lastIndex < 0) return

        val pointStep = if (points.size <= 1) 0f else (right - left) / (points.size - 1)
        val point = points[lastIndex]
        val x = left + (pointStep * lastIndex)
        val normalized = point.currentValue.toFloat() / maxValue.toFloat()
        val y = bottom - ((bottom - top) * normalized)
        val label = formatAxisValue(point.currentValue)
        val textWidth = labelPaint.measureText(label)

        labelPaint.color = UiConstants.TEXT_PRIMARY
        canvas.drawText(label, (x - textWidth - 8f).coerceAtLeast(left), (y - 8f).coerceAtLeast(top + 14f), labelPaint)
        labelPaint.color = UiConstants.TEXT_SECONDARY
    }

    private fun calculateDateStep(size: Int): Int {
        return when {
            size <= 7 -> 1
            size <= 21 -> 3
            size <= 45 -> 7
            else -> 14
        }
    }

    private fun calculateAxisMax(rawMax: Long): Long {
        val safeMax = rawMax.coerceAtLeast(1L)
        if (!isDuration) return safeMax.coerceAtLeast(2L)

        val minute = 60_000L
        val safeMinutes = ceil(safeMax.toDouble() / minute.toDouble()).toLong().coerceAtLeast(1L)
        val roundedMinutes = when {
            safeMinutes <= 10 -> 10L
            safeMinutes <= 30 -> 30L
            safeMinutes <= 60 -> 60L
            safeMinutes <= 120 -> 120L
            else -> ceil(safeMinutes / 60.0).toLong() * 60L
        }
        return roundedMinutes * minute
    }

    private fun formatAxisValue(value: Long): String {
        if (!isDuration) return value.toString()

        val minutes = (value / 60_000L).toInt()
        val hours = minutes / 60
        val remaining = minutes % 60
        return when {
            hours > 0 && remaining > 0 -> "${hours}h${remaining}m"
            hours > 0 -> "${hours}h"
            minutes > 0 -> "${minutes}m"
            else -> "0m"
        }
    }
}
