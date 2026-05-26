package com.example.organizadorapps

import android.content.Context

object SmartRecentAppsManager {

    fun getRecentApps(
        context: Context,
        allApps: List<InstalledApp>,
        limit: Int = 4,
        daysBack: Int = 7
    ): List<InstalledApp> {
        val usageStatsApps = if (UsageStatsHelper.hasUsageStatsPermission(context)) {
            UsageStatsHelper.getRecentUsedApps(
                context = context,
                allApps = allApps,
                limit = limit,
                daysBack = daysBack
            )
        } else {
            emptyList()
        }

        if (usageStatsApps.isNotEmpty()) {
            return usageStatsApps
        }

        return getLocalRecentApps(
            context = context,
            allApps = allApps,
            limit = limit
        )
    }

    private fun getLocalRecentApps(
        context: Context,
        allApps: List<InstalledApp>,
        limit: Int
    ): List<InstalledApp> {
        val recentPackages = RecentAppsManager.getRecentPackageNames(context)

        return recentPackages
            .mapNotNull { packageName ->
                allApps.find { it.packageName == packageName }
            }
            .take(limit)
    }
}