package com.example.organizadorapps

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.RemoteViews

class FixedCategoriesWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val columns = resolveColumnCount(newOptions)
        CategoryWidgetPrefs.saveColumnCount(context, appWidgetId, columns)
        updateWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        appWidgetIds.forEach { widgetId ->
            CategoryWidgetPrefs.deleteWidget(context, widgetId)
        }
    }

    companion object {
        const val EXTRA_CATEGORY_TO_OPEN = "com.example.organizadorapps.extra.CATEGORY_TO_OPEN"

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val categoryName = CategoryWidgetPrefs.getCategory(context, appWidgetId)
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            CategoryWidgetPrefs.saveColumnCount(context, appWidgetId, resolveColumnCount(options))

            val views = RemoteViews(context.packageName, R.layout.widget_fixed_categories)
            views.setTextViewText(R.id.widgetHeader, categoryName)

            val serviceIntent = Intent(context, FixedCategoriesWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(
                    "organizadorapps://category-widget/$appWidgetId/" +
                            "${Uri.encode(categoryName)}/${CategoryWidgetPrefs.getColumnCount(context, appWidgetId)}"
                )
            }

            views.setRemoteAdapter(R.id.widgetCategoryList, serviceIntent)
            views.setEmptyView(R.id.widgetCategoryList, R.id.widgetEmptyState)

            val launchTemplate = Intent(context, WidgetAppLaunchReceiver::class.java).apply {
                action = WidgetAppLaunchReceiver.ACTION_OPEN_APP
            }
            val templatePendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                launchTemplate,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.widgetCategoryList, templatePendingIntent)

            val openOrganizerIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_CATEGORY_TO_OPEN, categoryName)
            }
            val openOrganizerPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId + 10_000,
                openOrganizerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetHeader, openOrganizerPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widgetCategoryList)
        }

        private fun resolveColumnCount(options: Bundle?): Int {
            val minWidth = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 0
            return when {
                minWidth >= 260 -> 4
                minWidth >= 180 -> 3
                else -> 2
            }
        }
    }
}
