package com.example.organizadorapps

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog

object HiddenAppsBottomSheet {

    fun show(
        context: Context,
        onHiddenChanged: (() -> Unit)? = null
    ) {
        val hiddenApps = AppRepository.getHiddenLaunchableApps(context)
        val dialog = BottomSheetDialog(context)

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                AppUiUtils.dp(context, 20),
                AppUiUtils.dp(context, 16),
                AppUiUtils.dp(context, 20),
                AppUiUtils.dp(context, 20)
            )
            background = AppUiUtils.roundedDrawable(
                context = context,
                color = UiConstants.SURFACE,
                radiusDp = 28,
                strokeColor = UiConstants.BORDER,
                strokeWidthDp = 1
            )
        }

        root.addView(
            TextView(context).apply {
                text = "Apps ocultas"
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(UiConstants.TEXT_PRIMARY)
                includeFontPadding = false
                setPadding(0, 0, 0, AppUiUtils.dp(context, 6))
            }
        )

        root.addView(
            TextView(context).apply {
                text = if (hiddenApps.isEmpty()) {
                    "No tienes apps ocultas. Mantén presionada una app y usa Ocultar app."
                } else {
                    "Toca Restaurar para que la app vuelva a aparecer en Inicio, búsqueda y categorías."
                }
                textSize = 12.5f
                setTextColor(UiConstants.TEXT_SECONDARY)
                includeFontPadding = false
                setPadding(0, 0, 0, AppUiUtils.dp(context, 14))
            }
        )

        if (hiddenApps.isEmpty()) {
            root.addView(AppUiUtils.miniEmpty(context, "Sin apps ocultas."))
        } else {
            val list = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
            }

            hiddenApps.forEach { app ->
                list.addView(hiddenAppRow(context, app) {
                    HiddenAppsManager.unhideApp(context, app.packageName)
                    onHiddenChanged?.invoke()
                    Toast.makeText(context, "${app.name} restaurada.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                })
            }

            root.addView(
                ScrollView(context).apply {
                    overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
                    addView(list)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        AppUiUtils.dp(context, 360)
                    )
                }
            )
        }

        dialog.setContentView(root)
        dialog.show()
    }

    private fun hiddenAppRow(
        context: Context,
        app: InstalledApp,
        onRestore: () -> Unit
    ): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = AppUiUtils.roundedDrawable(
                context = context,
                color = UiConstants.SURFACE_ALT,
                radiusDp = 18,
                strokeColor = UiConstants.BORDER,
                strokeWidthDp = 1
            )
            setPadding(
                AppUiUtils.dp(context, 12),
                AppUiUtils.dp(context, 10),
                AppUiUtils.dp(context, 12),
                AppUiUtils.dp(context, 10)
            )
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = AppUiUtils.dp(context, 8)
            }

            addView(
                ImageView(context).apply {
                    setImageDrawable(app.icon)
                    layoutParams = LinearLayout.LayoutParams(
                        AppUiUtils.dp(context, 36),
                        AppUiUtils.dp(context, 36)
                    ).apply {
                        marginEnd = AppUiUtils.dp(context, 12)
                    }
                }
            )

            addView(
                LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

                    addView(
                        TextView(context).apply {
                            text = app.name
                            textSize = 14f
                            typeface = Typeface.DEFAULT_BOLD
                            setTextColor(UiConstants.TEXT_PRIMARY)
                            includeFontPadding = false
                            maxLines = 1
                        }
                    )

                    addView(
                        TextView(context).apply {
                            text = app.packageName
                            textSize = 11f
                            setTextColor(UiConstants.TEXT_SECONDARY)
                            includeFontPadding = false
                            maxLines = 1
                            setPadding(0, AppUiUtils.dp(context, 4), 0, 0)
                        }
                    )
                }
            )

            addView(
                TextView(context).apply {
                    text = "Restaurar"
                    textSize = 13f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(UiConstants.ACCENT)
                    gravity = Gravity.CENTER
                    includeFontPadding = false
                    setPadding(
                        AppUiUtils.dp(context, 10),
                        AppUiUtils.dp(context, 7),
                        AppUiUtils.dp(context, 10),
                        AppUiUtils.dp(context, 7)
                    )
                    background = LauncherUiFactory.pillBackground(context)
                    setOnClickListener { onRestore() }
                }
            )
        }
    }
}
