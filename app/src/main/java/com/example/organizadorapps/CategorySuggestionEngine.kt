package com.example.organizadorapps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build

object CategorySuggestionEngine {

    fun categorizeApps(apps: List<InstalledApp>, context: Context? = null): List<AppCategory> {
        val matchedPackages = mutableSetOf<String>()

        val categories = CategoryRules.rules.map { (categoryName, keywords) ->
            val matchedApps = apps
                .filter { app -> app.matchesCategory(context, categoryName, keywords) }
                .distinctBy { it.packageName }
                .sortedBy { it.name.lowercase() }

            matchedPackages.addAll(matchedApps.map { it.packageName })
            AppCategory(categoryName, matchedApps)
        }.toMutableList()

        val others = apps
            .filterNot { it.packageName in matchedPackages }
            .sortedBy { it.name.lowercase() }

        categories.add(AppCategory("Otros", others))
        return categories
    }

    private fun InstalledApp.matchesCategory(
        context: Context?,
        categoryName: String,
        keywords: List<String>
    ): Boolean {
        val searchable = "${name} ${packageName}".lowercase()

        val exclusions = CategoryRules.categoryExclusions[categoryName].orEmpty()
        if (exclusions.any { searchable.contains(it.lowercase()) }) return false

        val ruleMatch = keywords.any { keyword ->
            val normalized = keyword.lowercase().trim()
            normalized.isNotBlank() && searchable.contains(normalized)
        }
        if (ruleMatch) return true

        val androidCategory = context?.let { getAndroidCategoryName(it, packageName) }
        return androidCategory == categoryName
    }

    private fun getAndroidCategoryName(context: Context, packageName: String): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null

        return runCatching {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            mapAndroidCategoryToCategory(appInfo.category)
        }.getOrNull()
    }

    private fun mapAndroidCategoryToCategory(category: Int): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null

        return when (category) {
            ApplicationInfo.CATEGORY_GAME -> "Juegos"
            ApplicationInfo.CATEGORY_AUDIO -> "Música"
            ApplicationInfo.CATEGORY_VIDEO -> "Streaming"
            ApplicationInfo.CATEGORY_IMAGE -> "Fotos y edición"
            ApplicationInfo.CATEGORY_SOCIAL -> "Redes sociales"
            ApplicationInfo.CATEGORY_NEWS -> "Noticias y lectura"
            ApplicationInfo.CATEGORY_MAPS -> "Transporte"
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Productividad"
            else -> null
        }
    }
}
