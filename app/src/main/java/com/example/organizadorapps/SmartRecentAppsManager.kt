package com.example.organizadorapps

import android.content.Context

data class SmartRecentAppItem(
    val app: InstalledApp,
    val lastUsedAt: Long,
    val source: String
)

object SmartRecentAppsManager {

    private const val SOURCE_SYSTEM = "SYSTEM"
    private const val SOURCE_LOCAL = "LOCAL"

    fun getRecentApps(
        context: Context,
        allApps: List<InstalledApp>,
        limit: Int = 4,
        daysBack: Int = 7
    ): List<InstalledApp> {
        return getRecentAppItems(
            context = context,
            allApps = allApps,
            limit = limit,
            daysBack = daysBack
        ).map { it.app }
    }

    fun getRecentAppItems(
        context: Context,
        allApps: List<InstalledApp>,
        limit: Int = 4,
        daysBack: Int = 7
    ): List<SmartRecentAppItem> {
        val allAppsByPackage = allApps.associateBy { it.packageName }
        val merged = mutableMapOf<String, SmartRecentAppItem>()

        if (UsageStatsHelper.hasUsageStatsPermission(context)) {
            UsageStatsHelper.getRecentUsedAppsFast(
                context = context,
                allApps = allApps,
                limit = limit * 2,
                daysBack = daysBack
            ).forEach { item ->
                merged[item.app.packageName] = SmartRecentAppItem(
                    app = item.app,
                    lastUsedAt = item.lastUsedAt,
                    source = SOURCE_SYSTEM
                )
            }
        }

        RecentAppsManager.getRecentPackageNames(context, limit = limit * 2).forEach { packageName ->
            val app = allAppsByPackage[packageName] ?: return@forEach
            val localTime = RecentAppsManager.getLastKnownUsedAt(context, packageName) ?: 0L
            val current = merged[packageName]

            if (current == null || localTime > current.lastUsedAt) {
                merged[packageName] = SmartRecentAppItem(
                    app = app,
                    lastUsedAt = localTime,
                    source = SOURCE_LOCAL
                )
            }
        }

        return merged.values
            .sortedByDescending { it.lastUsedAt }
            .take(limit)
    }
}
