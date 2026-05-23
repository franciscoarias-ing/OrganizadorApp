package com.example.organizadorapps.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadorapps.AppAdapter
import com.example.organizadorapps.AppRepository
import com.example.organizadorapps.AppUiUtils
import com.example.organizadorapps.FavoritesManager
import com.example.organizadorapps.InstalledApp
import com.example.organizadorapps.UiConstants

class FavoritesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppAdapter

    private var favoriteApps: List<InstalledApp> = emptyList()
    private val filteredApps = mutableListOf<InstalledApp>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val allApps = AppRepository.getInstalledLaunchableApps(requireContext())
        val favoritePackages = FavoritesManager.getFavoritePackages(requireContext())

        favoriteApps = allApps
            .filter { favoritePackages.contains(it.packageName) }
            .sortedBy { it.name.lowercase() }

        filteredApps.clear()
        filteredApps.addAll(favoriteApps)

        val scroll = ScrollView(requireContext()).apply {
            setBackgroundColor(UiConstants.BACKGROUND)
            overScrollMode = View.OVER_SCROLL_NEVER
            isFillViewport = true
        }

        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                AppUiUtils.dp(requireContext(), 22),
                AppUiUtils.dp(requireContext(), 42),
                AppUiUtils.dp(requireContext(), 22),
                AppUiUtils.dp(requireContext(), 24)
            )
        }

        root.addView(
            AppUiUtils.smallGreeting(
                requireContext(),
                "Acceso rápido"
            )
        )

        root.addView(
            AppUiUtils.title(
                requireContext(),
                "Favoritos"
            )
        )

        root.addView(
            AppUiUtils.subtitle(
                requireContext(),
                "${favoriteApps.size} apps favoritas"
            )
        )

        root.addView(searchBox())

        if (favoriteApps.isEmpty()) {
            root.addView(
                AppUiUtils.miniEmpty(
                    requireContext(),
                    "Mantén presionada una app para agregarla a favoritos."
                )
            )
        } else {
            recyclerView = RecyclerView(requireContext()).apply {
                layoutManager = GridLayoutManager(requireContext(), 2)
                overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                isNestedScrollingEnabled = false

                adapter = AppAdapter(
                    filteredApps,
                    AppAdapter.Mode.DEFAULT
                ).also {
                    this@FavoritesFragment.adapter = it
                }

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            root.addView(recyclerView)
        }

        scroll.addView(root)
        return scroll
    }

    private fun searchBox(): EditText {
        return AppUiUtils.searchBox(
            context = requireContext(),
            hintValue = "Buscar favoritos...",
            onTextChanged = { query ->
                filterFavorites(query)
            }
        )
    }

    private fun filterFavorites(query: String) {
        if (!::adapter.isInitialized) return

        filteredApps.clear()

        if (query.isBlank()) {
            filteredApps.addAll(favoriteApps)
        } else {
            filteredApps.addAll(
                favoriteApps.filter {
                    it.name.contains(query, ignoreCase = true)
                }
            )
        }

        adapter.notifyDataSetChanged()
    }
}