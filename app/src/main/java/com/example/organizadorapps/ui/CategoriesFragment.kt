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
import com.example.organizadorapps.AppCategory
import com.example.organizadorapps.AppRepository
import com.example.organizadorapps.AppUiUtils
import com.example.organizadorapps.CategoryFolderAdapter
import com.example.organizadorapps.CategorySuggestionEngine
import com.example.organizadorapps.UiConstants

class CategoriesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    private lateinit var adapter: CategoryFolderAdapter

    private var categories: List<AppCategory> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val apps = AppRepository
            .getInstalledLaunchableApps(requireContext())

        categories = CategorySuggestionEngine
            .categorizeApps(apps)
            .filter { it.apps.isNotEmpty() }

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
                "Organización inteligente"
            )
        )

        root.addView(
            AppUiUtils.title(
                requireContext(),
                "Categorías"
            )
        )

        root.addView(
            AppUiUtils.subtitle(
                requireContext(),
                "Tus apps agrupadas automáticamente"
            )
        )

        root.addView(searchBox())

        recyclerView = RecyclerView(requireContext()).apply {

            // Cambio importante:
            // ahora 2 columnas premium.
            layoutManager = GridLayoutManager(requireContext(), 2)

            overScrollMode = RecyclerView.OVER_SCROLL_NEVER

            isNestedScrollingEnabled = false

            adapter = CategoryFolderAdapter(
                categories
            ) { category ->

                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                    )
                    .replace(
                        (view?.parent as ViewGroup).id,
                        CategoryDetailFragment(
                            category.name,
                            category.apps
                        )
                    )
                    .addToBackStack(null)
                    .commit()
            }.also {
                this@CategoriesFragment.adapter = it
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
                filterCategories(query)
            }
        )
    }

    private fun filterCategories(query: String) {

        val filtered = if (query.isBlank()) {

            categories

        } else {

            categories.filter { category ->

                category.name.contains(
                    query,
                    ignoreCase = true
                ) ||

                        category.apps.any { app ->
                            app.name.contains(
                                query,
                                ignoreCase = true
                            )
                        }
            }
        }

        recyclerView.adapter = CategoryFolderAdapter(
            filtered
        ) { category ->

            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                .replace(
                    (view?.parent as ViewGroup).id,
                    CategoryDetailFragment(
                        category.name,
                        category.apps
                    )
                )
                .addToBackStack(null)
                .commit()
        }
    }
}