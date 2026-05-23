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
            isClickable = true
            isFocusable = true

            // Sin fondo externo.
            background = null

            setPadding(
                AppUiUtils.dp(context, 2),
                AppUiUtils.dp(context, 2),
                AppUiUtils.dp(context, 2),
                AppUiUtils.dp(context, 2)
            )

            layoutParams = ViewGroup.MarginLayoutParams(
                AppUiUtils.dp(context, 76),
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(
                    AppUiUtils.dp(context, 2),
                    0,
                    AppUiUtils.dp(context, 8),
                    0
                )
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

            // Sin fondo interno. Esto elimina el bloque oscuro detrás de los íconos.
            background = null

            layoutParams = LinearLayout.LayoutParams(
                AppUiUtils.dp(context, 52),
                AppUiUtils.dp(context, 52)
            )
        }

        if (category.name == "Todas las apps") {
            val row1 = LinearLayout(context).apply { gravity = Gravity.CENTER }
            val row2 = LinearLayout(context).apply { gravity = Gravity.CENTER }
            val row3 = LinearLayout(context).apply { gravity = Gravity.CENTER }

            repeat(3) { row1.addView(dot(context)) }
            repeat(3) { row2.addView(dot(context)) }
            repeat(3) { row3.addView(dot(context)) }

            iconGrid.addView(row1)
            iconGrid.addView(row2)
            iconGrid.addView(row3)
        } else {
            val row1 = LinearLayout(context).apply { gravity = Gravity.CENTER }
            val row2 = LinearLayout(context).apply { gravity = Gravity.CENTER }

            category.apps.take(4).forEachIndexed { index, app ->
                val icon = ImageView(context).apply {
                    setImageDrawable(app.icon)

                    layoutParams = LinearLayout.LayoutParams(
                        AppUiUtils.dp(context, 22),
                        AppUiUtils.dp(context, 22)
                    ).apply {
                        setMargins(
                            AppUiUtils.dp(context, 2),
                            AppUiUtils.dp(context, 2),
                            AppUiUtils.dp(context, 2),
                            AppUiUtils.dp(context, 2)
                        )
                    }
                }

                if (index < 2) row1.addView(icon) else row2.addView(icon)
            }

            iconGrid.addView(row1)
            iconGrid.addView(row2)
        }

        holder.layout.addView(iconGrid)

        holder.layout.addView(TextView(context).apply {
            text = category.name
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(UiConstants.TEXT_PRIMARY)
            gravity = Gravity.CENTER
            includeFontPadding = false
            maxLines = 1
            setPadding(0, AppUiUtils.dp(context, 5), 0, 0)
        })

        holder.layout.addView(TextView(context).apply {
            text = "${category.apps.size} apps"
            textSize = 9f
            setTextColor(UiConstants.TEXT_SECONDARY)
            gravity = Gravity.CENTER
            includeFontPadding = false
            maxLines = 1
            setPadding(0, AppUiUtils.dp(context, 2), 0, 0)
        })
    }

    private fun dot(context: android.content.Context): TextView {
        return TextView(context).apply {
            text = "●"
            textSize = 8f
            setTextColor(UiConstants.ACCENT)
            gravity = Gravity.CENTER

            layoutParams = LinearLayout.LayoutParams(
                AppUiUtils.dp(context, 11),
                AppUiUtils.dp(context, 11)
            )
        }
    }

    override fun getItemCount(): Int = categories.size
}