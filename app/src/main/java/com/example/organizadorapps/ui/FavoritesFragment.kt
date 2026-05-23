package com.example.organizadorapps.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadorapps.AppAdapter
import com.example.organizadorapps.AppRepository
import com.example.organizadorapps.AppUiUtils
import com.example.organizadorapps.FavoritesManager
import com.example.organizadorapps.RecentAppsManager

class FavoritesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val allApps = AppRepository.getInstalledLaunchableApps(requireContext())
        val favoritePackages = FavoritesManager.getFavoritePackages(requireContext())
        val recentPackages = RecentAppsManager.getRecentPackageNames(requireContext())

        val favoriteApps = allApps
            .filter { favoritePackages.contains(it.packageName) }
            .sortedBy { app ->
                val index = recentPackages.indexOf(app.packageName)
                if (index == -1) Int.MAX_VALUE else index
            }

        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(AppUiUtils.backgroundColor)
            setPadding(24, 42, 24, 24)
        }

        root.addView(AppUiUtils.title(requireContext(), "Favoritos"))

        root.addView(
            AppUiUtils.subtitle(
                requireContext(),
                if (favoriteApps.isEmpty()) {
                    "Accesos rápidos todavía vacíos"
                } else {
                    "${favoriteApps.size} apps favoritas"
                }
            )
        )

        if (favoriteApps.isEmpty()) {
            root.addView(
                AppUiUtils.subtitle(
                    requireContext(),
                    "Mantén presionada una app en Categorías o Todas las apps para guardarla aquí."
                )
            )
        } else {
            root.addView(
                RecyclerView(requireContext()).apply {
                    layoutManager = GridLayoutManager(requireContext(), 4)
                    adapter = AppAdapter(favoriteApps)
                    overScrollMode = RecyclerView.OVER_SCROLL_NEVER

                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                    )
                }
            )
        }

        return root
    }
}