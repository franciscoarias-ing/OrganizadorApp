package com.example.organizadorapps

import android.content.Context
import com.example.organizadorapps.data.dao.AppLaunchDao
import com.example.organizadorapps.data.dao.AppUsageSnapshotDao
import com.example.organizadorapps.data.entity.AppLaunchEntity
import org.json.JSONArray
import org.json.JSONObject

object RecentAppsManager {

    private const val PREFS = "recent_apps_prefs"
    private const val KEY = "recent_apps"
    private const val KEY_DB_MIGRATED = "recent_apps_db_migrated"

    fun registerAppOpen(context: Context, app: InstalledApp) {
        val timestamp = System.currentTimeMillis()

        runCatching {
            AppLaunchDao(context).insertLaunch(
                packageName = app.packageName,
                appName = app.name,
                openedAt = timestamp,
                source = AppLaunchEntity.SOURCE_FROM_APP
            )
            AppUsageSnapshotDao(context).upsertSnapshot(
                packageName = app.packageName,
                appName = app.name,
                lastUsedAt = timestamp,
                source = AppLaunchEntity.SOURCE_FROM_APP
            )
        }

        // Fallback temporal para no romper lecturas antiguas mientras se estabiliza la BD.
        registerAppOpenLegacy(context, app, timestamp)
    }

    fun getRecentPackageNames(context: Context, limit: Int = 20): List<String> {
        migrateLegacyIfNeeded(context)

        val dbPackages = runCatching {
            AppLaunchDao(context).getRecentPackages(limit)
        }.getOrDefault(emptyList())

        if (dbPackages.isNotEmpty()) return dbPackages

        return getRecentAppsRaw(context).map { it.optString("packageName") }.take(limit)
    }

    fun getLastKnownUsedAt(context: Context, packageName: String): Long? {
        val snapshotTime = runCatching {
            AppUsageSnapshotDao(context).getLastUsedAt(packageName)
        }.getOrNull()

        if (snapshotTime != null && snapshotTime > 0L) return snapshotTime

        val launchTime = runCatching {
            AppLaunchDao(context).getLastOpenedAt(packageName)
        }.getOrNull()

        if (launchTime != null && launchTime > 0L) return launchTime

        return getRecentAppsRaw(context)
            .firstOrNull { it.optString("packageName") == packageName }
            ?.optLong("timestamp", 0L)
            ?.takeIf { it > 0L }
    }

    fun getRecentTimeLabel(context: Context, packageName: String): String {
        val timestamp = getLastKnownUsedAt(context, packageName) ?: return "Reciente"
        return formatRelativeTime(timestamp)
    }

    fun formatRelativeTime(timestamp: Long): String {
        if (timestamp <= 0L) return "Reciente"

        val diffMinutes = ((System.currentTimeMillis() - timestamp) / 60000).coerceAtLeast(1)

        return when {
            diffMinutes < 60 -> "Hace ${diffMinutes} min"
            diffMinutes < 1440 -> "Hace ${diffMinutes / 60} h"
            else -> "Hace ${diffMinutes / 1440} d"
        }
    }

    private fun registerAppOpenLegacy(context: Context, app: InstalledApp, timestamp: Long) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        val existing = getRecentAppsRaw(context)
            .filterNot { it.optString("packageName") == app.packageName }
            .toMutableList()

        val newItem = JSONObject().apply {
            put("name", app.name)
            put("packageName", app.packageName)
            put("timestamp", timestamp)
        }

        existing.add(0, newItem)

        val array = JSONArray()
        existing.take(20).forEach { array.put(it) }

        prefs.edit().putString(KEY, array.toString()).apply()
    }

    private fun migrateLegacyIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_DB_MIGRATED, false)) return

        val legacyItems = getRecentAppsRaw(context).mapNotNull { json ->
            val packageName = json.optString("packageName").takeIf { it.isNotBlank() } ?: return@mapNotNull null
            AppLaunchEntity(
                packageName = packageName,
                appName = json.optString("name", packageName),
                openedAt = json.optLong("timestamp", System.currentTimeMillis()),
                source = AppLaunchEntity.SOURCE_FROM_APP
            )
        }

        runCatching {
            AppLaunchDao(context).migrateLegacyLaunches(legacyItems)
            legacyItems.forEach { item ->
                AppUsageSnapshotDao(context).upsertSnapshot(
                    packageName = item.packageName,
                    appName = item.appName,
                    lastUsedAt = item.openedAt,
                    source = item.source
                )
            }
            prefs.edit().putBoolean(KEY_DB_MIGRATED, true).apply()
        }
    }

    private fun getRecentAppsRaw(context: Context): List<JSONObject> {
        return runCatching {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val raw = prefs.getString(KEY, "[]") ?: "[]"
            val jsonArray = JSONArray(raw)

            List(jsonArray.length()) {
                jsonArray.getJSONObject(it)
            }
        }.getOrDefault(emptyList())
    }
}
