package com.example.organizadorapps.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Primera base local del organizador.
 *
 * Se usa SQLiteOpenHelper en vez de Room para evitar tocar Gradle en esta primera fase.
 * Más adelante se puede migrar a Room manteniendo las mismas tablas.
 */
class LocalAppDatabase private constructor(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE apps (
                package_name TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                is_hidden INTEGER NOT NULL DEFAULT 0,
                is_installed INTEGER NOT NULL DEFAULT 1,
                last_seen_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE app_launch_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                package_name TEXT NOT NULL,
                app_name TEXT NOT NULL,
                opened_at INTEGER NOT NULL,
                source TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE app_usage_snapshot (
                package_name TEXT PRIMARY KEY,
                app_name TEXT NOT NULL,
                last_used_at INTEGER NOT NULL,
                source TEXT NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL("CREATE INDEX idx_launch_history_opened_at ON app_launch_history(opened_at DESC)")
        db.execSQL("CREATE INDEX idx_launch_history_package ON app_launch_history(package_name)")
        db.execSQL("CREATE INDEX idx_usage_snapshot_last_used ON app_usage_snapshot(last_used_at DESC)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Primera versión. Las siguientes migraciones deben ser incrementales.
    }

    companion object {
        private const val DATABASE_NAME = "organizador_apps.db"
        private const val DATABASE_VERSION = 1

        @Volatile
        private var INSTANCE: LocalAppDatabase? = null

        fun getInstance(context: Context): LocalAppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocalAppDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
