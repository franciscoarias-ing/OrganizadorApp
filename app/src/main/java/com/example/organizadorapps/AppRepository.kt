package com.example.organizadorapps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

object AppRepository {

    fun getInstalledLaunchableApps(context: Context): List<InstalledApp> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return context.packageManager
            .queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .map {
                InstalledApp(
                    name = it.loadLabel(context.packageManager).toString(),
                    packageName = it.activityInfo.packageName,
                    icon = it.loadIcon(context.packageManager)
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.name.lowercase() }
    }
}