package com.example.organizadorapps

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
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
            setPadding(14, 14, 14, 14)
            background = cardBackground(false)
            isClickable = true
            isFocusable = true

            layoutParams = ViewGroup.MarginLayoutParams(178, 215).apply {
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
        holder.layout.background = cardBackground(isFavorite)

        holder.layout.setOnClickListener {
            holder.layout.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(70)
                .withEndAction {
                    holder.layout.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(70)
                        .start()

                    AppLauncher.openApp(context, app.packageName, app.name)
                }
                .start()
        }

        holder.layout.setOnLongClickListener {
            if (FavoritesManager.isFavorite(context, app.packageName)) {
                FavoritesManager.removeFavorite(context, app.packageName)
                Toast.makeText(context, "${app.name} quitada de favoritos", Toast.LENGTH_SHORT).show()
            } else {
                FavoritesManager.addFavorite(context, app.packageName)
                Toast.makeText(context, "${app.name} agregada a favoritos", Toast.LENGTH_SHORT).show()
            }

            holder.layout.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(90)
                .withEndAction {
                    holder.layout.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(90)
                        .start()
                }
                .start()

            notifyItemChanged(position)
            true
        }

        holder.layout.addView(ImageView(context).apply {
            setImageDrawable(app.icon)
            layoutParams = LinearLayout.LayoutParams(76, 76)
        })

        holder.layout.addView(TextView(context).apply {
            text = if (isFavorite) "★ ${app.name}" else app.name
            textSize = 11f
            typeface = if (isFavorite) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            setTextColor(Color.parseColor("#E5E7EB"))
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 0)
            maxLines = 2
        })
    }

    override fun getItemCount(): Int = apps.size

    private fun cardBackground(isFavorite: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.parseColor(if (isFavorite) "#252D3D" else "#1A1F2B"))
            cornerRadius = 26f
            setStroke(
                if (isFavorite) 2 else 1,
                Color.parseColor(if (isFavorite) "#D6A84F" else "#2B3140")
            )
        }
    }
}