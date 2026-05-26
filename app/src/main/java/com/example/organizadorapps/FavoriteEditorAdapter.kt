package com.example.organizadorapps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FavoriteEditorAdapter(
    private val allApps: List<InstalledApp>,
    private val maxFavorites: Int = UiConstants.MAX_FAVORITES,
    private val onLimitReached: (() -> Unit)? = null,
    private val onStartDrag: ((RecyclerView.ViewHolder) -> Unit)? = null,
    private val onFavoriteChanged: () -> Unit
) : RecyclerView.Adapter<FavoriteEditorAdapter.FavoriteEditorViewHolder>() {

    private val visibleApps = allApps.toMutableList()

    inner class FavoriteEditorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.imgFavoriteEditorIcon)
        val name: TextView = itemView.findViewById(R.id.txtFavoriteEditorName)
        val badge: LinearLayout = itemView.findViewById(R.id.txtFavoriteEditorBadge)
        val dragHandle: ImageView = itemView.findViewById(R.id.txtFavoriteEditorDragHandle)
        val action: ImageView = itemView.findViewById(R.id.btnFavoriteEditorAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteEditorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_editor_app, parent, false)
        return FavoriteEditorViewHolder(view)
    }

    override fun getItemCount(): Int = visibleApps.size

    override fun onBindViewHolder(holder: FavoriteEditorViewHolder, position: Int) {
        val app = visibleApps[position]
        val context = holder.itemView.context

        val isFavorite = FavoritesManager.isFavorite(context, app.packageName)
        val favoriteCount = FavoritesManager.getFavoritePackageList(context).size
        val reachedLimit = favoriteCount >= maxFavorites
        val canAdd = isFavorite || !reachedLimit

        holder.icon.setImageDrawable(app.icon)
        holder.name.text = app.name

        if (isFavorite) {
            holder.badge.visibility = View.VISIBLE
            holder.dragHandle.visibility = View.VISIBLE
            holder.action.setImageResource(R.drawable.ic_remove)
            holder.itemView.alpha = 1f
        } else {
            holder.badge.visibility = View.GONE
            holder.dragHandle.visibility = View.GONE
            holder.action.setImageResource(R.drawable.ic_add)
            holder.itemView.alpha = if (canAdd) 1f else 0.55f
        }

        val toggleClick = View.OnClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition == RecyclerView.NO_POSITION) return@OnClickListener

            val currentApp = visibleApps.getOrNull(currentPosition) ?: return@OnClickListener
            val currentlyFavorite = FavoritesManager.isFavorite(context, currentApp.packageName)
            val currentCount = FavoritesManager.getFavoritePackageList(context).size
            val currentlyCanAdd = currentlyFavorite || currentCount < maxFavorites

            if (!currentlyCanAdd) {
                onLimitReached?.invoke()
                return@OnClickListener
            }

            FavoritesManager.toggleFavorite(context, currentApp.packageName)
            reorderVisibleAppsKeepingFavoritesFirst(context)
            notifyDataSetChanged()
            onFavoriteChanged()
        }

        holder.itemView.setOnClickListener(toggleClick)
        holder.action.setOnClickListener(toggleClick)

        val dragLongClick = View.OnLongClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition == RecyclerView.NO_POSITION) return@OnLongClickListener false

            val currentApp = visibleApps.getOrNull(currentPosition) ?: return@OnLongClickListener false
            if (!FavoritesManager.isFavorite(context, currentApp.packageName)) {
                return@OnLongClickListener false
            }

            onStartDrag?.invoke(holder)
            true
        }

        holder.itemView.setOnLongClickListener(dragLongClick)
        holder.dragHandle.setOnLongClickListener(dragLongClick)
        holder.dragHandle.setOnClickListener {
            if (isFavorite) {
                android.widget.Toast.makeText(
                    context,
                    "Mantén presionado y arrastra para ordenar.",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun filter(query: String) {
        val cleanQuery = query.trim().lowercase()
        visibleApps.clear()
        if (cleanQuery.isBlank()) {
            visibleApps.addAll(allApps)
        } else {
            visibleApps.addAll(allApps.filter { app -> app.name.lowercase().contains(cleanQuery) })
        }
        notifyDataSetChanged()
    }

    fun refreshFavoritesState() {
        notifyDataSetChanged()
    }

    fun getAppAt(position: Int): InstalledApp? = visibleApps.getOrNull(position)

    fun moveVisibleItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition < 0 || toPosition < 0 || fromPosition >= visibleApps.size || toPosition >= visibleApps.size || fromPosition == toPosition) return
        val movedApp = visibleApps.removeAt(fromPosition)
        visibleApps.add(toPosition, movedApp)
        notifyItemMoved(fromPosition, toPosition)
    }

    private fun reorderVisibleAppsKeepingFavoritesFirst(context: android.content.Context) {
        val appsByPackage = allApps.associateBy { it.packageName }
        val favoritePackages = FavoritesManager.getFavoritePackageList(context)
        val favoriteApps = favoritePackages.mapNotNull { appsByPackage[it] }
        val remainingApps = allApps
            .filterNot { it.packageName in favoritePackages }
            .sortedBy { it.name.lowercase() }
        visibleApps.clear()
        visibleApps.addAll(favoriteApps + remainingApps)
    }
}
