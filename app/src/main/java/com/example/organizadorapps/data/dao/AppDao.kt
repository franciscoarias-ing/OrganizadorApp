package com.example.organizadorapps.data.dao

import android.content.ContentValues
import android.content.Context
import com.example.organizadorapps.InstalledApp
import com.example.organizadorapps.data.LocalAppDatabase

class AppDao(context: Context) {

    private val database = LocalAppDatabase.getInstance(context)

    fun upsertInstalledApps(apps: List<InstalledApp>, hiddenPackages: Set<String>) {
        if (apps.isEmpty()) return

        val now = System.currentTimeMillis()
        val db = database.writableDatabase

        db.beginTransaction()
        try {
            apps.forEach { app ->
                val values = ContentValues().apply {
                    put("package_name", app.packageName)
                    put("name", app.name)
                    put("is_hidden", if (app.packageName in hiddenPackages) 1 else 0)
                    put("is_installed", 1)
                    put("last_seen_at", now)
                    put("updated_at", now)
                }
                db.insertWithOnConflict("apps", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
