package com.example.organizadorapps.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadorapps.AppCategory
import com.example.organizadorapps.AppRepository
import com.example.organizadorapps.AppUiUtils
import com.example.organizadorapps.CategoryAdapter
import com.example.organizadorapps.CategorySuggestionEngine
import com.example.organizadorapps.InstalledApp

class CategoriesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var allApps: List<InstalledApp>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        allApps = AppRepository.getInstalledLaunchableApps(requireContext())

        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(AppUiUtils.backgroundColor)
            setPadding(24, 42, 24, 24)
        }

        root.addView(AppUiUtils.title(requireContext(), "Categorías"))

        root.addView(
            AppUiUtils.subtitle(
                requireContext(),
                "Apps organizadas automáticamente"
            )
        )

        root.addView(searchBox())

        recyclerView = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(requireContext())
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        root.addView(
            recyclerView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        renderCategories(allApps)

        return root
    }

    private fun searchBox(): EditText {
        return AppUiUtils.searchBox(
            requireContext(),
            "Buscar por nombre o paquete..."
        ).apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    val query = s.toString().trim().lowercase()

                    val filteredApps = if (query.isEmpty()) {
                        allApps
                    } else {
                        allApps.filter {
                            it.name.lowercase().contains(query) ||
                                    it.packageName.lowercase().contains(query)
                        }
                    }

                    renderCategories(filteredApps)
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun renderCategories(apps: List<InstalledApp>) {
        val categories: List<AppCategory> =
            CategorySuggestionEngine.categorizeApps(apps)

        recyclerView.adapter = CategoryAdapter(
            categories.filter { it.apps.isNotEmpty() }
        )
    }
}