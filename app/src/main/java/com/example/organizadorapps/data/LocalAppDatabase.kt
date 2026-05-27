package com.example.organizadorapps.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Base local del organizador.
 *
 * Se usa SQLiteOpenHelper para mantener el proyecto sin dependencias nuevas.
 */
class LocalAppDatabase private constructor(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {
        createAppsTable(db)
        createLaunchHistoryTable(db)
        createUsageSnapshotTable(db)
        createUsageDailyTable(db)
        createIndexes(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            createUsageDailyTable(db)
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_usage_daily_package_date ON app_usage_daily(package_name, usage_date)")
        }
    }

    private fun createAppsTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS apps (
                package_name TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                is_hidden INTEGER NOT NULL DEFAULT 0,
                is_installed INTEGER NOT NULL DEFAULT 1,
                last_seen_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    private fun createLaunchHistoryTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS app_launch_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                package_name TEXT NOT NULL,
                app_name TEXT NOT NULL,
                opened_at INTEGER NOT NULL,
                source TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    private fun createUsageSnapshotTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS app_usage_snapshot (
                package_name TEXT PRIMARY KEY,
                app_name TEXT NOT NULL,
                last_used_at INTEGER NOT NULL,
                source TEXT NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    private fun createUsageDailyTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS app_usage_daily (
                package_name TEXT NOT NULL,
                app_name TEXT NOT NULL,
                usage_date TEXT NOT NULL,
                total_usage_ms INTEGER NOT NULL DEFAULT 0,
                open_count INTEGER NOT NULL DEFAULT 0,
                source TEXT NOT NULL,
                updated_at INTEGER NOT NULL,
                PRIMARY KEY(package_name, usage_date, source)
            )
            """.trimIndent()
        )
    }

    private fun createIndexes(db: SQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_launch_history_opened_at ON app_launch_history(opened_at DESC)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_launch_history_package ON app_launch_history(package_name)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_usage_snapshot_last_used ON app_usage_snapshot(last_used_at DESC)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_usage_daily_package_date ON app_usage_daily(package_name, usage_date)")
    }

    companion object {
        private const val DATABASE_NAME = "organizador_apps.db"
        private const val DATABASE_VERSION = 2

        @Volatile
        private var INSTANCE: LocalAppDatabase? = null

        fun getInstance(context: Context): LocalAppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocalAppDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

