package com.example.organizadorapps

import android.content.Context

object FavoritesManager {

    private const val PREF_NAME = "organizador_favorites"
    private const val KEY_PACKAGES = "favorite_packages"

    fun addFavorite(context: Context, packageName: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val favorites = getFavoritePackages(context).toMutableSet()

        favorites.add(packageName)

        prefs.edit()
            .putStringSet(KEY_PACKAGES, favorites)
            .apply()
    }

    fun removeFavorite(context: Context, packageName: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val favorites = getFavoritePackages(context).toMutableSet()

        favorites.remove(packageName)

        prefs.edit()
            .putStringSet(KEY_PACKAGES, favorites)
            .apply()
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
        return getFavoritePackages(context).contains(packageName)
    }

    fun getFavoritePackages(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_PACKAGES, emptySet()) ?: emptySet()
    }
}