package com.example.organizadorapps

import android.graphics.Color

object UiConstants {
    val BACKGROUND = Color.parseColor("#070C16")
    val SURFACE = Color.parseColor("#111827")
    val SURFACE_ALT = Color.parseColor("#151B26")
    val BORDER = Color.parseColor("#263247")

    val TEXT_PRIMARY = Color.parseColor("#F8FAFC")
    val TEXT_SECONDARY = Color.parseColor("#94A3B8")
    val TEXT_MUTED = Color.parseColor("#7B8496")

    val ACCENT = Color.parseColor("#A855F7")
    val ACCENT_SOFT = Color.parseColor("#271B3F")
    val GOLD = Color.parseColor("#D6A84F")

    const val SCREEN_PADDING = 22
    const val TOP_PADDING = 42

    const val CARD_RADIUS = 32f
    const val SMALL_CARD_RADIUS = 24f

    // Home
    const val HOME_HORIZONTAL_PADDING_DP = 14
    const val HOME_TOP_PADDING_DP = 12
    const val HOME_BOTTOM_PADDING_DP = 12
    const val HOME_CATEGORY_COLUMNS = 4

    // Compact mode
    const val COMPACT_COLUMNS = 3
    // 20% menos ancho que la versión anterior (0.84f -> 0.67f aprox.).
    const val COMPACT_WIDTH_PERCENT = 0.67f
    const val COMPACT_OUTER_HORIZONTAL_PADDING_DP = 12
    const val COMPACT_OUTER_VERTICAL_PADDING_DP = 18
    const val COMPACT_CARD_HORIZONTAL_PADDING_DP = 10
    const val COMPACT_CARD_TOP_PADDING_DP = 12
    const val COMPACT_CARD_BOTTOM_PADDING_DP = 8
    const val COMPACT_CARD_RADIUS_DP = 22
    const val COMPACT_APP_TILE_HEIGHT_DP = 66
    const val COMPACT_APP_ICON_SIZE_DP = 32
    const val COMPACT_CATEGORY_FOLDER_HEIGHT_DP = 86
    const val COMPACT_CATEGORY_PREVIEW_ICON_SIZE_DP = 15
    const val COMPACT_SEARCH_GRID_TILE_HEIGHT_DP = 68
    const val COMPACT_SEARCH_GRID_ICON_SIZE_DP = 30

    // Shared launcher UI
    const val PILL_RADIUS_DP = 22
    const val HEADER_BUTTON_WIDTH_DP = 44
    const val HEADER_BUTTON_HEIGHT_DP = 38
    const val HEADER_ICON_SIZE_DP = 22
    const val CATEGORY_FOLDER_RADIUS_DP = 20
    const val CATEGORY_PANEL_RADIUS_DP = 22

    // Animations
    const val EXPAND_DURATION_MS = 240L
    const val COLLAPSE_DURATION_MS = 220L
    const val PANEL_OPEN_DURATION_MS = 230L
    const val PANEL_CLOSE_DURATION_MS = 180L

    // Favorites
    const val HOME_FAVORITE_TILE_WIDTH_DP = 70
    const val HOME_FAVORITE_TILE_HEIGHT_DP = 62
    const val HOME_FAVORITE_ICON_SIZE_DP = 30
    const val HOME_FAVORITE_TEXT_SIZE_SP = 9f
    const val HOME_FAVORITE_MARGIN_END_DP = 7

    // Recent apps
    const val HOME_RECENT_TILE_WIDTH_DP = 74
    const val HOME_RECENT_TILE_HEIGHT_DP = 82
    const val HOME_RECENT_ICON_TILE_DP = 42
    const val HOME_RECENT_ICON_SIZE_DP = 29
    const val HOME_RECENT_LIMIT = 6

    const val MAX_FAVORITES = 8
}