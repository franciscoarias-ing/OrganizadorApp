package com.example.organizadorapps

import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(
    private var apps: List<InstalledApp>,
    private val mode: Mode = Mode.DEFAULT,
    private val showAddFavorite: Boolean = false,
    private val onAddFavoriteClick: (() -> Unit)? = null,
    private val onLongPress: ((InstalledApp) -> Unit)? = null
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    enum class Mode {
        DEFAULT,
        RECENT,
        FAVORITE
    }

    companion object {
        private const val TYPE_APP = 0
        private const val TYPE_ADD_FAVORITE = 1
    }

    inner class AppViewHolder(val container: LinearLayout) :
        RecyclerView.ViewHolder(container)

    override fun getItemViewType(position: Int): Int {
        return if (
            mode == Mode.FAVORITE &&
            showAddFavorite &&
            position == apps.size
        ) {
            TYPE_ADD_FAVORITE
        } else {
            TYPE_APP
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val context = parent.context

        val root = when {
            viewType == TYPE_ADD_FAVORITE -> {
                LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    setPadding(
                        AppUiUtils.dp(context, 10),
                        AppUiUtils.dp(context, 10),
                        AppUiUtils.dp(context, 10),
                        AppUiUtils.dp(context, 10)
                    )

                    layoutParams = RecyclerView.LayoutParams(
                        AppUiUtils.dp(context, 102),
                        RecyclerView.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginEnd = AppUiUtils.dp(context, 14)
                    }
                }
            }

            mode == Mode.RECENT -> {
                LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    background = AppUiUtils.roundedCard()
                    setPadding(
                        AppUiUtils.dp(context, 6),
                        AppUiUtils.dp(context, 6),
                        AppUiUtils.dp(context, 6),
                        AppUiUtils.dp(context, 5)
                    )

                    layoutParams = RecyclerView.LayoutParams(
                        AppUiUtils.dp(context, UiConstants.HOME_RECENT_TILE_WIDTH_DP),
                        AppUiUtils.dp(context, UiConstants.HOME_RECENT_TILE_HEIGHT_DP)
                    ).apply {
                        marginEnd = AppUiUtils.dp(context, 8)
                    }
                }
            }

            mode == Mode.FAVORITE -> {
                LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    background = AppUiUtils.roundedCard()
                    setPadding(
                        AppUiUtils.dp(context, 7),
                        AppUiUtils.dp(context, 7),
                        AppUiUtils.dp(context, 7),
                        AppUiUtils.dp(context, 6)
                    )

                    layoutParams = RecyclerView.LayoutParams(
                        AppUiUtils.dp(context, UiConstants.HOME_FAVORITE_TILE_WIDTH_DP),
                        AppUiUtils.dp(context, UiConstants.HOME_FAVORITE_TILE_HEIGHT_DP)
                    ).apply {
                        marginEnd = AppUiUtils.dp(context, UiConstants.HOME_FAVORITE_MARGIN_END_DP)
                    }
                }
            }

            else -> {
                LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    background = AppUiUtils.roundedCard()
                    setPadding(
                        AppUiUtils.dp(context, 12),
                        AppUiUtils.dp(context, 14),
                        AppUiUtils.dp(context, 12),
                        AppUiUtils.dp(context, 14)
                    )

                    layoutParams = RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 0, AppUiUtils.dp(context, 12))
                    }
                }
            }
        }

        return AppViewHolder(root)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.container.removeAllViews()

        if (getItemViewType(position) == TYPE_ADD_FAVORITE) {
            bindAddFavorite(holder)
            return
        }

        val app = apps[position]

        when (mode) {
            Mode.RECENT -> bindRecent(holder, app)
            Mode.FAVORITE -> bindFavorite(holder, app)
            Mode.DEFAULT -> bindDefault(holder, app)
        }

        holder.container.setOnClickListener {
            AnimationUtils.press(holder.container) {
                val context = holder.container.context
                val launchIntent =
                    context.packageManager.getLaunchIntentForPackage(app.packageName)

                launchIntent?.let {
                    RecentAppsManager.registerAppOpen(context, app)
                    context.startActivity(it)
                }
            }
        }

        holder.container.setOnLongClickListener {
            onLongPress?.invoke(app)
            true
        }
    }

    private fun bindRecent(holder: AppViewHolder, app: InstalledApp) {
        val context = holder.container.context

        val iconContainer = LinearLayout(context).apply {
            gravity = Gravity.CENTER
            background = AppUiUtils.appIconTile(context)

            layoutParams = LinearLayout.LayoutParams(
                AppUiUtils.dp(context, UiConstants.HOME_RECENT_ICON_TILE_DP),
                AppUiUtils.dp(context, UiConstants.HOME_RECENT_ICON_TILE_DP)
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        }

        iconContainer.addView(
            ImageView(context).apply {
                setImageDrawable(IconCacheManager.getIcon(context, app))
                layoutParams = LinearLayout.LayoutParams(
                    AppUiUtils.dp(context, UiConstants.HOME_RECENT_ICON_SIZE_DP),
                    AppUiUtils.dp(context, UiConstants.HOME_RECENT_ICON_SIZE_DP)
                )
            }
        )

        holder.container.addView(iconContainer)

        holder.container.addView(
            TextView(context).apply {
                text = app.name
                textSize = 9.5f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setTextColor(UiConstants.TEXT_PRIMARY)
                maxLines = 1
                includeFontPadding = false

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = AppUiUtils.dp(context, 5)
                }
            }
        )

        holder.container.addView(
            TextView(context).apply {
                text = RecentAppsManager.getRecentTimeLabel(context, app.packageName)
                textSize = 8.5f
                gravity = Gravity.CENTER
                setTextColor(UiConstants.TEXT_SECONDARY)
                maxLines = 1
                includeFontPadding = false

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = AppUiUtils.dp(context, 3)
                }
            }
        )
    }

    private fun bindFavorite(holder: AppViewHolder, app: InstalledApp) {
        val context = holder.container.context

        holder.container.addView(
            ImageView(context).apply {
                setImageDrawable(IconCacheManager.getIcon(context, app))

                layoutParams = LinearLayout.LayoutParams(
                    AppUiUtils.dp(context, UiConstants.HOME_FAVORITE_ICON_SIZE_DP),
                    AppUiUtils.dp(context, UiConstants.HOME_FAVORITE_ICON_SIZE_DP)
                ).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                }
            }
        )

        holder.container.addView(
            TextView(context).apply {
                text = app.name
                textSize = UiConstants.HOME_FAVORITE_TEXT_SIZE_SP
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setTextColor(UiConstants.TEXT_PRIMARY)
                maxLines = 1
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

    private fun bindAddFavorite(holder: AppViewHolder) {
        val context = holder.container.context

        val addCard = LinearLayout(context).apply {
            gravity = Gravity.CENTER
            background = AppUiUtils.addFavoriteCard(context)

            layoutParams = LinearLayout.LayoutParams(
                AppUiUtils.dp(context, 62),
                AppUiUtils.dp(context, 62)
            )
        }

        addCard.addView(
            ImageView(context).apply {
                setImageResource(R.drawable.ic_add)
                setColorFilter(UiConstants.ACCENT)
                setPadding(
                    AppUiUtils.dp(context, 14),
                    AppUiUtils.dp(context, 14),
                    AppUiUtils.dp(context, 14),
                    AppUiUtils.dp(context, 14)
                )
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            }
        )

        holder.container.addView(addCard)

        holder.container.addView(
            TextView(context).apply {
                text = "Agregar"
                textSize = 13f
                gravity = Gravity.CENTER
                setTextColor(UiConstants.TEXT_PRIMARY)
                includeFontPadding = false
                setPadding(0, AppUiUtils.dp(context, 10), 0, 0)
            }
        )

        holder.container.setOnClickListener {
            AnimationUtils.press(holder.container) {
                onAddFavoriteClick?.invoke()
            }
        }
    }

    private fun bindDefault(holder: AppViewHolder, app: InstalledApp) {
        val context = holder.container.context

        holder.container.addView(
            ImageView(context).apply {
                setImageDrawable(IconCacheManager.getIcon(context, app))

                layoutParams = LinearLayout.LayoutParams(
                    AppUiUtils.dp(context, 54),
                    AppUiUtils.dp(context, 54)
                ).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                }
            }
        )

        holder.container.addView(
            TextView(context).apply {
                text = app.name
                textSize = 15f
                gravity = Gravity.CENTER
                setTextColor(UiConstants.TEXT_PRIMARY)
                maxLines = 2
                includeFontPadding = false
                setPadding(0, AppUiUtils.dp(context, 12), 0, 0)
            }
        )
    }

    fun updateApps(newApps: List<InstalledApp>) {
        val diffResult = DiffUtil.calculateDiff(AppDiffCallback(apps, newApps))
        apps = newApps
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int {
        return apps.size + if (mode == Mode.FAVORITE && showAddFavorite) 1 else 0
    }
}