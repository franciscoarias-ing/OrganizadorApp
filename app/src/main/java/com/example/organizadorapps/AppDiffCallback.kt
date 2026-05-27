package com.example.organizadorapps

import androidx.recyclerview.widget.DiffUtil

class AppDiffCallback(
    private val oldApps: List<InstalledApp>,
    private val newApps: List<InstalledApp>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldApps.size

    override fun getNewListSize(): Int = newApps.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldApps[oldItemPosition].packageName == newApps[newItemPosition].packageName
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldApp = oldApps[oldItemPosition]
        val newApp = newApps[newItemPosition]
        return oldApp.packageName == newApp.packageName && oldApp.name == newApp.name
    }
}
