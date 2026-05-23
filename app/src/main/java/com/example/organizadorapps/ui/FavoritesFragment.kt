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
import com.example.organizadorapps.UiConstants

class FavoritesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val allApps =
            AppRepository.getInstalledLaunchableApps(requireContext())

        val favoritePackages =
            FavoritesManager.getFavoritePackages(requireContext())

        val recentPackages =
            RecentAppsManager.getRecentPackageNames(requireContext())

        val favoriteApps = allApps
            .filter { favoritePackages.contains(it.packageName) }
            .sortedBy { app ->
                val index = recentPackages.indexOf(app.packageName)
                if (index == -1) Int.MAX_VALUE else index
            }

        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(UiConstants.BACKGROUND)

            setPadding(
                UiConstants.SCREEN_PADDING,
                UiConstants.TOP_PADDING,
                UiConstants.SCREEN_PADDING,
                24
            )
        }

        root.addView(AppUiUtils.kicker(requireContext(), "Personal"))
        root.addView(AppUiUtils.title(requireContext(), "Favoritos"))

        root.addView(
            AppUiUtils.subtitle(
                requireContext(),
                if (favoriteApps.isEmpty()) {
                    "Todavía no tienes favoritos"
                } else {
                    "${favoriteApps.size} apps favoritas"
                }
            )
        )

        if (favoriteApps.isEmpty()) {

            root.addView(
                AppUiUtils.actionCard(
                    context = requireContext(),
                    title = "Agrega favoritos",
                    subtitle = "Mantén presionada una app para guardarla aquí.",
                    icon = "★"
                ) {}
            )

        } else {

            root.addView(
                RecyclerView(requireContext()).apply {

                    layoutManager =
                        GridLayoutManager(requireContext(), 4)

                    adapter = AppAdapter(favoriteApps)

                    overScrollMode =
                        RecyclerView.OVER_SCROLL_NEVER

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