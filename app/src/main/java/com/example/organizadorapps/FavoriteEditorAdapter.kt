package com.example.organizadorapps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FavoriteEditorAdapter(
    private val allApps: List<InstalledApp>,
    private val onFavoriteChanged: () -> Unit
) : RecyclerView.Adapter<FavoriteEditorAdapter.FavoriteEditorViewHolder>() {

    private val visibleApps = allApps.toMutableList()

    inner class FavoriteEditorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.imgFavoriteEditorIcon)
        val name: TextView = itemView.findViewById(R.id.txtFavoriteEditorName)
        val badge: TextView = itemView.findViewById(R.id.txtFavoriteEditorBadge)
        val action: TextView = itemView.findViewById(R.id.btnFavoriteEditorAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteEditorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_editor_app, parent, false)

        return FavoriteEditorViewHolder(view)
    }

    override fun getItemCount(): Int {
        return visibleApps.size
    }

    override fun onBindViewHolder(holder: FavoriteEditorViewHolder, position: Int) {
        val app = visibleApps[position]
        val context = holder.itemView.context
        val isFavorite = FavoritesManager.isFavorite(context, app.packageName)

        holder.icon.setImageDrawable(app.icon)
        holder.name.text = app.name

        if (isFavorite) {
            holder.badge.visibility = View.VISIBLE
            holder.badge.text = "✓ Favorito"
            holder.action.text = "−"
            holder.action.textSize = 24f
        } else {
            holder.badge.visibility = View.GONE
            holder.action.text = "+"
            holder.action.textSize = 24f
        }

        holder.itemView.setOnClickListener {
            FavoritesManager.toggleFavorite(context, app.packageName)

            notifyItemChanged(position)
            onFavoriteChanged()
        }

        holder.action.setOnClickListener {
            FavoritesManager.toggleFavorite(context, app.packageName)

            notifyItemChanged(position)
            onFavoriteChanged()
        }
    }

    fun filter(query: String) {
        val cleanQuery = query.trim().lowercase()

        visibleApps.clear()

        if (cleanQuery.isBlank()) {
            visibleApps.addAll(allApps)
        } else {
            visibleApps.addAll(
                allApps.filter { app ->
                    app.name.lowercase().contains(cleanQuery)
                }
            )
        }

        notifyDataSetChanged()
    }

    fun refreshFavoritesState() {
        notifyDataSetChanged()
    }
}