package com.example.organizadorapps

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(
    private val apps: List<InstalledApp>,
    private val mode: Mode = Mode.GRID
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    enum class Mode {
        GRID,
        RECENT,
        FAVORITE
    }

    class AppViewHolder(val layout: LinearLayout) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val context = parent.context

        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true

            // Solo GRID mantiene card. RECENT y FAVORITE quedan minimalistas.
            background = if (mode == Mode.GRID) {
                AppUiUtils.roundedCard()
            } else {
                null
            }

            val width = when (mode) {
                Mode.RECENT -> AppUiUtils.dp(context, 72)
                Mode.FAVORITE -> AppUiUtils.dp(context, 72)
                Mode.GRID -> AppUiUtils.dp(context, 148)
            }

            val height = when (mode) {
                Mode.RECENT -> LinearLayout.LayoutParams.WRAP_CONTENT
                Mode.FAVORITE -> LinearLayout.LayoutParams.WRAP_CONTENT
                Mode.GRID -> AppUiUtils.dp(context, 168)
            }

            setPadding(
                AppUiUtils.dp(context, 3),
                AppUiUtils.dp(context, 3),
                AppUiUtils.dp(context, 3),
                AppUiUtils.dp(context, 3)
            )

            layoutParams = ViewGroup.MarginLayoutParams(width, height).apply {
                setMargins(
                    AppUiUtils.dp(context, 4),
                    AppUiUtils.dp(context, 4),
                    AppUiUtils.dp(context, 16),
                    AppUiUtils.dp(context, 4)
                )
            }
        }

        return AppViewHolder(card)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        val context = holder.layout.context
        val isFavorite = FavoritesManager.isFavorite(context, app.packageName)

        holder.layout.removeAllViews()

        holder.layout.background = when (mode) {
            Mode.GRID -> {
                if (isFavorite) AppUiUtils.favoriteCard() else AppUiUtils.roundedCard()
            }

            Mode.RECENT,
            Mode.FAVORITE -> null
        }

        holder.layout.setOnClickListener {
            RecentAppsManager.registerAppOpen(context, app)
            AnimationUtils.press(holder.layout) {
                AppLauncher.openApp(context, app.packageName, app.name)
            }
        }

        holder.layout.setOnLongClickListener {
            if (FavoritesManager.isFavorite(context, app.packageName)) {
                FavoritesManager.removeFavorite(context, app.packageName)
                Toast.makeText(context, "${app.name} quitada de favoritos", Toast.LENGTH_SHORT).show()
            } else {
                FavoritesManager.addFavorite(context, app.packageName)
                Toast.makeText(context, "${app.name} agregada a favoritos", Toast.LENGTH_SHORT).show()
            }

            AnimationUtils.pop(holder.layout)
            notifyItemChanged(position)
            true
        }

        when (mode) {
            Mode.RECENT -> bindRecent(holder, app)
            Mode.FAVORITE -> bindFavorite(holder, app)
            Mode.GRID -> bindGrid(holder, app, isFavorite)
        }
    }

    private fun bindRecent(holder: AppViewHolder, app: InstalledApp) {
        val context = holder.layout.context

        holder.layout.addView(ImageView(context).apply {
            setImageDrawable(app.icon)
            layoutParams = LinearLayout.LayoutParams(
                AppUiUtils.dp(context, 50),
                AppUiUtils.dp(context, 50)
            )
        })

        holder.layout.addView(TextView(context).apply {
            text = app.name
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(UiConstants.TEXT_PRIMARY)
            gravity = Gravity.CENTER
            includeFontPadding = false
            maxLines = 1
            setPadding(0, AppUiUtils.dp(context, 6), 0, 0)
        })
    }

    private fun bindFavorite(holder: AppViewHolder, app: InstalledApp) {
        val context = holder.layout.context

        holder.layout.addView(ImageView(context).apply {
            setImageDrawable(app.icon)
            layoutParams = LinearLayout.LayoutParams(
                AppUiUtils.dp(context, 52),
                AppUiUtils.dp(context, 52)
            )
        })

        holder.layout.addView(TextView(context).apply {
            text = app.name
            textSize = 11f
            setTextColor(UiConstants.TEXT_PRIMARY)
            gravity = Gravity.CENTER
            includeFontPadding = false
            maxLines = 1
            setPadding(0, AppUiUtils.dp(context, 6), 0, 0)
        })
    }

    private fun bindGrid(holder: AppViewHolder, app: InstalledApp, isFavorite: Boolean) {
        val context = holder.layout.context

        holder.layout.addView(ImageView(context).apply {
            setImageDrawable(app.icon)
            layoutParams = LinearLayout.LayoutParams(
                AppUiUtils.dp(context, 64),
                AppUiUtils.dp(context, 64)
            )
        })

        holder.layout.addView(TextView(context).apply {
            text = if (isFavorite) "★ ${app.name}" else app.name
            textSize = 12f
            typeface = if (isFavorite) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            setTextColor(Color.parseColor("#E5E7EB"))
            gravity = Gravity.CENTER
            includeFontPadding = false
            setPadding(0, AppUiUtils.dp(context, 10), 0, 0)
            maxLines = 2
        })
    }

    override fun getItemCount(): Int = apps.size
}