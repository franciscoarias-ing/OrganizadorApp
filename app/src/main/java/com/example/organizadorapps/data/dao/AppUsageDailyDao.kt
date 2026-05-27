package com.example.organizadorapps.data.dao

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.organizadorapps.data.LocalAppDatabase
import com.example.organizadorapps.data.entity.AppUsageDailyEntity

class AppUsageDailyDao(context: Context) {

    private val database = LocalAppDatabase.getInstance(context.applicationContext)

    fun upsertDailyUsage(items: List<AppUsageDailyEntity>) {
        if (items.isEmpty()) return

        val db = database.writableDatabase
        db.beginTransaction()
        try {
            items.forEach { item ->
                val values = ContentValues().apply {
                    put("package_name", item.packageName)
                    put("app_name", item.appName)
                    put("usage_date", item.usageDate)
                    put("total_usage_ms", item.totalUsageMs)
                    put("open_count", item.openCount)
                    put("source", item.source)
                    put("updated_at", item.updatedAt)
                }
                db.insertWithOnConflict(
                    "app_usage_daily",
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getDailyUsageForPackage(
        packageName: String,
        startDate: String,
        endDate: String,
        source: String
    ): Map<String, Long> {
        val db = database.readableDatabase
        val result = linkedMapOf<String, Long>()

        db.rawQuery(
            """
            SELECT usage_date, total_usage_ms
            FROM app_usage_daily
            WHERE package_name = ?
              AND source = ?
              AND usage_date BETWEEN ? AND ?
            ORDER BY usage_date ASC
            """.trimIndent(),
            arrayOf(packageName, source, startDate, endDate)
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result[cursor.getString(0)] = cursor.getLong(1)
            }
        }

        return result
    }
}
