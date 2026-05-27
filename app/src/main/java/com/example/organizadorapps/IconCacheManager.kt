package com.example.organizadorapps

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.LruCache

/**
 * Cache simple de iconos para evitar pedir repetidamente Drawables al PackageManager.
 *
 * La clave es packageName. No persiste en disco: se limpia al cerrar el proceso,
 * por lo que no cambia datos ni comportamiento funcional de la app.
 */
object IconCacheManager {

    private val cache = object : LruCache<String, Drawable>(120) {}

    fun getIcon(context: Context, app: InstalledApp): Drawable {
        return getIcon(context, app.packageName) ?: app.icon
    }

    fun getIcon(context: Context, packageName: String): Drawable? {
        cache.get(packageName)?.let { return it }

        return runCatching {
            context.packageManager.getApplicationIcon(packageName)
        }.getOrNull()?.also { drawable ->
            cache.put(packageName, drawable)
        }
    }

    fun putIcon(packageName: String, drawable: Drawable) {
        cache.put(packageName, drawable)
    }

    fun preload(context: Context, apps: List<InstalledApp>, limit: Int = 24) {
        apps.take(limit).forEach { app ->
            if (cache.get(app.packageName) == null) {
                cache.put(app.packageName, app.icon)
            }
        }
    }

    fun remove(packageName: String) {
        cache.remove(packageName)
    }

    fun clear() {
        cache.evictAll()
    }
}
