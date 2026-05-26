package com.example.organizadorapps

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog

object AppActionsBottomSheet {

    fun show(
        context: Context,
        app: InstalledApp,
        onFavoritesChanged: (() -> Unit)? = null,
        onHiddenChanged: (() -> Unit)? = null
    ) {
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

        root.addView(buildHeader(context, app))

        val isFavorite = FavoritesManager.isFavorite(context, app.packageName)
        root.addView(
            actionRow(
                context = context,
                title = if (isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
                subtitle = if (isFavorite) "Retira esta app de Favoritos rápidos." else "Añade esta app a Favoritos rápidos.",
                iconRes = if (isFavorite) R.drawable.ic_remove else R.drawable.ic_add
            ) {
                FavoritesManager.toggleFavorite(context, app.packageName)
                onFavoritesChanged?.invoke()
                Toast.makeText(
                    context,
                    if (isFavorite) "App retirada de favoritos." else "App agregada a favoritos.",
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
        )

        root.addView(
            actionRow(
                context = context,
                title = "Ocultar app",
                subtitle = "No se mostrará en Inicio, búsqueda ni categorías.",
                iconRes = R.drawable.ic_hide_app
            ) {
                HiddenAppsManager.hideApp(context, app.packageName)
                FavoritesManager.removeFavorite(context, app.packageName)
                onHiddenChanged?.invoke()
                Toast.makeText(context, "App ocultada.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        )

        root.addView(
            actionRow(
                context = context,
                title = "Info del sistema",
                subtitle = "Abre la ficha de información de Android.",
                iconRes = R.drawable.ic_info
            ) {
                openIntent(
                    context,
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${app.packageName}")
                    },
                    "No se pudo abrir la información de la app."
                )
                dialog.dismiss()
            }
        )

        root.addView(
            actionRow(
                context = context,
                title = "Abrir Play Store",
                subtitle = "Busca esta app en la tienda.",
                iconRes = R.drawable.ic_play_store
            ) {
                openPlayStore(context, app.packageName)
                dialog.dismiss()
            }
        )

        root.addView(
            actionRow(
                context = context,
                title = "Abrir ajustes",
                subtitle = "Abre Ajustes del sistema Android.",
                iconRes = R.drawable.ic_settings_gear
            ) {
                openIntent(
                    context,
                    Intent(Settings.ACTION_SETTINGS),
                    "No se pudo abrir Ajustes."
                )
                dialog.dismiss()
            }
        )

        dialog.setContentView(root)
        dialog.show()
    }

    private fun buildHeader(context: Context, app: InstalledApp): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, AppUiUtils.dp(context, 14))

            addView(
                ImageView(context).apply {
                    setImageDrawable(app.icon)
                    layoutParams = LinearLayout.LayoutParams(
                        AppUiUtils.dp(context, 42),
                        AppUiUtils.dp(context, 42)
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
                            textSize = 17f
                            typeface = Typeface.DEFAULT_BOLD
                            setTextColor(UiConstants.TEXT_PRIMARY)
                            maxLines = 1
                            includeFontPadding = false
                        }
                    )

                    addView(
                        TextView(context).apply {
                            text = app.packageName
                            textSize = 11f
                            setTextColor(UiConstants.TEXT_SECONDARY)
                            maxLines = 1
                            includeFontPadding = false
                            setPadding(0, AppUiUtils.dp(context, 5), 0, 0)
                        }
                    )
                }
            )
        }
    }

    private fun actionRow(
        context: Context,
        title: String,
        subtitle: String,
        iconRes: Int,
        onClick: () -> Unit
    ): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            isClickable = true
            isFocusable = true
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
                    setImageResource(iconRes)
                    setColorFilter(UiConstants.ACCENT)
                    setPadding(
                        AppUiUtils.dp(context, 9),
                        AppUiUtils.dp(context, 9),
                        AppUiUtils.dp(context, 9),
                        AppUiUtils.dp(context, 9)
                    )
                    background = AppUiUtils.appIconTile(context)
                    layoutParams = LinearLayout.LayoutParams(
                        AppUiUtils.dp(context, 38),
                        AppUiUtils.dp(context, 38)
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
                            text = title
                            textSize = 14f
                            typeface = Typeface.DEFAULT_BOLD
                            setTextColor(UiConstants.TEXT_PRIMARY)
                            includeFontPadding = false
                        }
                    )

                    addView(
                        TextView(context).apply {
                            text = subtitle
                            textSize = 11.5f
                            setTextColor(UiConstants.TEXT_SECONDARY)
                            includeFontPadding = false
                            setPadding(0, AppUiUtils.dp(context, 4), 0, 0)
                        }
                    )
                }
            )

            setOnClickListener {
                AnimationUtils.press(this) { onClick() }
            }
        }
    }

    private fun openPlayStore(context: Context, packageName: String) {
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))

        try {
            marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(marketIntent)
        } catch (_: Exception) {
            openIntent(context, webIntent, "No se pudo abrir Play Store.")
        }
    }

    private fun openIntent(context: Context, intent: Intent, errorMessage: String) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
}
