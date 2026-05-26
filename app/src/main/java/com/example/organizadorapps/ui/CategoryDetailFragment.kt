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
import com.example.organizadorapps.AppUiUtils
import com.example.organizadorapps.InstalledApp
import com.example.organizadorapps.UiConstants

class CategoryDetailFragment(
    private val categoryName: String,
    private val apps: List<InstalledApp>
) : Fragment() {

    private lateinit var adapter: AppAdapter
    private lateinit var recyclerView: RecyclerView

    private val filteredApps = mutableListOf<InstalledApp>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        filteredApps.clear()
        filteredApps.addAll(apps.sortedBy { it.name.lowercase() })

        val scroll = ScrollView(requireContext()).apply {
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
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
                "Categoría"
            )
        )

        root.addView(
            AppUiUtils.title(
                requireContext(),
                categoryName
            )
        )

        root.addView(
            AppUiUtils.subtitle(
                requireContext(),
                "${apps.size} apps encontradas"
            )
        )

        root.addView(searchBox())

        recyclerView = RecyclerView(requireContext()).apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            isNestedScrollingEnabled = false

            adapter = AppAdapter(
                filteredApps,
                AppAdapter.Mode.DEFAULT
            ).also {
                this@CategoryDetailFragment.adapter = it
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
            hintValue = "Buscar en $categoryName...",
            onTextChanged = { query ->
                filterApps(query)
            }
        )
    }

    private fun filterApps(query: String) {
        filteredApps.clear()

        if (query.isBlank()) {
            filteredApps.addAll(apps.sortedBy { it.name.lowercase() })
        } else {
            filteredApps.addAll(
                apps.filter {
                    it.name.contains(query, ignoreCase = true)
                }.sortedBy { it.name.lowercase() }
            )
        }

        adapter.notifyDataSetChanged()
    }
}