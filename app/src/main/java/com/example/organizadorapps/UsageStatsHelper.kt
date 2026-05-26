package com.example.organizadorapps

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import java.util.Calendar

object UsageStatsHelper {

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )

        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openUsageAccessSettings(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    }

    fun getRecentUsedApps(
        context: Context,
        allApps: List<InstalledApp>,
        limit: Int = 4,
        daysBack: Int = 7
    ): List<InstalledApp> {
        if (!hasUsageStatsPermission(context)) {
            return emptyList()
        }

        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, -daysBack)
        val startTime = calendar.timeInMillis

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        if (usageStatsList.isNullOrEmpty()) {
            return emptyList()
        }

        val launchablePackages = allApps.map { it.packageName }.toSet()

        val recentPackages = usageStatsList
            .filter { usage ->
                usage.packageName in launchablePackages &&
                        usage.lastTimeUsed > 0 &&
                        usage.totalTimeInForeground > 0
            }
            .groupBy { it.packageName }
            .mapValues { (_, stats) ->
                stats.maxOf { it.lastTimeUsed }
            }
            .entries
            .sortedByDescending { it.value }
            .map { it.key }
            .take(limit)

        return recentPackages.mapNotNull { packageName ->
            allApps.find { it.packageName == packageName }
        }
    }

    fun getMostUsedApps(
        context: Context,
        allApps: List<InstalledApp>,
        limit: Int = 4,
        daysBack: Int = 7
    ): List<InstalledApp> {
        if (!hasUsageStatsPermission(context)) {
            return emptyList()
        }

        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, -daysBack)
        val startTime = calendar.timeInMillis

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        if (usageStatsList.isNullOrEmpty()) {
            return emptyList()
        }

        val launchablePackages = allApps.map { it.packageName }.toSet()

        val mostUsedPackages = usageStatsList
            .filter { usage ->
                usage.packageName in launchablePackages &&
                        usage.totalTimeInForeground > 0
            }
            .groupBy { it.packageName }
            .mapValues { (_, stats) ->
                stats.sumOf { it.totalTimeInForeground }
            }
            .entries
            .sortedByDescending { it.value }
            .map { it.key }
            .take(limit)

        return mostUsedPackages.mapNotNull { packageName ->
            allApps.find { it.packageName == packageName }
        }
    }
}