package com.example.organizadorapps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable

object WidgetIconUtils {

    fun appIconBitmap(context: Context, app: InstalledApp, sizeDp: Int = 42): Bitmap {
        val drawable = IconCacheManager.getIcon(context, app)
        return drawableToBitmap(drawable, AppUiUtils.dp(context, sizeDp))
    }

    private fun drawableToBitmap(drawable: Drawable, sizePx: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
