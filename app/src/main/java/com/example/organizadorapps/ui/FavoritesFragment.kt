package com.example.organizadorapps.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadorapps.AppAdapter
import com.example.organizadorapps.FavoritesManager
import com.example.organizadorapps.InstalledApp

class FavoritesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val allApps = getInstalledLaunchableApps()
        val favoritePackages = FavoritesManager.getFavoritePackages(requireContext())
        val favoriteApps = allApps.filter { favoritePackages.contains(it.packageName) }

        val rootLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0F1115"))
            setPadding(24, 42, 24, 24)
        }

        rootLayout.addView(TextView(requireContext()).apply {
            text = "Favoritos"
            textSize = 30f
            setTextColor(Color.WHITE)
        })

        rootLayout.addView(TextView(requireContext()).apply {
            text = if (favoriteApps.isEmpty()) {
                "Mantén presionada una app en Categorías para agregarla aquí"
            } else {
                "${favoriteApps.size} apps favoritas"
            }
            textSize = 14f
            setTextColor(Color.parseColor("#8F96A3"))
            setPadding(0, 8, 0, 24)
        })

        if (favoriteApps.isEmpty()) {
            rootLayout.addView(TextView(requireContext()).apply {
                text = "Todavía no tienes favoritos"
                textSize = 18f
                setTextColor(Color.parseColor("#E5E7EB"))
                setPadding(0, 36, 0, 0)
            })
        } else {
            rootLayout.addView(RecyclerView(requireContext()).apply {
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                adapter = AppAdapter(favoriteApps)
                overScrollMode = RecyclerView.OVER_SCROLL_NEVER

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    260
                )
            })
        }

        return rootLayout
    }

    private fun getInstalledLaunchableApps(): List<InstalledApp> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return requireContext()
            .packageManager
            .queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .map {
                InstalledApp(
                    name = it.loadLabel(requireContext().packageManager).toString(),
                    packageName = it.activityInfo.packageName,
                    icon = it.loadIcon(requireContext().packageManager)
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.name.lowercase() }
    }
}