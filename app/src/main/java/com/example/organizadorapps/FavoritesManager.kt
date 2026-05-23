package com.example.organizadorapps

import android.content.Context

object FavoritesManager {

    private const val PREF_NAME = "organizador_favorites"
    private const val KEY_PACKAGES = "favorite_packages"

    fun addFavorite(context: Context, packageName: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val favorites = prefs.getStringSet(KEY_PACKAGES, emptySet())?.toMutableSet() ?: mutableSetOf()

        favorites.add(packageName)

        prefs.edit()
            .putStringSet(KEY_PACKAGES, favorites)
            .apply()
    }

    fun removeFavorite(context: Context, packageName: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val favorites = prefs.getStringSet(KEY_PACKAGES, emptySet())?.toMutableSet() ?: mutableSetOf()

        favorites.remove(packageName)

        prefs.edit()
            .putStringSet(KEY_PACKAGES, favorites)
            .apply()
    }

    fun isFavorite(context: Context, packageName: String): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_PACKAGES, emptySet())?.contains(packageName) == true
    }

    fun getFavoritePackages(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_PACKAGES, emptySet()) ?: emptySet()
    }
}