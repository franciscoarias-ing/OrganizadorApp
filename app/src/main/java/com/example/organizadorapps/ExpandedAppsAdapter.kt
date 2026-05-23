package com.example.organizadorapps

import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExpandedAppsAdapter(
    private val apps: List<InstalledApp>,
    private val onAppClick: (InstalledApp) -> Unit
) : RecyclerView.Adapter<ExpandedAppsAdapter.ExpandedAppViewHolder>() {

    class ExpandedAppViewHolder(val root: LinearLayout) : RecyclerView.ViewHolder(root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpandedAppViewHolder {
        val context = parent.context

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true

            setPadding(
                AppUiUtils.dp(context, 6),
                AppUiUtils.dp(context, 8),
                AppUiUtils.dp(context, 6),
                AppUiUtils.dp(context, 8)
            )

            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        return ExpandedAppViewHolder(root)
    }

    override fun onBindViewHolder(holder: ExpandedAppViewHolder, position: Int) {
        val app = apps[position]
        val context = holder.root.context

        holder.root.removeAllViews()

        holder.root.setOnClickListener {
            AnimationUtils.press(holder.root) {
                onAppClick(app)
            }
        }

        holder.root.addView(
            ImageView(context).apply {
                setImageDrawable(app.icon)

                layoutParams = LinearLayout.LayoutParams(
                    AppUiUtils.dp(context, 34),
                    AppUiUtils.dp(context, 34)
                )
            }
        )

        holder.root.addView(
            TextView(context).apply {
                text = app.name
                textSize = 11f
                typeface = Typeface.DEFAULT
                setTextColor(UiConstants.TEXT_PRIMARY)
                gravity = Gravity.CENTER
                maxLines = 1
                includeFontPadding = false

                setPadding(
                    0,
                    AppUiUtils.dp(context, 6),
                    0,
                    0
                )
            }
        )
    }

    override fun getItemCount(): Int = apps.size
}