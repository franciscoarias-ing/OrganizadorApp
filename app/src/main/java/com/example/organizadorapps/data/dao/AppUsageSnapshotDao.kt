package com.example.organizadorapps.data.dao

import android.content.ContentValues
import android.content.Context
import com.example.organizadorapps.data.LocalAppDatabase
import com.example.organizadorapps.data.entity.AppUsageSnapshotEntity

class AppUsageSnapshotDao(context: Context) {

    private val database = LocalAppDatabase.getInstance(context)

    fun upsertSnapshot(packageName: String, appName: String, lastUsedAt: Long, source: String) {
        if (lastUsedAt <= 0L) return

        val db = database.writableDatabase
        val current = getLastUsedAt(packageName)
        if (current != null && current > lastUsedAt) return

        val now = System.currentTimeMillis()
        val values = ContentValues().apply {
            put("package_name", packageName)
            put("app_name", appName)
            put("last_used_at", lastUsedAt)
            put("source", source)
            put("updated_at", now)
        }
        db.insertWithOnConflict("app_usage_snapshot", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun upsertSnapshots(items: List<AppUsageSnapshotEntity>) {
        if (items.isEmpty()) return
        val db = database.writableDatabase
        db.beginTransaction()
        try {
            items.forEach { item ->
                val values = ContentValues().apply {
                    put("package_name", item.packageName)
                    put("app_name", item.appName)
                    put("last_used_at", item.lastUsedAt)
                    put("source", item.source)
                    put("updated_at", item.updatedAt)
                }
                db.insertWithOnConflict("app_usage_snapshot", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getLastUsedAt(packageName: String): Long? {
        val db = database.readableDatabase
        db.rawQuery(
            "SELECT last_used_at FROM app_usage_snapshot WHERE package_name = ? LIMIT 1",
            arrayOf(packageName)
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getLong(0) else null
        }
    }

    fun getRecentPackages(limit: Int): List<String> {
        val db = database.readableDatabase
        val result = mutableListOf<String>()
        db.rawQuery(
            """
            SELECT package_name
            FROM app_usage_snapshot
            ORDER BY last_used_at DESC
            LIMIT ?
            """.trimIndent(),
            arrayOf(limit.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) result.add(cursor.getString(0))
        }
        return result
    }
}
