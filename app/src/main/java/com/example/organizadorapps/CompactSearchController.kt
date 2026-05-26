package com.example.organizadorapps

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Buscador inline para Compact Mode.
 *
 * Mejora aplicada:
 * - Al entrar al buscador ya se muestran todas las apps.
 * - A medida que el usuario escribe, la lista se reduce en la misma pantalla.
 * - No abre Home/MainActivity para buscar.
 */
class CompactSearchController(
    private val context: Context,
    private val host: LinearLayout,
    private val allAppsProvider: () -> List<InstalledApp>,
    private val onBack: () -> Unit,
    private val onAppClick: (InstalledApp) -> Unit
) {

    private var searchEditText: EditText? = null
    private var resultsHost: LinearLayout? = null

    fun show() {
        host.removeAllViews()
        host.visibility = View.VISIBLE

        host.addView(buildHeader())

        val input = AppUiUtils.searchBox(
            context = context,
            hintValue = "Buscar apps..."
        ) { query ->
            renderResults(query)
        }.apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                AppUiUtils.dp(context, 46)
            ).apply {
                bottomMargin = AppUiUtils.dp(context, 10)
            }
        }

        searchEditText = input
        host.addView(input)

        resultsHost = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        host.addView(
            resultsHost,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // Cambio clave: query vacío ya no muestra mensaje vacío; muestra todas las apps.
        renderResults("")

        input.requestFocus()
        input.post {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun clearAndHideKeyboard() {
        searchEditText?.let { editText ->
            editText.setText("")
            editText.clearFocus()

            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(editText.windowToken, 0)
        }
    }

    private fun buildHeader(): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, AppUiUtils.dp(context, 4), 0, AppUiUtils.dp(context, 10))

            addView(
                TextView(context).apply {
                    text = "Buscar apps"
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

            addView(
                TextView(context).apply {
                    text = "Volver"
                    textSize = 14f
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    setTextColor(UiConstants.ACCENT)
                    includeFontPadding = false
                    setPadding(
                        AppUiUtils.dp(context, 12),
                        AppUiUtils.dp(context, 6),
                        0,
                        AppUiUtils.dp(context, 6)
                    )
                    setOnClickListener {
                        AnimationUtils.press(this) {
                            clearAndHideKeyboard()
                            onBack()
                        }
                    }
                }
            )
        }
    }

    private fun renderResults(query: String) {
        val container = resultsHost ?: return
        container.removeAllViews()

        val cleanQuery = query.trim()

        val results = allAppsProvider()
            .filter { app ->
                cleanQuery.isBlank() || app.name.contains(cleanQuery, ignoreCase = true)
            }
            .sortedBy { it.name.lowercase() }

        container.addView(
            LauncherUiFactory.emptyText(
                context = context,
                textValue = if (cleanQuery.isBlank()) {
                    "Todas las apps (${results.size})"
                } else {
                    "Resultados (${results.size})"
                },
                bottomPaddingDp = 6
            )
        )

        if (results.isEmpty()) {
            container.addView(
                LauncherUiFactory.emptyText(
                    context = context,
                    textValue = "No se encontraron apps.",
                    bottomPaddingDp = 12
                )
            )
            return
        }

        container.addView(
            LauncherUiFactory.expandedAppsGrid(
                context = context,
                apps = results,
                columns = UiConstants.COMPACT_COLUMNS,
                iconSizeDp = UiConstants.COMPACT_SEARCH_GRID_ICON_SIZE_DP,
                tileHeightDp = UiConstants.COMPACT_SEARCH_GRID_TILE_HEIGHT_DP,
                horizontalGapDp = 4,
                bottomGapDp = 4,
                closeAfterLaunch = null
            ) { app ->
                RecentAppsManager.registerAppOpen(context, app)
                onAppClick(app)
            }.apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = AppUiUtils.dp(context, 4)
                    bottomMargin = AppUiUtils.dp(context, 14)
                }
            }
        )
    }
}
