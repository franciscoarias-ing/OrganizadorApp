package com.example.organizadorapps

import android.graphics.Typeface
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
            background = AppUiUtils.roundedCard()

            setPadding(
                AppUiUtils.dp(context, 14),
                AppUiUtils.dp(context, 14),
                AppUiUtils.dp(context, 14),
                AppUiUtils.dp(context, 12)
            )

            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                AppUiUtils.dp(context, 176)
            ).apply {
                setMargins(
                    AppUiUtils.dp(context, 6),
                    AppUiUtils.dp(context, 6),
                    AppUiUtils.dp(context, 6),
                    AppUiUtils.dp(context, 12)
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
            background = AppUiUtils.roundedCardAlt()

            layoutParams = LinearLayout.LayoutParams(
                AppUiUtils.dp(context, 86),
                AppUiUtils.dp(context, 86)
            )
        }

        val row1 = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val row2 = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        if (category.name == "Todas las apps") {
            repeat(2) {
                row1.addView(dot(context))
            }

            repeat(2) {
                row2.addView(dot(context))
            }
        } else {
            val previewApps = category.apps.take(4)

            repeat(4) { index ->
                val iconView = ImageView(context).apply {
                    if (index < previewApps.size) {
                        setImageDrawable(previewApps[index].icon)
                        alpha = 1f
                    } else {
                        alpha = 0.16f
                    }

                    layoutParams = LinearLayout.LayoutParams(
                        AppUiUtils.dp(context, 30),
                        AppUiUtils.dp(context, 30)
                    ).apply {
                        setMargins(
                            AppUiUtils.dp(context, 3),
                            AppUiUtils.dp(context, 3),
                            AppUiUtils.dp(context, 3),
                            AppUiUtils.dp(context, 3)
                        )
                    }
                }

                if (index < 2) {
                    row1.addView(iconView)
                } else {
                    row2.addView(iconView)
                }
            }
        }

        iconGrid.addView(row1)
        iconGrid.addView(row2)

        holder.layout.addView(iconGrid)

        holder.layout.addView(
            TextView(context).apply {
                text = category.name
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(UiConstants.TEXT_PRIMARY)
                gravity = Gravity.CENTER
                includeFontPadding = false
                maxLines = 1

                setPadding(
                    0,
                    AppUiUtils.dp(context, 14),
                    0,
                    0
                )
            }
        )

        holder.layout.addView(
            TextView(context).apply {
                text = "${category.apps.size} apps"
                textSize = 13f
                setTextColor(UiConstants.TEXT_SECONDARY)
                gravity = Gravity.CENTER
                includeFontPadding = false
                maxLines = 1

                setPadding(
                    0,
                    AppUiUtils.dp(context, 6),
                    0,
                    0
                )
            }
        )
    }

    private fun dot(context: android.content.Context): TextView {
        return TextView(context).apply {
            text = "●"
            textSize = 18f
            setTextColor(UiConstants.ACCENT)
            gravity = Gravity.CENTER

            layoutParams = LinearLayout.LayoutParams(
                AppUiUtils.dp(context, 30),
                AppUiUtils.dp(context, 30)
            ).apply {
                setMargins(
                    AppUiUtils.dp(context, 3),
                    AppUiUtils.dp(context, 3),
                    AppUiUtils.dp(context, 3),
                    AppUiUtils.dp(context, 3)
                )
            }
        }
    }

    override fun getItemCount(): Int = categories.size
}