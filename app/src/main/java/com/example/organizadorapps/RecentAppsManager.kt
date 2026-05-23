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
            .filterNot { it.getString("packageName") == app.packageName }
            .toMutableList()

        val newItem = JSONObject().apply {
            put("name", app.name)
            put("packageName", app.packageName)
            put("timestamp", System.currentTimeMillis())
        }

        existing.add(0, newItem)

        val trimmed = existing.take(20)

        val array = JSONArray()
        trimmed.forEach { array.put(it) }

        prefs.edit().putString(KEY, array.toString()).apply()
    }

    fun getRecentPackageNames(context: Context): List<String> {
        return getRecentAppsRaw(context).map {
            it.getString("packageName")
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