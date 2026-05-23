package com.example.organizadorapps

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object RecentAppsManager {

    private const val PREFS = "recent_apps_prefs"
    private const val KEY = "recent_apps"

    fun registerAppOpen(context: Context, app: InstalledApp) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        val existing = getRecentAppsRaw(context)
            .filterNot { it.optString("packageName") == app.packageName }
            .toMutableList()

        val newItem = JSONObject().apply {
            put("name", app.name)
            put("packageName", app.packageName)
            put("timestamp", System.currentTimeMillis())
        }

        existing.add(0, newItem)

        val array = JSONArray()
        existing.take(20).forEach { array.put(it) }

        prefs.edit().putString(KEY, array.toString()).apply()
    }

    fun getRecentPackageNames(context: Context): List<String> {
        return getRecentAppsRaw(context).map {
            it.optString("packageName")
        }
    }

    fun getRecentTimeLabel(context: Context, packageName: String): String {
        val item = getRecentAppsRaw(context)
            .firstOrNull { it.optString("packageName") == packageName }
            ?: return "Reciente"

        val timestamp = item.optLong("timestamp", 0L)
        if (timestamp <= 0L) return "Reciente"

        val diffMinutes = ((System.currentTimeMillis() - timestamp) / 60000).coerceAtLeast(1)

        return when {
            diffMinutes < 60 -> "Hace ${diffMinutes} min"
            diffMinutes < 1440 -> "Hace ${diffMinutes / 60} h"
            else -> "Hace ${diffMinutes / 1440} d"
        }
    }

    private fun getRecentAppsRaw(context: Context): List<JSONObject> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY, "[]") ?: "[]"
        val jsonArray = JSONArray(raw)

        return List(jsonArray.length()) {
            jsonArray.getJSONObject(it)
        }
    }
}