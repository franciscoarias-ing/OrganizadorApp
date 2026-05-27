package com.example.organizadorapps

object CategorySuggestionEngine {

    fun categorizeApps(apps: List<InstalledApp>): List<AppCategory> {
        val matchedPackages = mutableSetOf<String>()

        val categories = CategoryRules.rules.map { (categoryName, keywords) ->
            val matchedApps = apps
                .filter { app -> app.matchesAnyKeyword(keywords) }
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

    private fun InstalledApp.matchesAnyKeyword(keywords: List<String>): Boolean {
        val searchable = "${name} ${packageName}".lowercase()
        return keywords.any { keyword ->
            val normalized = keyword.lowercase().trim()
            normalized.isNotBlank() && searchable.contains(normalized)
        }
    }
}
