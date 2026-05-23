package com.example.organizadorapps

import android.graphics.Color
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private val categories: List<AppCategory>
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(val layout: LinearLayout) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val context = parent.context

        val section = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 20, 0, 10)
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            )
        }

        return CategoryViewHolder(section)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        val context = holder.layout.context

        holder.layout.removeAllViews()

        holder.layout.addView(TextView(context).apply {
            text = category.name
            textSize = 20f
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, 12)
        })

        holder.layout.addView(RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = AppAdapter(category.apps)
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        })
    }

    override fun getItemCount(): Int = categories.size
}