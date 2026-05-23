package com.example.organizadorapps

object CategorySuggestionEngine {

    fun categorizeApps(apps: List<InstalledApp>): List<AppCategory> {

        val categoryMap = linkedMapOf(
            "Social" to listOf("whatsapp", "telegram", "instagram", "facebook", "messenger", "discord"),
            "Google" to listOf("google", "gmail", "maps", "youtube", "drive"),
            "Multimedia" to listOf("spotify", "netflix", "music", "video", "vlc"),
            "Compras" to listOf("amazon", "mercado", "shop", "aliexpress", "temu"),
            "Herramientas" to listOf("files", "calculator", "drive", "settings"),
            "Productividad" to listOf("notion", "keep", "office", "word", "excel", "docs"),
            "Educación" to listOf("classroom", "coursera", "udemy", "duolingo"),
            "Finanzas" to listOf("bcp", "yape", "plin", "paypal", "binance"),
            "Transporte" to listOf("uber", "didi", "cabify", "indrive"),
            "Juegos" to listOf("game", "roblox", "minecraft", "clash")
        )

        val categorizedPackages = mutableSetOf<String>()

        val categories = categoryMap.map { (categoryName, keywords) ->

            val matchedApps = apps.filter { app ->

                val searchable =
                    "${app.name} ${app.packageName}".lowercase()

                val matches = keywords.any { keyword ->
                    searchable.contains(keyword)
                }

                if (matches) {
                    categorizedPackages.add(app.packageName)
                }

                matches
            }

            AppCategory(categoryName, matchedApps)
        }.toMutableList()

        val others = apps.filterNot {
            categorizedPackages.contains(it.packageName)
        }

        categories.add(
            AppCategory("Otros", others)
        )

        return categories
    }
}