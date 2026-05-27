package com.example.organizadorapps

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CategoryFolderAdapter(
    initialItems: List<CategoryGridItem>,
    private val onFolderClick: (ExpandableCategoryItem) -> Unit,
    private val onCollapseClick: () -> Unit,
    private val onAppClick: (InstalledApp) -> Unit,
    private val onAllAppsClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<CategoryGridItem>()

    companion object {
        const val VIEW_TYPE_FOLDER = 1
        const val VIEW_TYPE_EXPANDED_PANEL = 2
    }

    init {
        items.addAll(initialItems)
        setHasStableIds(false)
    }

    fun updateItems(newItems: List<CategoryGridItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getItemAt(position: Int): CategoryGridItem? {
        return items.getOrNull(position)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is CategoryGridItem.Folder -> VIEW_TYPE_FOLDER
            is CategoryGridItem.ExpandedPanel -> VIEW_TYPE_EXPANDED_PANEL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            VIEW_TYPE_EXPANDED_PANEL -> {
                val view = inflater.inflate(
                    R.layout.item_category_expanded_panel,
                    parent,
                    false
                )
                ExpandedPanelViewHolder(view)
            }

            else -> {
                val view = inflater.inflate(
                    R.layout.item_expandable_category_folder,
                    parent,
                    false
                )
                FolderViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val gridItem = items[position]) {
            is CategoryGridItem.Folder -> bindFolder(
                holder = holder as FolderViewHolder,
                item = gridItem.item
            )

            is CategoryGridItem.ExpandedPanel -> bindExpandedPanel(
                holder = holder as ExpandedPanelViewHolder,
                item = gridItem.item
            )
        }
    }

    private fun bindFolder(
        holder: FolderViewHolder,
        item: ExpandableCategoryItem
    ) {
        val context = holder.itemView.context
        val category = item.category
        val apps = category.apps

        holder.txtCategoryName.text = category.name
        holder.txtCategoryCount.text = "${apps.size} apps"

        holder.txtCategoryName.typeface = Typeface.DEFAULT_BOLD
        holder.txtCategoryName.setTextColor(UiConstants.TEXT_PRIMARY)
        holder.txtCategoryCount.setTextColor(UiConstants.TEXT_SECONDARY)

        holder.iconPreviewGrid.removeAllViews()

        apps.take(4).forEach { app ->
            val iconSize = AppUiUtils.dp(context, 22)

            val icon = ImageView(context).apply {
                setImageDrawable(IconCacheManager.getIcon(context, app))
                scaleType = ImageView.ScaleType.FIT_CENTER

                layoutParams = ViewGroup.MarginLayoutParams(
                    iconSize,
                    iconSize
                ).apply {
                    setMargins(
                        AppUiUtils.dp(context, 2),
                        AppUiUtils.dp(context, 2),
                        AppUiUtils.dp(context, 2),
                        AppUiUtils.dp(context, 2)
                    )
                }
            }

            holder.iconPreviewGrid.addView(icon)
        }

        holder.folderCard.alpha = if (item.isExpanded) 0.92f else 1f

        holder.folderCard.setOnClickListener {
            AnimationUtils.press(holder.folderCard) {
                if (category.name == "Todas las apps") {
                    onAllAppsClick()
                } else {
                    onFolderClick(item)
                }
            }
        }
    }

    private fun bindExpandedPanel(
        holder: ExpandedPanelViewHolder,
        item: ExpandableCategoryItem
    ) {
        val context = holder.itemView.context
        val category = item.category
        val apps = category.apps

        holder.txtExpandedCategoryName.text = category.name
        holder.txtExpandedCategoryCount.text = "${apps.size} apps"
        holder.btnCollapseCategory.text = "⌃"

        holder.expandedPreviewGrid.removeAllViews()

        apps.take(4).forEach { app ->
            val iconSize = AppUiUtils.dp(context, 20)

            val icon = ImageView(context).apply {
                setImageDrawable(IconCacheManager.getIcon(context, app))
                scaleType = ImageView.ScaleType.FIT_CENTER

                layoutParams = ViewGroup.MarginLayoutParams(
                    iconSize,
                    iconSize
                ).apply {
                    setMargins(
                        AppUiUtils.dp(context, 2),
                        AppUiUtils.dp(context, 2),
                        AppUiUtils.dp(context, 2),
                        AppUiUtils.dp(context, 2)
                    )
                }
            }

            holder.expandedPreviewGrid.addView(icon)
        }

        holder.btnCollapseCategory.setOnClickListener {
            AnimationUtils.press(holder.btnCollapseCategory) {
                onCollapseClick()
            }
        }

        holder.recyclerPanelApps.apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = ExpandedAppsAdapter(apps) { app ->
                onAppClick(app)
            }
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            isNestedScrollingEnabled = false
        }

        holder.expandedPanelRoot.alpha = 0f
        holder.expandedPanelRoot.translationY = -AppUiUtils.dp(context, 14).toFloat()

        holder.expandedPanelRoot.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(220)
            .start()
    }

    override fun getItemCount(): Int = items.size

    class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryRoot: LinearLayout = itemView.findViewById(R.id.categoryRoot)
        val folderCard: LinearLayout = itemView.findViewById(R.id.folderCard)
        val iconPreviewGrid: GridLayout = itemView.findViewById(R.id.iconPreviewGrid)
        val txtCategoryName: TextView = itemView.findViewById(R.id.txtCategoryName)
        val txtCategoryCount: TextView = itemView.findViewById(R.id.txtCategoryCount)
    }

    class ExpandedPanelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val expandedPanelRoot: LinearLayout = itemView.findViewById(R.id.expandedPanelRoot)
        val expandedPreviewGrid: GridLayout = itemView.findViewById(R.id.expandedPreviewGrid)
        val txtExpandedCategoryName: TextView = itemView.findViewById(R.id.txtExpandedCategoryName)
        val txtExpandedCategoryCount: TextView = itemView.findViewById(R.id.txtExpandedCategoryCount)
        val btnCollapseCategory: TextView = itemView.findViewById(R.id.btnCollapseCategory)
        val recyclerPanelApps: RecyclerView = itemView.findViewById(R.id.recyclerPanelApps)
    }
}