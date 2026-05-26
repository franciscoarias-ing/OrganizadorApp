package com.example.organizadorapps

import android.content.Context
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections

object FavoriteOrderManager {

    fun attachToRecyclerView(
        recyclerView: RecyclerView,
        adapter: FavoriteEditorAdapter,
        context: Context,
        onOrderChanged: (() -> Unit)? = null
    ): ItemTouchHelper {
        val callback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            0
        ) {
            override fun isLongPressDragEnabled(): Boolean {
                // El inicio manual desde el adapter es más confiable dentro del panel inline.
                return false
            }

            override fun isItemViewSwipeEnabled(): Boolean {
                return false
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.animate()
                        ?.scaleX(1.02f)
                        ?.scaleY(1.02f)
                        ?.alpha(0.92f)
                        ?.setDuration(90)
                        ?.start()
                }
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(90)
                    .start()
                adapter.refreshFavoritesState()
            }

            override fun onMove(
                recyclerView: RecyclerView,
                source: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = source.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition

                if (
                    fromPosition == RecyclerView.NO_POSITION ||
                    toPosition == RecyclerView.NO_POSITION ||
                    fromPosition == toPosition
                ) {
                    return false
                }

                val fromApp = adapter.getAppAt(fromPosition) ?: return false
                val toApp = adapter.getAppAt(toPosition) ?: return false

                val favoritePackages = FavoritesManager.getFavoritePackageList(context).toMutableList()

                val fromFavoriteIndex = favoritePackages.indexOf(fromApp.packageName)
                val toFavoriteIndex = favoritePackages.indexOf(toApp.packageName)

                // Solo permitimos mover favoritos sobre favoritos.
                if (fromFavoriteIndex == -1 || toFavoriteIndex == -1) {
                    return false
                }

                Collections.swap(favoritePackages, fromFavoriteIndex, toFavoriteIndex)
                FavoritesManager.replaceFavorites(context, favoritePackages)

                adapter.swapVisibleItems(fromPosition, toPosition)
                onOrderChanged?.invoke()

                return true
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
                // No usamos swipe para eliminar favoritos.
            }
        }

        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        return itemTouchHelper
    }
}
