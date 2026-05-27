package com.example.organizadorapps

import android.content.Context

object CategoryWidgetPrefs {

    private const val PREFS_NAME = "category_widget_prefs"
    private const val KEY_PREFIX_CATEGORY = "category_"
    private const val KEY_PREFIX_COLUMNS = "columns_"
    private const val DEFAULT_CATEGORY = "Billeteras digitales"
    private const val DEFAULT_COLUMNS = 2

    fun getAvailableCategories(): List<String> {
        return CategoryRules.rules.keys.toList()
    }

    fun getDefaultCategory(): String {
        return if (CategoryRules.rules.containsKey(DEFAULT_CATEGORY)) {
            DEFAULT_CATEGORY
        } else {
            getAvailableCategories().firstOrNull().orEmpty()
        }
    }

    fun saveCategory(context: Context, widgetId: Int, categoryName: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(categoryKey(widgetId), categoryName)
            .apply()
    }

    fun getCategory(context: Context, widgetId: Int): String {
        val saved = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(categoryKey(widgetId), null)
            .orEmpty()

        return saved.ifBlank { getDefaultCategory() }
    }

    fun saveColumnCount(context: Context, widgetId: Int, columnCount: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(columnsKey(widgetId), columnCount.coerceIn(2, 4))
            .apply()
    }

    fun getColumnCount(context: Context, widgetId: Int): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(columnsKey(widgetId), DEFAULT_COLUMNS)
            .coerceIn(2, 4)
    }

    fun deleteWidget(context: Context, widgetId: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(categoryKey(widgetId))
            .remove(columnsKey(widgetId))
            .apply()
    }

    private fun categoryKey(widgetId: Int): String = "$KEY_PREFIX_CATEGORY$widgetId"
    private fun columnsKey(widgetId: Int): String = "$KEY_PREFIX_COLUMNS$widgetId"
}
