package com.example.organizadorapps.data.dao

import android.content.ContentValues
import android.content.Context
import com.example.organizadorapps.data.LocalAppDatabase
import com.example.organizadorapps.data.entity.AppLaunchEntity

class AppLaunchDao(context: Context) {

    private val database = LocalAppDatabase.getInstance(context)

    fun insertLaunch(packageName: String, appName: String, openedAt: Long, source: String) {
        val db = database.writableDatabase
        val values = ContentValues().apply {
            put("package_name", packageName)
            put("app_name", appName)
            put("opened_at", openedAt)
            put("source", source)
        }
        db.insert("app_launch_history", null, values)
        trimHistory(maxRows = 80)
    }

    fun getRecentPackages(limit: Int): List<String> {
        val db = database.readableDatabase
        val result = mutableListOf<String>()
        val seen = mutableSetOf<String>()

        db.rawQuery(
            """
            SELECT package_name
            FROM app_launch_history
            ORDER BY opened_at DESC
            LIMIT ?
            """.trimIndent(),
            arrayOf((limit * 4).coerceAtLeast(limit).toString())
        ).use { cursor ->
            while (cursor.moveToNext() && result.size < limit) {
                val packageName = cursor.getString(0)
                if (seen.add(packageName)) result.add(packageName)
            }
        }

        return result
    }

    fun getLastOpenedAt(packageName: String): Long? {
        val db = database.readableDatabase
        db.rawQuery(
            """
            SELECT opened_at
            FROM app_launch_history
            WHERE package_name = ?
            ORDER BY opened_at DESC
            LIMIT 1
            """.trimIndent(),
            arrayOf(packageName)
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getLong(0) else null
        }
    }

    fun migrateLegacyLaunches(items: List<AppLaunchEntity>) {
        if (items.isEmpty()) return
        val db = database.writableDatabase
        db.beginTransaction()
        try {
            items.forEach { item ->
                val values = ContentValues().apply {
                    put("package_name", item.packageName)
                    put("app_name", item.appName)
                    put("opened_at", item.openedAt)
                    put("source", item.source)
                }
                db.insert("app_launch_history", null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        trimHistory(maxRows = 80)
    }

    private fun trimHistory(maxRows: Int) {
        val db = database.writableDatabase
        db.execSQL(
            """
            DELETE FROM app_launch_history
            WHERE id NOT IN (
                SELECT id FROM app_launch_history
                ORDER BY opened_at DESC
                LIMIT ?
            )
            """.trimIndent(),
            arrayOf<Any>(maxRows)
        )
    }
}
