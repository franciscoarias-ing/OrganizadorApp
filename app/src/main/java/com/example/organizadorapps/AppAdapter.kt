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
    private val apps: List<InstalledApp>
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    class AppViewHolder(val layout: LinearLayout) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val context = parent.context

        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(12, 12, 12, 12)
            background = AppUiUtils.roundedCard()
            isClickable = true
            isFocusable = true

            layoutParams = ViewGroup.MarginLayoutParams(168, 198).apply {
                setMargins(8, 8, 8, 8)
            }
        }

        return AppViewHolder(card)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        val context = holder.layout.context
        val isFavorite = FavoritesManager.isFavorite(context, app.packageName)

        holder.layout.removeAllViews()
        holder.layout.background =
            if (isFavorite) AppUiUtils.favoriteCard() else AppUiUtils.roundedCard()

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

        holder.layout.addView(ImageView(context).apply {
            setImageDrawable(app.icon)
            layoutParams = LinearLayout.LayoutParams(74, 74)
        })

        holder.layout.addView(TextView(context).apply {
            text = if (isFavorite) "★ ${app.name}" else app.name
            textSize = 11f
            typeface = if (isFavorite) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            setTextColor(Color.parseColor("#E5E7EB"))
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 0)
            maxLines = 2
        })
    }

    override fun getItemCount(): Int = apps.size
}