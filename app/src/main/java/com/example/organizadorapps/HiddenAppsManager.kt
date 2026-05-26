package com.example.organizadorapps

import android.content.Context

object HiddenAppsManager {

    private const val PREF_NAME = "organizador_hidden_apps"
    private const val KEY_PACKAGES = "hidden_packages"

    fun hideApp(context: Context, packageName: String) {
        val hidden = getHiddenPackages(context).toMutableSet()
        hidden.add(packageName)
        save(context, hidden)
    }

    fun unhideApp(context: Context, packageName: String) {
        val hidden = getHiddenPackages(context).toMutableSet()
        hidden.remove(packageName)
        save(context, hidden)
    }

    fun isHidden(context: Context, packageName: String): Boolean {
        return getHiddenPackages(context).contains(packageName)
    }

    fun getHiddenPackages(context: Context): Set<String> {
        return context
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_PACKAGES, emptySet())
            ?.filter { it.isNotBlank() }
            ?.toSet()
            .orEmpty()
    }

    private fun save(context: Context, packages: Set<String>) {
        context
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_PACKAGES, packages)
            .apply()
    }
}
