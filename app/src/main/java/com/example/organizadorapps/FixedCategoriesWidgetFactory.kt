package com.example.organizadorapps

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService

class FixedCategoriesWidgetFactory(
    private val context: Context,
    private val appWidgetId: Int
) : RemoteViewsService.RemoteViewsFactory {

    private val allSlots = listOf(
        SlotViews(R.id.widgetAppSlot1, R.id.widgetAppIcon1, R.id.widgetAppName1),
        SlotViews(R.id.widgetAppSlot2, R.id.widgetAppIcon2, R.id.widgetAppName2),
        SlotViews(R.id.widgetAppSlot3, R.id.widgetAppIcon3, R.id.widgetAppName3),
        SlotViews(R.id.widgetAppSlot4, R.id.widgetAppIcon4, R.id.widgetAppName4)
    )

    private var selectedCategoryName: String = CategoryWidgetPrefs.getDefaultCategory()
    private var columnCount: Int = 2
    private var rows: List<WidgetAppRow> = emptyList()

    override fun onCreate() = Unit

    override fun onDataSetChanged() {
        selectedCategoryName = CategoryWidgetPrefs.getCategory(context, appWidgetId)
        columnCount = CategoryWidgetPrefs.getColumnCount(context, appWidgetId)

        val apps = AppRepository.getAppsFast(context)
        val selectedCategory = CategorySuggestionEngine
            .categorizeApps(apps, context)
            .firstOrNull { it.name.equals(selectedCategoryName, ignoreCase = true) }

        val categoryApps = selectedCategory?.apps.orEmpty()
        rows = categoryApps.chunked(columnCount).mapIndexed { index, chunk ->
            WidgetAppRow(
                apps = chunk,
                blockIndex = index
            )
        }
    }

    override fun onDestroy() {
        rows = emptyList()
    }

    override fun getCount(): Int = rows.size

    override fun getViewAt(position: Int): RemoteViews {
        val row = rows.getOrNull(position)
            ?: return RemoteViews(context.packageName, R.layout.widget_category_row)

        val views = RemoteViews(context.packageName, R.layout.widget_category_row)

        allSlots.forEachIndexed { index, slot ->
            when {
                index >= columnCount -> {
                    views.setViewVisibility(slot.containerId, View.GONE)
                }
                row.apps.getOrNull(index) == null -> {
                    views.setViewVisibility(slot.containerId, View.INVISIBLE)
                }
                else -> {
                    val app = row.apps[index]
                    views.setViewVisibility(slot.containerId, View.VISIBLE)
                    views.setImageViewBitmap(slot.iconId, WidgetIconUtils.appIconBitmap(context, app))
                    views.setTextViewText(slot.nameId, app.name)

                    val fillInIntent = Intent().apply {
                        action = WidgetAppLaunchReceiver.ACTION_OPEN_APP
                        putExtra(WidgetAppLaunchReceiver.EXTRA_PACKAGE_NAME, app.packageName)
                        putExtra(WidgetAppLaunchReceiver.EXTRA_APP_NAME, app.name)
                    }
                    views.setOnClickFillInIntent(slot.containerId, fillInIntent)
                }
            }
        }

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long {
        val row = rows.getOrNull(position) ?: return position.toLong()
        return "$appWidgetId-$selectedCategoryName-$columnCount-${row.blockIndex}".hashCode().toLong()
    }

    override fun hasStableIds(): Boolean = true

    private data class WidgetAppRow(
        val apps: List<InstalledApp>,
        val blockIndex: Int
    )

    private data class SlotViews(
        val containerId: Int,
        val iconId: Int,
        val nameId: Int
    )
}
