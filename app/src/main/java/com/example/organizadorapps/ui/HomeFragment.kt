package com.example.organizadorapps.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadorapps.AppAdapter
import com.example.organizadorapps.AppRepository
import com.example.organizadorapps.AppUiUtils
import com.example.organizadorapps.FavoritesManager
import com.example.organizadorapps.RecentAppsManager

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val allApps = AppRepository.getInstalledLaunchableApps(requireContext())

        val favoritePackages = FavoritesManager.getFavoritePackages(requireContext())
        val favoriteApps = allApps.filter {
            favoritePackages.contains(it.packageName)
        }

        val recentPackages = RecentAppsManager.getRecentPackageNames(requireContext())
        val recentApps = recentPackages.mapNotNull { packageName ->
            allApps.find { it.packageName == packageName }
        }

        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(AppUiUtils.backgroundColor)
            setPadding(24, 42, 24, 24)
        }

        root.addView(AppUiUtils.title(requireContext(), "Inicio"))

        root.addView(
            AppUiUtils.subtitle(
                requireContext(),
                "Tu hub inteligente de acceso rápido"
            )
        )

        root.addView(
            android.widget.TextView(requireContext()).apply {
                text = "Ver todas las apps"
                textSize = 18f
                setTextColor(AppUiUtils.textColor)
                setPadding(32, 28, 32, 28)
                background = AppUiUtils.roundedCard()

                setOnClickListener {
                    val containerId = (view?.parent as? ViewGroup)?.id
                        ?: return@setOnClickListener

                    parentFragmentManager.beginTransaction()
                        .replace(containerId, AllAppsFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }
        )

        addAppSection(
            root = root,
            title = "Favoritos rápidos",
            emptyText = "Mantén presionada una app para agregarla aquí.",
            apps = favoriteApps.take(10)
        )

        addAppSection(
            root = root,
            title = "Recientes",
            emptyText = "Abre apps desde OrganizadorApp para verlas aquí.",
            apps = recentApps.take(10)
        )

        root.addView(
            AppUiUtils.subtitle(
                requireContext(),
                "Próximamente: sugerencias contextuales y apps más usadas."
            )
        )

        return root
    }

    private fun addAppSection(
        root: LinearLayout,
        title: String,
        emptyText: String,
        apps: List<com.example.organizadorapps.InstalledApp>
    ) {
        root.addView(
            android.widget.TextView(requireContext()).apply {
                text = title
                textSize = 18f
                setTextColor(AppUiUtils.textColor)
                setPadding(0, 30, 0, 14)
            }
        )

        if (apps.isEmpty()) {
            root.addView(
                AppUiUtils.subtitle(
                    requireContext(),
                    emptyText
                )
            )
        } else {
            root.addView(
                RecyclerView(requireContext()).apply {
                    layoutManager = LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    adapter = AppAdapter(apps)
                    overScrollMode = RecyclerView.OVER_SCROLL_NEVER

                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        225
                    )
                }
            )
        }
    }
}