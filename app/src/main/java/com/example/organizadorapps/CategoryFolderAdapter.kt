package com.example.organizadorapps

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryFolderAdapter(
    private val categories: List<AppCategory>,
    private val onCategoryClick: (AppCategory) -> Unit
) : RecyclerView.Adapter<CategoryFolderAdapter.FolderViewHolder>() {

    class FolderViewHolder(val layout: LinearLayout) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val context = parent.context

        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(14, 14, 14, 14)
            isClickable = true
            isFocusable = true

            background = GradientDrawable().apply {
                setColor(UiConstants.SURFACE)
                cornerRadius = 28f
                setStroke(1, UiConstants.BORDER)
            }

            layoutParams = ViewGroup.MarginLayoutParams(175, 190).apply {
                setMargins(8, 8, 8, 14)
            }
        }

        return FolderViewHolder(card)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val category = categories[position]
        val context = holder.layout.context

        holder.layout.removeAllViews()

        holder.layout.setOnClickListener {
            AnimationUtils.press(holder.layout) {
                onCategoryClick(category)
            }
        }

        val iconGrid = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        val row1 = LinearLayout(context).apply {
            gravity = Gravity.CENTER
        }

        val row2 = LinearLayout(context).apply {
            gravity = Gravity.CENTER
        }

        category.apps.take(4).forEachIndexed { index, app ->
            val icon = ImageView(context).apply {
                setImageDrawable(app.icon)
                layoutParams = LinearLayout.LayoutParams(36, 36).apply {
                    setMargins(4, 4, 4, 4)
                }
            }

            if (index < 2) row1.addView(icon) else row2.addView(icon)
        }

        iconGrid.addView(row1)
        iconGrid.addView(row2)

        holder.layout.addView(iconGrid)

        holder.layout.addView(TextView(context).apply {
            text = category.name
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(UiConstants.TEXT_PRIMARY)
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 0)
            maxLines = 1
        })

        holder.layout.addView(TextView(context).apply {
            text = "${category.apps.size} apps"
            textSize = 12f
            setTextColor(UiConstants.TEXT_SECONDARY)
            gravity = Gravity.CENTER
            setPadding(0, 3, 0, 0)
        })
    }

    override fun getItemCount(): Int = categories.size
}