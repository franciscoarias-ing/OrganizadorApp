package com.example.organizadorapps

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(
    private val apps: List<InstalledApp>
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    class AppViewHolder(val layout: LinearLayout) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val context = parent.context

        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1A1F2B"))
                cornerRadius = 28f
                setStroke(1, Color.parseColor("#2B3140"))
            }
            isClickable = true
            isFocusable = true

            layoutParams = ViewGroup.MarginLayoutParams(210, 240).apply {
                setMargins(8, 8, 8, 8)
            }
        }

        return AppViewHolder(card)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        val context = holder.layout.context

        holder.layout.removeAllViews()

        holder.layout.setOnClickListener {
            AppLauncher.openApp(context, app.packageName, app.name)
        }

        holder.layout.addView(ImageView(context).apply {
            setImageDrawable(app.icon)
            layoutParams = LinearLayout.LayoutParams(88, 88)
        })

        holder.layout.addView(TextView(context).apply {
            text = app.name
            textSize = 12f
            setTextColor(Color.parseColor("#E5E7EB"))
            gravity = Gravity.CENTER
            setPadding(0, 14, 0, 0)
            maxLines = 2
        })
    }

    override fun getItemCount(): Int = apps.size
}