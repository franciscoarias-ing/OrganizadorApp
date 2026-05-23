package com.example.organizadorapps

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UsageStatsAdapter(
    private val stats: List<UsageAppStat>
) : RecyclerView.Adapter<UsageStatsAdapter.StatViewHolder>() {

    class StatViewHolder(val layout: LinearLayout)
        : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StatViewHolder {

        val context = parent.context

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(24, 24, 24, 24)

            background = AppUiUtils.roundedCard()

            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 18)
            }
        }

        return StatViewHolder(layout)
    }

    override fun onBindViewHolder(holder: StatViewHolder, position: Int) {

        val context = holder.layout.context
        val stat = stats[position]

        holder.layout.removeAllViews()

        holder.layout.addView(ImageView(context).apply {
            setImageDrawable(stat.app.icon)

            layoutParams = LinearLayout.LayoutParams(82, 82)
        })

        holder.layout.addView(LinearLayout(context).apply {

            orientation = LinearLayout.VERTICAL

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginStart = 24
            }

            addView(TextView(context).apply {
                text = stat.app.name
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.WHITE)
            })

            addView(TextView(context).apply {
                text = "${stat.openCount} aperturas"
                textSize = 13f
                setTextColor(AppUiUtils.subtitleColor())
            })
        })
    }

    override fun getItemCount(): Int = stats.size
}