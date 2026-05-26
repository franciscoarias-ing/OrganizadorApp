package com.example.organizadorapps

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.view.View
import kotlin.math.max

/**
 * Fondo opaco tipo "wallpaper distorsionado".
 *
 * Se usa mientras Android no permite leer el wallpaper real de forma estable.
 * A diferencia de windowIsTranslucent/FLAG_SHOW_WALLPAPER, este fondo no deja ver
 * iconos/widgets del launcher durante la apertura de la app.
 */
class AppBackgroundView(context: Context) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat().coerceAtLeast(1f)
        val h = height.toFloat().coerceAtLeast(1f)
        val radius = max(w, h)

        paint.shader = LinearGradient(
            0f,
            0f,
            w,
            h,
            intArrayOf(
                Color.rgb(5, 9, 18),
                Color.rgb(10, 16, 31),
                Color.rgb(5, 9, 18)
            ),
            floatArrayOf(0f, 0.48f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, paint)

        drawBlob(canvas, w * 0.12f, h * 0.06f, radius * 0.42f, Color.argb(88, 126, 34, 206))
        drawBlob(canvas, w * 0.88f, h * 0.18f, radius * 0.30f, Color.argb(56, 14, 165, 233))
        drawBlob(canvas, w * 0.20f, h * 0.74f, radius * 0.38f, Color.argb(58, 168, 85, 247))
        drawBlob(canvas, w * 0.82f, h * 0.88f, radius * 0.34f, Color.argb(42, 34, 197, 94))

        paint.shader = LinearGradient(
            0f,
            0f,
            0f,
            h,
            intArrayOf(
                Color.argb(42, 255, 255, 255),
                Color.argb(0, 255, 255, 255),
                Color.argb(72, 0, 0, 0)
            ),
            floatArrayOf(0f, 0.42f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, paint)

        paint.shader = null
        paint.color = Color.argb(112, 3, 7, 18)
        canvas.drawRect(0f, 0f, w, h, paint)
    }

    private fun drawBlob(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int) {
        paint.shader = RadialGradient(
            cx,
            cy,
            radius,
            color,
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, radius, paint)
    }
}
