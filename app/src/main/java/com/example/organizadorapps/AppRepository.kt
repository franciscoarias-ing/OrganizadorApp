package com.example.organizadorapps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import com.example.organizadorapps.data.dao.AppDao

object AppRepository {

    private const val CACHE_TTL_MS = 5 * 60 * 1000L

    @Volatile
    private var cachedLaunchableApps: List<InstalledApp>? = null

    @Volatile
    private var cachedAt: Long = 0L

    @Volatile
    private var isBackgroundRefreshRunning: Boolean = false

    fun getInstalledLaunchableApps(context: Context): List<InstalledApp> {
        return getInstalledLaunchableAppsCached(context)
    }

    /**
     * Carga rápida para Home/Compact.
     *
     * Prioridad:
     * 1. Memoria reciente.
     * 2. BD local.
     * 3. PackageManager como fallback.
     */
    fun getAppsFast(context: Context): List<InstalledApp> {
        val hiddenPackages = HiddenAppsManager.getHiddenPackages(context)
        val memoryApps = cachedLaunchableApps
        val now = System.currentTimeMillis()

        if (memoryApps != null && now - cachedAt < CACHE_TTL_MS) {
            return memoryApps
                .filterNot { it.packageName in hiddenPackages }
                .sortedBy { it.name.lowercase() }
        }

        val rawDbApps = runCatching { AppDao(context).getInstalledApps() }
            .getOrDefault(emptyList())
            .sortedBy { it.name.lowercase() }

        if (rawDbApps.isNotEmpty()) {
            cachedLaunchableApps = rawDbApps
            cachedAt = now
            IconCacheManager.preload(context, rawDbApps)
            return rawDbApps
                .filterNot { it.packageName in hiddenPackages }
                .sortedBy { it.name.lowercase() }
        }

        return getInstalledLaunchableAppsCached(context)
    }

    fun getInstalledLaunchableAppsCached(context: Context): List<InstalledApp> {
        val hiddenPackages = HiddenAppsManager.getHiddenPackages(context)
        val rawApps = getRawLaunchableApps(context, forceRefresh = false)
        return rawApps
            .filterNot { it.packageName in hiddenPackages }
            .sortedBy { it.name.lowercase() }
    }

    fun refreshInstalledApps(context: Context): List<InstalledApp> {
        val hiddenPackages = HiddenAppsManager.getHiddenPackages(context)
        val rawApps = getRawLaunchableApps(context, forceRefresh = true)
        return rawApps
            .filterNot { it.packageName in hiddenPackages }
            .sortedBy { it.name.lowercase() }
    }

    fun refreshAppsInBackground(
        context: Context,
        onUpdated: ((List<InstalledApp>) -> Unit)? = null
    ) {
        if (isBackgroundRefreshRunning) return

        val appContext = context.applicationContext
        isBackgroundRefreshRunning = true

        Thread {
            val updatedApps = runCatching {
                refreshInstalledApps(appContext)
            }.getOrDefault(emptyList())

            isBackgroundRefreshRunning = false

            if (updatedApps.isNotEmpty() && onUpdated != null) {
                Handler(Looper.getMainLooper()).post {
                    onUpdated(updatedApps)
                }
            }
        }.start()
    }

    fun getHiddenLaunchableApps(context: Context): List<InstalledApp> {
        val hiddenPackages = HiddenAppsManager.getHiddenPackages(context)
        return getRawLaunchableApps(context, forceRefresh = false)
            .filter { it.packageName in hiddenPackages }
            .sortedBy { it.name.lowercase() }
    }

    fun invalidateCache() {
        cachedLaunchableApps = null
        cachedAt = 0L
        IconCacheManager.clear()
    }

    private fun getRawLaunchableApps(context: Context, forceRefresh: Boolean): List<InstalledApp> {
        val now = System.currentTimeMillis()
        val currentCache = cachedLaunchableApps

        if (!forceRefresh && currentCache != null && now - cachedAt < CACHE_TTL_MS) {
            return currentCache
        }

        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val packageManager = context.packageManager
        val loadedApps = packageManager
            .queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .map {
                InstalledApp(
                    name = it.loadLabel(packageManager).toString(),
                    packageName = it.activityInfo.packageName,
                    icon = it.loadIcon(packageManager)
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.name.lowercase() }

        cachedLaunchableApps = loadedApps
        cachedAt = now
        IconCacheManager.preload(context, loadedApps)

        // Persistimos inventario ligero para futuras cargas rápidas desde BD.
        runCatching {
            AppDao(context).upsertInstalledApps(
                apps = loadedApps,
                hiddenPackages = HiddenAppsManager.getHiddenPackages(context)
            )
        }

        return loadedApps
    }
}
