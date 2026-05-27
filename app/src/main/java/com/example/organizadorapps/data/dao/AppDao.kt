package com.example.organizadorapps.data.dao

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.organizadorapps.IconCacheManager
import com.example.organizadorapps.InstalledApp
import com.example.organizadorapps.data.LocalAppDatabase

class AppDao(context: Context) {

    private val appContext = context.applicationContext
    private val database = LocalAppDatabase.getInstance(appContext)

    fun upsertInstalledApps(apps: List<InstalledApp>, hiddenPackages: Set<String>) {
        if (apps.isEmpty()) return

        val now = System.currentTimeMillis()
        val db = database.writableDatabase
        val currentPackages = apps.map { it.packageName }.toSet()

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
                db.insertWithOnConflict(
                    "apps",
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
                IconCacheManager.putIcon(app.packageName, app.icon)
            }

            // Conservador: marcamos como no instaladas solo las que ya conocíamos y no vinieron del PackageManager.
            val args = currentPackages.toTypedArray()
            if (args.isNotEmpty()) {
                val placeholders = args.joinToString(",") { "?" }
                db.execSQL(
                    "UPDATE apps SET is_installed = 0, updated_at = ? WHERE package_name NOT IN ($placeholders)",
                    arrayOf(now.toString(), *args)
                )
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getInstalledApps(): List<InstalledApp> {
        val db = database.readableDatabase
        val apps = mutableListOf<InstalledApp>()
        val packageManager = appContext.packageManager

        val cursor = db.query(
            "apps",
            arrayOf("package_name", "name"),
            "is_installed = 1",
            null,
            null,
            null,
            "name COLLATE NOCASE ASC"
        )

        cursor.use {
            val packageIndex = it.getColumnIndexOrThrow("package_name")
            val nameIndex = it.getColumnIndexOrThrow("name")

            while (it.moveToNext()) {
                val packageName = it.getString(packageIndex)
                val name = it.getString(nameIndex)
                val hasLaunchIntent = packageManager.getLaunchIntentForPackage(packageName) != null
                val icon = IconCacheManager.getIcon(appContext, packageName)

                if (hasLaunchIntent && icon != null) {
                    apps.add(
                        InstalledApp(
                            name = name,
                            packageName = packageName,
                            icon = icon
                        )
                    )
                }
            }
        }

        return apps.distinctBy { it.packageName }.sortedBy { it.name.lowercase() }
    }
}
