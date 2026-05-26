package com.example.organizadorapps

import android.content.Context

object FavoritesManager {

    private const val PREF_NAME = "organizador_favorites"
    private const val KEY_PACKAGES = "favorite_packages"
    private const val KEY_PACKAGES_ORDERED = "favorite_packages_ordered"
    private const val SEPARATOR = "|"

    fun addFavorite(context: Context, packageName: String) {
        val favorites = getFavoritePackageList(context).toMutableList()

        if (packageName !in favorites && favorites.size < UiConstants.MAX_FAVORITES) {
            favorites.add(packageName)
            saveFavoritePackageList(context, favorites)
        }
    }

    fun removeFavorite(context: Context, packageName: String) {
        val favorites = getFavoritePackageList(context)
            .filterNot { it == packageName }

        saveFavoritePackageList(context, favorites)
    }

    fun toggleFavorite(context: Context, packageName: String): Boolean {
        return if (isFavorite(context, packageName)) {
            removeFavorite(context, packageName)
            false
        } else {
            addFavorite(context, packageName)
            true
        }
    }

    fun isFavorite(context: Context, packageName: String): Boolean {
        return getFavoritePackageList(context).contains(packageName)
    }

    fun getFavoritePackages(context: Context): Set<String> {
        return getFavoritePackageList(context).toSet()
    }

    fun getFavoritePackageList(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val orderedValue = prefs.getString(KEY_PACKAGES_ORDERED, null)

        if (!orderedValue.isNullOrBlank()) {
            return orderedValue
                .split(SEPARATOR)
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()
        }

        val legacyFavorites = prefs.getStringSet(KEY_PACKAGES, emptySet())
            ?.filter { it.isNotBlank() }
            ?.distinct()
            .orEmpty()

        if (legacyFavorites.isNotEmpty()) {
            saveFavoritePackageList(context, legacyFavorites)
        }

        return legacyFavorites
    }

    fun replaceFavorites(context: Context, favorites: List<String>) {
        saveFavoritePackageList(context, favorites)
    }

    private fun saveFavoritePackageList(context: Context, favorites: List<String>) {
        val cleanFavorites = favorites
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(UiConstants.MAX_FAVORITES)

        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        prefs.edit()
            .putString(KEY_PACKAGES_ORDERED, cleanFavorites.joinToString(SEPARATOR))
            .putStringSet(KEY_PACKAGES, cleanFavorites.toSet())
            .apply()
    }
}