package com.example.organizadorapps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.organizadorapps.data.dao.AppDao

object AppRepository {

    private const val CACHE_TTL_MS = 5 * 60 * 1000L

    @Volatile
    private var cachedLaunchableApps: List<InstalledApp>? = null

    @Volatile
    private var cachedAt: Long = 0L

    fun getInstalledLaunchableApps(context: Context): List<InstalledApp> {
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

    fun getHiddenLaunchableApps(context: Context): List<InstalledApp> {
        val hiddenPackages = HiddenAppsManager.getHiddenPackages(context)
        return getRawLaunchableApps(context, forceRefresh = false)
            .filter { it.packageName in hiddenPackages }
            .sortedBy { it.name.lowercase() }
    }

    fun invalidateCache() {
        cachedLaunchableApps = null
        cachedAt = 0L
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

        // Persistimos inventario ligero para futuras funciones sin bloquear el flujo si falla SQLite.
        runCatching {
            AppDao(context).upsertInstalledApps(
                apps = loadedApps,
                hiddenPackages = HiddenAppsManager.getHiddenPackages(context)
            )
        }

        return loadedApps
    }
}
