package com.example.organizadorapps.ui

import android.os.Bundle
import android.view.Gravity
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
import com.example.organizadorapps.InstalledApp
import com.example.organizadorapps.UiConstants

class AllAppsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    private var allApps: List<InstalledApp> = emptyList()

    private var filteredApps: MutableList<InstalledApp> = mutableListOf()

    private lateinit var adapter: AppAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        allApps = AppRepository.getInstalledLaunchableApps(requireContext())
            .sortedBy { it.name.lowercase() }

        filteredApps = allApps.toMutableList()

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
                "Explora tus apps"
            )
        )

        root.addView(
            AppUiUtils.title(
                requireContext(),
                "Todas las apps"
            )
        )

        root.addView(
            AppUiUtils.subtitle(
                requireContext(),
                "${allApps.size} aplicaciones detectadas automáticamente"
            )
        )

        root.addView(searchBox())

        recyclerView = RecyclerView(requireContext()).apply {

            // 2 columnas premium.
            layoutManager = GridLayoutManager(requireContext(), 2)

            overScrollMode = RecyclerView.OVER_SCROLL_NEVER

            isNestedScrollingEnabled = false

            adapter = AppAdapter(
                filteredApps,
                AppAdapter.Mode.GRID
            ).also {
                this@AllAppsFragment.adapter = it
            }

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        root.addView(recyclerView)

        scroll.addView(root)

        return scroll
    }

    private fun searchBox(): EditText {

        return AppUiUtils.searchBox(
            context = requireContext(),
            hintValue = "Buscar apps...",
            onTextChanged = { query ->
                filterApps(query)
            }
        )
    }

    private fun filterApps(query: String) {

        filteredApps.clear()

        if (query.isBlank()) {

            filteredApps.addAll(allApps)

        } else {

            filteredApps.addAll(
                allApps.filter {
                    it.name.contains(
                        query,
                        ignoreCase = true
                    )
                }
            )
        }

        adapter.notifyDataSetChanged()
    }
}