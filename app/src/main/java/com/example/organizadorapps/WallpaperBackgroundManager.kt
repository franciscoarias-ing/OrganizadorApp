package com.example.organizadorapps

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout

/**
 * Administrador del fondo principal.
 *
 * Decisión actual: no usar ventana translúcida ni FLAG_SHOW_WALLPAPER en MainActivity.
 * Ese enfoque deja ver el launcher real durante la transición de apertura/cierre.
 * En su lugar se pinta un fondo opaco tipo wallpaper distorsionado dentro de la app.
 */
object WallpaperBackgroundManager {

    fun prepareWallpaperWindow(window: Window) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.setBackgroundDrawable(ColorDrawable(Color.rgb(5, 9, 18)))
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }

    fun buildWallpaperLayer(context: Context): FrameLayout {
        return FrameLayout(context).apply {
            setBackgroundColor(Color.rgb(5, 9, 18))
            addView(
                AppBackgroundView(context),
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }
    }

    fun wallpaperScrimColor(): Int {
        return Color.argb(34, 0, 0, 0)
    }
}
