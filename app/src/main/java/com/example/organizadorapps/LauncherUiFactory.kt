package com.example.organizadorapps

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes

/**
 * Factory visual compartida por HomeFragment y CompactLauncherActivity.
 *
 * Objetivo:
 * - Mantener el estilo sobrio/dark actual.
 * - Evitar duplicar fondos, títulos, botones y tiles de apps.
 * - No cambiar la lógica funcional de favoritos, recientes, búsqueda ni categorías.
 */
object LauncherUiFactory {

    fun screenRoot(context: Context): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                AppUiUtils.dp(context, UiConstants.HOME_HORIZONTAL_PADDING_DP),
                AppUiUtils.dp(context, UiConstants.HOME_TOP_PADDING_DP),
                AppUiUtils.dp(context, UiConstants.HOME_HORIZONTAL_PADDING_DP),
                AppUiUtils.dp(context, UiConstants.HOME_BOTTOM_PADDING_DP)
            )
        }
    }

    fun compactCard(context: Context): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = AppUiUtils.roundedDrawable(
                context = context,
                color = UiConstants.BACKGROUND,
                radiusDp = UiConstants.COMPACT_CARD_RADIUS_DP,
                strokeColor = UiConstants.BORDER,
                strokeWidthDp = 1
            )
            setPadding(
                AppUiUtils.dp(context, UiConstants.COMPACT_CARD_HORIZONTAL_PADDING_DP),
                AppUiUtils.dp(context, UiConstants.COMPACT_CARD_TOP_PADDING_DP),
                AppUiUtils.dp(context, UiConstants.COMPACT_CARD_HORIZONTAL_PADDING_DP),
                AppUiUtils.dp(context, UiConstants.COMPACT_CARD_BOTTOM_PADDING_DP)
            )
        }
    }

    fun headerTitle(context: Context, title: String): TextView {
        return TextView(context).apply {
            text = title
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(UiConstants.TEXT_PRIMARY)
            includeFontPadding = false
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
    }

    fun iconButton(
        context: Context,
        @DrawableRes iconRes: Int,
        sizeDp: Int = UiConstants.HEADER_BUTTON_WIDTH_DP,
        heightDp: Int = UiConstants.HEADER_BUTTON_HEIGHT_DP,
        iconSizeDp: Int = UiConstants.HEADER_ICON_SIZE_DP,
        onClick: () -> Unit
    ): FrameLayout {
        return FrameLayout(context).apply {
            background = pillBackground(context)
            isClickable = true
            isFocusable = true

            layoutParams = LinearLayout.LayoutParams(
                AppUiUtils.dp(context, sizeDp),
                AppUiUtils.dp(context, heightDp)
            )

            addView(
                ImageView(context).apply {
                    setImageResource(iconRes)
                    imageTintList = ColorStateList.valueOf(UiConstants.TEXT_PRIMARY)
                    scaleType = ImageView.ScaleType.CENTER
                },
                FrameLayout.LayoutParams(
                    AppUiUtils.dp(context, iconSizeDp),
                    AppUiUtils.dp(context, iconSizeDp),
                    Gravity.CENTER
                )
            )

            setOnClickListener {
                AnimationUtils.press(this) { onClick() }
            }
        }
    }

    fun compactSearchButton(
        context: Context,
        textValue: String,
        onClick: () -> Unit
    ): TextView {
        return TextView(context).apply {
            text = textValue
            textSize = 13f
            gravity = Gravity.CENTER_VERTICAL
            setTextColor(UiConstants.TEXT_SECONDARY)
            background = pillBackground(context)
            isClickable = true
            isFocusable = true
            includeFontPadding = false
            setPadding(AppUiUtils.dp(context, 14), 0, AppUiUtils.dp(context, 14), 0)

            layoutParams = LinearLayout.LayoutParams(
                0,
                AppUiUtils.dp(context, UiConstants.HEADER_BUTTON_HEIGHT_DP),
                1f
            ).apply {
                marginEnd = AppUiUtils.dp(context, 10)
            }

            setOnClickListener {
                AnimationUtils.press(this) { onClick() }
            }
        }
    }

    fun sectionTitle(
        context: Context,
        textValue: String,
        topPaddingDp: Int = 12,
        bottomPaddingDp: Int = 10
    ): TextView {
        return TextView(context).apply {
            text = textValue
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(UiConstants.TEXT_PRIMARY)
            includeFontPadding = false
            setPadding(
                0,
                AppUiUtils.dp(context, topPaddingDp),
                0,
                AppUiUtils.dp(context, bottomPaddingDp)
            )
        }
    }

    fun sectionHeader(
        context: Context,
        title: String,
        actionText: String? = null,
        topPaddingDp: Int = 12,
        bottomPaddingDp: Int = 10,
        onActionClick: (() -> Unit)? = null
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, AppUiUtils.dp(context, topPaddingDp), 0, AppUiUtils.dp(context, bottomPaddingDp))

            addView(
                TextView(context).apply {
                    text = title
                    textSize = 18f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(UiConstants.TEXT_PRIMARY)
                    includeFontPadding = false
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }
            )

            if (actionText != null && onActionClick != null) {
                addView(
                    TextView(context).apply {
                        text = actionText
                        textSize = 14f
                        typeface = Typeface.DEFAULT_BOLD
                        setTextColor(UiConstants.ACCENT)
                        includeFontPadding = false
                        gravity = Gravity.CENTER
                        setPadding(
                            AppUiUtils.dp(context, 12),
                            AppUiUtils.dp(context, 6),
                            0,
                            AppUiUtils.dp(context, 6)
                        )
                        setOnClickListener { onActionClick() }
                    }
                )
            }
        }
    }

    fun emptyText(
        context: Context,
        textValue: String,
        bottomPaddingDp: Int = 8
    ): TextView {
        return TextView(context).apply {
            text = textValue
            textSize = 13f
            setTextColor(UiConstants.TEXT_SECONDARY)
            includeFontPadding = false
            setPadding(0, AppUiUtils.dp(context, 4), 0, AppUiUtils.dp(context, bottomPaddingDp))
        }
    }

    fun compactAppIcon(
        context: Context,
        app: InstalledApp,
        useWeight: Boolean = true,
        closeAfterLaunch: (() -> Unit)? = null,
        onAppLongPress: ((InstalledApp) -> Unit)? = null
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true

            layoutParams = if (useWeight) {
                LinearLayout.LayoutParams(
                    0,
                    AppUiUtils.dp(context, UiConstants.COMPACT_APP_TILE_HEIGHT_DP),
                    1f
                )
            } else {
                LinearLayout.LayoutParams(
                    AppUiUtils.dp(context, 72),
                    AppUiUtils.dp(context, UiConstants.COMPACT_APP_TILE_HEIGHT_DP)
                ).apply {
                    marginEnd = AppUiUtils.dp(context, 6)
                }
            }

            setOnClickListener {
                AnimationUtils.press(this) {
                    RecentAppsManager.registerAppOpen(context, app)
                    AppLauncher.openApp(context, app.packageName, app.name)
                    closeAfterLaunch?.invoke()
                }
            }

            setOnLongClickListener {
                onAppLongPress?.invoke(app)
                onAppLongPress != null
            }

            addView(
                ImageView(context).apply {
                    setImageDrawable(app.icon)
                    layoutParams = LinearLayout.LayoutParams(
                        AppUiUtils.dp(context, UiConstants.COMPACT_APP_ICON_SIZE_DP),
                        AppUiUtils.dp(context, UiConstants.COMPACT_APP_ICON_SIZE_DP)
                    )
                }
            )

            addView(
                TextView(context).apply {
                    text = app.name
                    textSize = 10f
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    gravity = Gravity.CENTER
                    setTextColor(UiConstants.TEXT_PRIMARY)
                    includeFontPadding = false
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = AppUiUtils.dp(context, 4)
                    }
                }
            )
        }
    }

    fun categoryFolderBackground(context: Context): GradientDrawable {
        return AppUiUtils.roundedDrawable(
            context = context,
            color = UiConstants.SURFACE,
            radiusDp = UiConstants.CATEGORY_FOLDER_RADIUS_DP,
            strokeColor = UiConstants.BORDER,
            strokeWidthDp = 1
        )
    }

    fun categoryExpandedBackground(context: Context): GradientDrawable {
        return AppUiUtils.roundedDrawable(
            context = context,
            color = UiConstants.SURFACE_ALT,
            radiusDp = UiConstants.CATEGORY_PANEL_RADIUS_DP,
            strokeColor = UiConstants.BORDER,
            strokeWidthDp = 1
        )
    }

    fun pillBackground(context: Context): GradientDrawable {
        return AppUiUtils.roundedDrawable(
            context = context,
            color = UiConstants.SURFACE_ALT,
            radiusDp = UiConstants.PILL_RADIUS_DP,
            strokeColor = UiConstants.BORDER,
            strokeWidthDp = 1
        )
    }

    fun fillPreviewIcons(
        context: Context,
        grid: GridLayout,
        apps: List<InstalledApp>,
        iconSizeDp: Int
    ) {
        grid.removeAllViews()

        apps.take(4).forEach { app ->
            grid.addView(
                ImageView(context).apply {
                    setImageDrawable(app.icon)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    layoutParams = ViewGroup.MarginLayoutParams(
                        AppUiUtils.dp(context, iconSizeDp),
                        AppUiUtils.dp(context, iconSizeDp)
                    ).apply {
                        setMargins(
                            AppUiUtils.dp(context, 2),
                            AppUiUtils.dp(context, 2),
                            AppUiUtils.dp(context, 2),
                            AppUiUtils.dp(context, 2)
                        )
                    }
                }
            )
        }
    }

    fun compactAppRow(
        context: Context,
        apps: List<InstalledApp>,
        closeAfterLaunch: (() -> Unit)?,
        onAppLongPress: ((InstalledApp) -> Unit)? = null
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START or Gravity.CENTER_VERTICAL

            if (apps.isEmpty()) {
                addView(emptyText(context, "Sin apps aún"))
            } else {
                val useWeightedItems = apps.size >= UiConstants.COMPACT_COLUMNS
                apps.forEach { app ->
                    addView(compactAppIcon(context, app, useWeightedItems, closeAfterLaunch, onAppLongPress))
                }
            }
        }
    }

    fun expandedAppsGrid(
        context: Context,
        apps: List<InstalledApp>,
        columns: Int,
        iconSizeDp: Int = 34,
        tileHeightDp: Int = 78,
        horizontalGapDp: Int = 6,
        bottomGapDp: Int = 8,
        closeAfterLaunch: (() -> Unit)? = null,
        onAppLongPress: ((InstalledApp) -> Unit)? = null,
        onAppClick: (InstalledApp) -> Unit
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            apps.chunked(columns).forEach { rowApps ->
                addView(
                    LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.START or Gravity.CENTER_VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            bottomMargin = AppUiUtils.dp(context, bottomGapDp)
                        }

                        rowApps.forEachIndexed { index, app ->
                            addView(
                                searchAppTile(
                                    context = context,
                                    app = app,
                                    iconSizeDp = iconSizeDp,
                                    tileHeightDp = tileHeightDp,
                                    closeAfterLaunch = closeAfterLaunch,
                                    onAppLongPress = onAppLongPress,
                                    onAppClick = onAppClick
                                ).apply {
                                    layoutParams = LinearLayout.LayoutParams(
                                        0,
                                        AppUiUtils.dp(context, tileHeightDp),
                                        1f
                                    ).apply {
                                        if (index < columns - 1) {
                                            marginEnd = AppUiUtils.dp(context, horizontalGapDp)
                                        }
                                    }
                                }
                            )
                        }

                        repeat(columns - rowApps.size) { spacerIndex ->
                            addView(
                                View(context).apply {
                                    layoutParams = LinearLayout.LayoutParams(0, 1, 1f).apply {
                                        if (rowApps.size + spacerIndex < columns - 1) {
                                            marginEnd = AppUiUtils.dp(context, horizontalGapDp)
                                        }
                                    }
                                }
                            )
                        }
                    }
                )
            }
        }
    }

    private fun searchAppTile(
        context: Context,
        app: InstalledApp,
        iconSizeDp: Int,
        tileHeightDp: Int,
        closeAfterLaunch: (() -> Unit)?,
        onAppLongPress: ((InstalledApp) -> Unit)?,
        onAppClick: (InstalledApp) -> Unit
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            setPadding(
                AppUiUtils.dp(context, 4),
                AppUiUtils.dp(context, 5),
                AppUiUtils.dp(context, 4),
                AppUiUtils.dp(context, 5)
            )

            setOnClickListener {
                AnimationUtils.press(this) {
                    onAppClick(app)
                    closeAfterLaunch?.invoke()
                }
            }

            setOnLongClickListener {
                onAppLongPress?.invoke(app)
                onAppLongPress != null
            }

            addView(
                ImageView(context).apply {
                    setImageDrawable(app.icon)
                    layoutParams = LinearLayout.LayoutParams(
                        AppUiUtils.dp(context, iconSizeDp),
                        AppUiUtils.dp(context, iconSizeDp)
                    )
                }
            )

            addView(
                TextView(context).apply {
                    text = app.name
                    textSize = 10.5f
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    gravity = Gravity.CENTER
                    setTextColor(UiConstants.TEXT_PRIMARY)
                    includeFontPadding = false
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = AppUiUtils.dp(context, 5)
                    }
                }
            )
        }
    }

    fun spacer(context: Context, heightDp: Int): View {
        return View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                AppUiUtils.dp(context, heightDp)
            )
        }
    }
}
