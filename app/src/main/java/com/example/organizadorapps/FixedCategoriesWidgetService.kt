package com.example.organizadorapps

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.widget.RemoteViewsService

class FixedCategoriesWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        return FixedCategoriesWidgetFactory(applicationContext, widgetId)
    }
}
