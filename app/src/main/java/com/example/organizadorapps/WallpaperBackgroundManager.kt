package com.example.organizadorapps

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.FrameLayout
import android.widget.ImageView

/**
 * Fondo visual basado en el wallpaper de la pantalla principal.
 *
 * Importante: Android permite leer el wallpaper del sistema en muchos equipos,
 * pero algunos fabricantes/versiones devuelven una imagen limitada o bloquean
 * el acceso. En ese caso se usa el fondo oscuro de la app como respaldo.
 */
object WallpaperBackgroundManager {

    fun buildWallpaperLayer(context: Context): FrameLayout {
        return FrameLayout(context).apply {
            setBackgroundColor(UiConstants.BACKGROUND)

            addView(
                ImageView(context).apply {
                    setImageDrawable(getHomeWallpaperDrawable(context))
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    adjustViewBounds = false
                    alpha = 1f
                },
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }
    }

    fun getHomeWallpaperDrawable(context: Context): Drawable {
        val appContext = context.applicationContext
        val wallpaperManager = WallpaperManager.getInstance(appContext)

        return try {
            getBitmapDrawable(appContext, wallpaperManager)
                ?: getDrawableCompat(wallpaperManager)
                ?: ColorDrawable(UiConstants.BACKGROUND)
        } catch (_: SecurityException) {
            ColorDrawable(UiConstants.BACKGROUND)
        } catch (_: Exception) {
            ColorDrawable(UiConstants.BACKGROUND)
        }
    }

    private fun getBitmapDrawable(
        context: Context,
        wallpaperManager: WallpaperManager
    ): Drawable? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return null

        return try {
            val fileDescriptor = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM)
                ?: return null

            fileDescriptor.use { descriptor ->
                val bitmap: Bitmap = BitmapFactory.decodeFileDescriptor(descriptor.fileDescriptor)
                    ?: return null

                BitmapDrawable(context.resources, bitmap).apply {
                    setDither(true)
                }
            }
        } catch (_: SecurityException) {
            null
        } catch (_: Exception) {
            null
        }
    }

    @Suppress("DEPRECATION")
    private fun getDrawableCompat(wallpaperManager: WallpaperManager): Drawable? {
        return try {
            wallpaperManager.drawable
        } catch (_: Exception) {
            null
        }
    }

    fun wallpaperScrimColor(): Int {
        // Más transparente para que el wallpaper sí se aprecie, pero sin perder lectura.
        return Color.argb(118, 7, 12, 22)
    }
}
