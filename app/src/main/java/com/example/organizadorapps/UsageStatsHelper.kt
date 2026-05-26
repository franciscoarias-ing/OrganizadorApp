package com.example.organizadorapps

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

data class DeviceUsageApp(
    val app: InstalledApp,
    val totalTimeMs: Long,
    val lastTimeUsed: Long,
    val openCount: Int
)

data class RecentUsageEventItem(
    val app: InstalledApp,
    val time: Long,
    val durationMs: Long
)

data class DeviceUsageSummary(
    val periodLabel: String,
    val totalUsageMs: Long,
    val previousUsageMs: Long,
    val usageDeltaPercent: Int?,
    val openedAppsCount: Int,
    val backgroundAppsCount: Int,
    val unlockCount: Int,
    val unlocksPerHourText: String,
    val intenseHourLabel: String,
    val intenseHourUsageMs: Long,
    val mostUsedApps: List<DeviceUsageApp>,
    val recentEvents: List<RecentUsageEventItem>,
    val topApp: DeviceUsageApp?,
    val socialUsageMs: Long,
    val productivityUsageMs: Long,
    val unusedApps: List<InstalledApp>,
    val totalOpenCount: Int,
    val averageUsagePerOpenedAppMs: Long,
    val mostOpenedApp: DeviceUsageApp?
)

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
        context.startActivity(
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun buildSummary(context: Context, allApps: List<InstalledApp>, daysBack: Int): DeviceUsageSummary? {
        if (!hasUsageStatsPermission(context)) return null

        val period = getPeriodBounds(daysBack)
        val previousPeriod = getPreviousPeriodBounds(daysBack)
        val launchableByPackage = allApps.associateBy { it.packageName }
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            period.first,
            period.second
        ).orEmpty()

        val previousUsageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            previousPeriod.first,
            previousPeriod.second
        ).orEmpty()

        val apps = aggregateUsageApps(usageStats, launchableByPackage)
        val previousTotal = previousUsageStats.sumOfLaunchableTime(launchableByPackage.keys)
        val events = queryEvents(usageStatsManager, period.first, period.second, launchableByPackage)

        val appsWithOpenCount = apps.map { item ->
            item.copy(openCount = events.foregroundCounts[item.app.packageName] ?: item.openCount)
        }

        val total = appsWithOpenCount.sumOf { it.totalTimeMs }
        val totalOpenCount = events.foregroundCounts.values.sum()
        val averageUsage = if (appsWithOpenCount.isNotEmpty()) total / appsWithOpenCount.size else 0L
        val usedPackages = appsWithOpenCount.map { it.app.packageName }.toSet() + events.foregroundPackages
        val unusedApps = allApps
            .filterNot { it.packageName in usedPackages }
            .sortedBy { it.name.lowercase(Locale.getDefault()) }

        val hourUsage = usageStats
            .filter { it.packageName in launchableByPackage.keys && it.totalTimeInForeground > 0L }
            .groupBy {
                Calendar.getInstance().apply { timeInMillis = it.lastTimeUsed }.get(Calendar.HOUR_OF_DAY)
            }
            .mapValues { (_, values) -> values.sumOf { it.totalTimeInForeground } }

        val intenseHour = hourUsage.maxByOrNull { it.value }
        val unlocksPerHour = if (events.unlockCount > 0 && period.activeHours > 0) {
            "${(events.unlockCount.toDouble() / period.activeHours).roundToInt()} cada hora aprox."
        } else {
            "Sin dato suficiente"
        }

        val delta = if (previousTotal > 0L) {
            (((total - previousTotal).toDouble() / previousTotal.toDouble()) * 100).roundToInt()
        } else null

        val socialMs = appsWithOpenCount.filter { isSocialApp(it.app) }.sumOf { it.totalTimeMs }
        val productivityMs = appsWithOpenCount.filter { isProductivityApp(it.app) }.sumOf { it.totalTimeMs }
        val sortedByUsage = appsWithOpenCount.sortedByDescending { it.totalTimeMs }

        return DeviceUsageSummary(
            periodLabel = when (daysBack) {
                1 -> "Hoy"
                7 -> "7 días"
                else -> "$daysBack días"
            },
            totalUsageMs = total,
            previousUsageMs = previousTotal,
            usageDeltaPercent = delta,
            openedAppsCount = events.foregroundPackages.size,
            backgroundAppsCount = events.backgroundPackages.size,
            unlockCount = events.unlockCount,
            unlocksPerHourText = unlocksPerHour,
            intenseHourLabel = intenseHour?.key?.let { formatHour(it) } ?: "—",
            intenseHourUsageMs = intenseHour?.value ?: 0L,
            mostUsedApps = sortedByUsage.take(5),
            recentEvents = events.recentEvents.take(6),
            topApp = sortedByUsage.firstOrNull(),
            socialUsageMs = socialMs,
            productivityUsageMs = productivityMs,
            unusedApps = unusedApps.take(8),
            totalOpenCount = totalOpenCount,
            averageUsagePerOpenedAppMs = averageUsage,
            mostOpenedApp = appsWithOpenCount.maxByOrNull { it.openCount }
        )
    }

    fun formatDuration(ms: Long): String {
        val totalMinutes = (ms / 60000L).toInt()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "$hours h $minutes min"
            hours > 0 -> "$hours h"
            minutes > 0 -> "$minutes min"
            else -> "0 min"
        }
    }

    fun formatCompactDuration(ms: Long): String {
        val totalMinutes = (ms / 60000L).toInt()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            minutes > 0 -> "${minutes}m"
            else -> "0m"
        }
    }

    fun formatClock(timeMs: Long): String {
        if (timeMs <= 0L) return "—"
        return SimpleDateFormat("h:mm a", Locale.getDefault())
            .format(timeMs)
            .lowercase(Locale.getDefault())
            .replace("a. m.", "a. m.")
            .replace("p. m.", "p. m.")
    }

    private fun aggregateUsageApps(
        usageStats: List<UsageStats>,
        launchableByPackage: Map<String, InstalledApp>
    ): List<DeviceUsageApp> {
        return usageStats
            .filter { it.packageName in launchableByPackage.keys && it.totalTimeInForeground > 0L }
            .groupBy { it.packageName }
            .mapNotNull { (packageName, stats) ->
                val app = launchableByPackage[packageName] ?: return@mapNotNull null
                DeviceUsageApp(
                    app = app,
                    totalTimeMs = stats.sumOf { it.totalTimeInForeground },
                    lastTimeUsed = stats.maxOf { it.lastTimeUsed },
                    openCount = 0
                )
            }
    }

    private fun queryEvents(
        manager: UsageStatsManager,
        startTime: Long,
        endTime: Long,
        launchableByPackage: Map<String, InstalledApp>
    ): EventAggregation {
        val events = manager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        val foregroundPackages = mutableSetOf<String>()
        val backgroundPackages = mutableSetOf<String>()
        val foregroundCounts = mutableMapOf<String, Int>()
        val unlockTimes = mutableListOf<Long>()
        val recent = mutableListOf<RecentUsageEventItem>()
        val foregroundStartByPackage = mutableMapOf<String, Long>()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val packageName = event.packageName ?: continue
            val app = launchableByPackage[packageName]

            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    if (app != null) {
                        foregroundPackages.add(packageName)
                        foregroundCounts[packageName] = (foregroundCounts[packageName] ?: 0) + 1
                        foregroundStartByPackage[packageName] = event.timeStamp
                        recent.add(RecentUsageEventItem(app, event.timeStamp, 0L))
                    }
                }
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    if (app != null) {
                        backgroundPackages.add(packageName)
                        val start = foregroundStartByPackage.remove(packageName)
                        if (start != null) {
                            val duration = (event.timeStamp - start).coerceAtLeast(0L)
                            val index = recent.indexOfLast { it.app.packageName == packageName && it.durationMs == 0L }
                            if (index >= 0) recent[index] = recent[index].copy(durationMs = duration)
                        }
                    }
                }
                else -> if (isUnlockEvent(event.eventType)) unlockTimes.add(event.timeStamp)
            }
        }

        return EventAggregation(
            foregroundPackages = foregroundPackages,
            backgroundPackages = backgroundPackages,
            foregroundCounts = foregroundCounts,
            unlockCount = unlockTimes.size,
            recentEvents = recent.sortedByDescending { it.time }
        )
    }

    private fun isUnlockEvent(eventType: Int): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            eventType == UsageEvents.Event.KEYGUARD_HIDDEN || eventType == UsageEvents.Event.SCREEN_INTERACTIVE
        } else false
    }

    private fun List<UsageStats>.sumOfLaunchableTime(launchablePackages: Set<String>): Long {
        return filter { it.packageName in launchablePackages }.sumOf { it.totalTimeInForeground }
    }

    private fun getPeriodBounds(daysBack: Int): PeriodBounds {
        val end = Calendar.getInstance()
        val start = Calendar.getInstance()
        if (daysBack <= 1) {
            start.set(Calendar.HOUR_OF_DAY, 0)
        } else {
            start.add(Calendar.DAY_OF_YEAR, -daysBack + 1)
            start.set(Calendar.HOUR_OF_DAY, 0)
        }
        start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0)
        start.set(Calendar.MILLISECOND, 0)
        return PeriodBounds(start.timeInMillis, end.timeInMillis)
    }

    private fun getPreviousPeriodBounds(daysBack: Int): Pair<Long, Long> {
        val current = getPeriodBounds(daysBack)
        val duration = current.second - current.first
        return (current.first - duration) to current.first
    }

    private fun formatHour(hour: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
        }
        return SimpleDateFormat("h:mm a", Locale.getDefault())
            .format(calendar.time)
            .lowercase(Locale.getDefault())
            .replace("a. m.", "a. m.")
            .replace("p. m.", "p. m.")
    }

    private fun isSocialApp(app: InstalledApp): Boolean {
        val text = "${app.name} ${app.packageName}".lowercase(Locale.getDefault())
        return listOf("whatsapp", "facebook", "instagram", "messenger", "telegram", "tiktok", "twitter", "snapchat")
            .any { text.contains(it) }
    }

    private fun isProductivityApp(app: InstalledApp): Boolean {
        val text = "${app.name} ${app.packageName}".lowercase(Locale.getDefault())
        return listOf("drive", "docs", "sheets", "calendar", "gmail", "notion", "office", "word", "excel", "teams", "slack", "keep", "trello")
            .any { text.contains(it) }
    }

    private data class EventAggregation(
        val foregroundPackages: Set<String>,
        val backgroundPackages: Set<String>,
        val foregroundCounts: Map<String, Int>,
        val unlockCount: Int,
        val recentEvents: List<RecentUsageEventItem>
    )

    private data class PeriodBounds(val first: Long, val second: Long) {
        val activeHours: Int get() = ((second - first) / 3600000L).toInt().coerceAtLeast(1)
    }

    fun getRecentUsedApps(context: Context, allApps: List<InstalledApp>, limit: Int = 4, daysBack: Int = 7): List<InstalledApp> {
        return buildSummary(context, allApps, daysBack)?.recentEvents
            ?.map { it.app }
            ?.distinctBy { it.packageName }
            ?.take(limit)
            .orEmpty()
    }

    fun getMostUsedApps(context: Context, allApps: List<InstalledApp>, limit: Int = 4, daysBack: Int = 7): List<InstalledApp> {
        return buildSummary(context, allApps, daysBack)?.mostUsedApps
            ?.map { it.app }
            ?.take(limit)
            .orEmpty()
    }
}
