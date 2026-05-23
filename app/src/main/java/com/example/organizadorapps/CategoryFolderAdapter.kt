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
    private val categories: List<ExpandableCategoryItem>,
    private val onCategoryClick: (ExpandableCategoryItem) -> Unit,
    private val onAppClick: (InstalledApp) -> Unit,
    private val onAllAppsClick: () -> Unit
) : RecyclerView.Adapter<CategoryFolderAdapter.CategoryFolderViewHolder>() {

    inner class CategoryFolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryRoot: LinearLayout = itemView.findViewById(R.id.categoryRoot)
        val folderCard: LinearLayout = itemView.findViewById(R.id.folderCard)
        val iconPreviewGrid: GridLayout = itemView.findViewById(R.id.iconPreviewGrid)
        val txtCategoryName: TextView = itemView.findViewById(R.id.txtCategoryName)
        val txtCategoryCount: TextView = itemView.findViewById(R.id.txtCategoryCount)
        val txtExpandArrow: TextView = itemView.findViewById(R.id.txtExpandArrow)
        val recyclerExpandedApps: RecyclerView = itemView.findViewById(R.id.recyclerExpandedApps)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryFolderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expandable_category_folder, parent, false)

        return CategoryFolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryFolderViewHolder, position: Int) {
        val item = categories[position]
        val category = item.category
        val context = holder.itemView.context
        val apps = category.apps

        holder.txtCategoryName.text = category.name
        holder.txtCategoryCount.text = "${apps.size} apps"
        holder.txtExpandArrow.text = if (item.isExpanded) "⌃" else "⌄"

        holder.txtCategoryName.typeface = Typeface.DEFAULT_BOLD
        holder.txtCategoryName.setTextColor(UiConstants.TEXT_PRIMARY)
        holder.txtCategoryCount.setTextColor(UiConstants.TEXT_SECONDARY)

        holder.iconPreviewGrid.removeAllViews()

        apps.take(4).forEach { app ->
            val iconSize = AppUiUtils.dp(context, 22)

            val icon = ImageView(context).apply {
                setImageDrawable(app.icon)
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

        holder.folderCard.setOnClickListener {
            AnimationUtils.press(holder.folderCard) {
                if (category.name == "Todas las apps") {
                    onAllAppsClick()
                } else {
                    onCategoryClick(item)
                }
            }
        }

        holder.recyclerExpandedApps.visibility =
            if (item.isExpanded && category.name != "Todas las apps") {
                View.VISIBLE
            } else {
                View.GONE
            }

        if (item.isExpanded && category.name != "Todas las apps") {
            holder.recyclerExpandedApps.apply {
                layoutManager = GridLayoutManager(context, 4)
                adapter = ExpandedAppsAdapter(apps) { app ->
                    onAppClick(app)
                }
                overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                isNestedScrollingEnabled = false
            }
        } else {
            holder.recyclerExpandedApps.adapter = null
        }
    }

    override fun getItemCount(): Int = categories.size
}