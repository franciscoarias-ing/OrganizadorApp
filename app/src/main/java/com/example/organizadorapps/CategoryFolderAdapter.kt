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

            // Folder más grande y premium, como la imagen objetivo.
            setPadding(
                AppUiUtils.dp(context, 14),
                AppUiUtils.dp(context, 16),
                AppUiUtils.dp(context, 14),
                AppUiUtils.dp(context, 16)
            )

            background = GradientDrawable().apply {
                setColor(UiConstants.SURFACE)
                cornerRadius = AppUiUtils.dp(context, 22).toFloat()
                setStroke(1, UiConstants.BORDER)
            }

            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                AppUiUtils.dp(context, 168)
            ).apply {
                setMargins(
                    AppUiUtils.dp(context, 7),
                    AppUiUtils.dp(context, 7),
                    AppUiUtils.dp(context, 7),
                    AppUiUtils.dp(context, 10)
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
            background = GradientDrawable().apply {
                setColor(UiConstants.SURFACE_ALT)
                cornerRadius = AppUiUtils.dp(context, 20).toFloat()
            }
            setPadding(
                AppUiUtils.dp(context, 10),
                AppUiUtils.dp(context, 10),
                AppUiUtils.dp(context, 10),
                AppUiUtils.dp(context, 10)
            )
            layoutParams = LinearLayout.LayoutParams(
                AppUiUtils.dp(context, 88),
                AppUiUtils.dp(context, 88)
            )
        }

        val row1 = LinearLayout(context).apply {
            gravity = Gravity.CENTER
        }

        val row2 = LinearLayout(context).apply {
            gravity = Gravity.CENTER
        }

        if (category.name == "Todas las apps") {
            repeat(9) {
                row1.addView(dot(context))
                if (it == 2) {
                    iconGrid.addView(row1)
                }
            }
            repeat(6) {
                row2.addView(dot(context))
            }
            iconGrid.addView(row2)
        } else {
            category.apps.take(4).forEachIndexed { index, app ->
                val icon = ImageView(context).apply {
                    setImageDrawable(app.icon)
                    layoutParams = LinearLayout.LayoutParams(
                        AppUiUtils.dp(context, 30),
                        AppUiUtils.dp(context, 30)
                    ).apply {
                        setMargins(
                            AppUiUtils.dp(context, 4),
                            AppUiUtils.dp(context, 4),
                            AppUiUtils.dp(context, 4),
                            AppUiUtils.dp(context, 4)
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
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(UiConstants.TEXT_PRIMARY)
            gravity = Gravity.CENTER
            includeFontPadding = false
            maxLines = 1
            setPadding(0, AppUiUtils.dp(context, 14), 0, 0)
        })

        holder.layout.addView(TextView(context).apply {
            text = "${category.apps.size} apps"
            textSize = 13f
            setTextColor(UiConstants.TEXT_SECONDARY)
            gravity = Gravity.CENTER
            includeFontPadding = false
            setPadding(0, AppUiUtils.dp(context, 5), 0, 0)
        })
    }

    private fun dot(context: android.content.Context): TextView {
        return TextView(context).apply {
            text = "●"
            textSize = 14f
            setTextColor(UiConstants.ACCENT)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                AppUiUtils.dp(context, 18),
                AppUiUtils.dp(context, 18)
            )
        }
    }

    override fun getItemCount(): Int = categories.size
}