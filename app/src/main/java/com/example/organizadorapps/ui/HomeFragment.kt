package com.example.organizadorapps.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadorapps.AppAdapter
import com.example.organizadorapps.AppCategory
import com.example.organizadorapps.AppRepository
import com.example.organizadorapps.AppUiUtils
import com.example.organizadorapps.CategoryFolderAdapter
import com.example.organizadorapps.CategorySuggestionEngine
import com.example.organizadorapps.FavoritesManager
import com.example.organizadorapps.RecentAppsManager
import com.example.organizadorapps.UiConstants

class HomeFragment : Fragment() {

    private lateinit var allApps: List<com.example.organizadorapps.InstalledApp>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        allApps = AppRepository.getInstalledLaunchableApps(requireContext())

        val favoritePackages = FavoritesManager.getFavoritePackages(requireContext())
        val favoriteApps = allApps.filter { favoritePackages.contains(it.packageName) }

        val recentPackages = RecentAppsManager.getRecentPackageNames(requireContext())
        val recentApps = recentPackages.mapNotNull { packageName ->
            allApps.find { it.packageName == packageName }
        }

        val categories = CategorySuggestionEngine
            .categorizeApps(allApps)
            .filter { it.apps.isNotEmpty() }
            .toMutableList()

        categories.add(AppCategory("Todas las apps", allApps))

        val scroll = ScrollView(requireContext()).apply {
            setBackgroundColor(UiConstants.BACKGROUND)
            overScrollMode = View.OVER_SCROLL_NEVER
            isFillViewport = true
        }

        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                AppUiUtils.dp(requireContext(), 22),
                AppUiUtils.dp(requireContext(), 34),
                AppUiUtils.dp(requireContext(), 22),
                AppUiUtils.dp(requireContext(), 26)
            )
        }

        root.addView(AppUiUtils.title(requireContext(), "Inicio Inteligente"))

        root.addView(
            AppUiUtils.searchButton(
                context = requireContext(),
                hint = "Buscar apps..."
            ) {
                openFragment(AllAppsFragment())
            }
        )

        root.addView(quickActions())

        addRecentSection(root, recentApps)
        addFavoritesSection(root, favoriteApps)
        addCategorySection(root, categories)

        scroll.addView(root)
        return scroll
    }

    private fun quickActions(): LinearLayout {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            background = AppUiUtils.glassCard()

            setPadding(
                AppUiUtils.dp(requireContext(), 10),
                AppUiUtils.dp(requireContext(), 14),
                AppUiUtils.dp(requireContext(), 10),
                AppUiUtils.dp(requireContext(), 14)
            )

            minimumHeight = AppUiUtils.dp(requireContext(), 112)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, AppUiUtils.dp(requireContext(), 18))
            }

            addView(
                AppUiUtils.quickAction(requireContext(), "⌘", "Todas las\napps") {
                    openFragment(AllAppsFragment())
                }
            )

            addView(AppUiUtils.verticalDivider(requireContext()))

            addView(
                AppUiUtils.quickAction(requireContext(), "☆", "Añadir\nfavorito") {
                    Toast.makeText(
                        requireContext(),
                        "Mantén presionada una app para agregarla a favoritos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )

            addView(AppUiUtils.verticalDivider(requireContext()))

            addView(
                AppUiUtils.quickAction(requireContext(), "▣", "Escanear\napps") {
                    Toast.makeText(
                        requireContext(),
                        "Tus apps ya fueron detectadas automáticamente",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )

            addView(AppUiUtils.verticalDivider(requireContext()))

            addView(
                AppUiUtils.quickAction(requireContext(), "✦", "Sugerir") {
                    Toast.makeText(
                        requireContext(),
                        "Categorías sugeridas automáticamente",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    private fun addRecentSection(
        root: LinearLayout,
        apps: List<com.example.organizadorapps.InstalledApp>
    ) {
        root.addView(
            AppUiUtils.sectionRow(requireContext(), "Recientes", "Ver todos", 18, 12) {
                openFragment(AllAppsFragment())
            }
        )

        if (apps.isEmpty()) {
            root.addView(AppUiUtils.miniEmpty(requireContext(), "Abre apps desde OrganizadorApp para verlas aquí."))
            return
        }

        root.addView(
            RecyclerView(requireContext()).apply {
                layoutManager = LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                adapter = AppAdapter(apps.take(10), AppAdapter.Mode.RECENT)
                overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                isNestedScrollingEnabled = false

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    AppUiUtils.dp(requireContext(), 78)
                )
            }
        )
    }

    private fun addFavoritesSection(
        root: LinearLayout,
        apps: List<com.example.organizadorapps.InstalledApp>
    ) {
        root.addView(
            AppUiUtils.sectionRow(requireContext(), "Favoritos rápidos", "Ver todos", 22, 12) {
                openFragment(FavoritesFragment())
            }
        )

        root.addView(
            RecyclerView(requireContext()).apply {
                layoutManager = LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )

                adapter = AppAdapter(
                    apps = apps.take(8),
                    mode = AppAdapter.Mode.FAVORITE,
                    showAddFavorite = true,
                    onAddFavoriteClick = {
                        Toast.makeText(
                            requireContext(),
                            "Mantén presionada una app para agregarla a favoritos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )

                overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                isNestedScrollingEnabled = false

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    AppUiUtils.dp(requireContext(), 118)
                )
            }
        )
    }

    private fun addCategorySection(
        root: LinearLayout,
        categories: List<AppCategory>
    ) {
        root.addView(
            AppUiUtils.sectionRow(requireContext(), "Mis categorías", "Editar", 24, 12) {
                Toast.makeText(requireContext(), "Edición de categorías próximamente", Toast.LENGTH_SHORT).show()
            }
        )

        root.addView(
            RecyclerView(requireContext()).apply {
                layoutManager = GridLayoutManager(requireContext(), 2)

                adapter = CategoryFolderAdapter(categories) { category ->
                    if (category.name == "Todas las apps") {
                        openFragment(AllAppsFragment())
                    } else {
                        openFragment(CategoryDetailFragment(category.name, category.apps))
                    }
                }

                overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                isNestedScrollingEnabled = false

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
        )
    }

    private fun openFragment(fragment: Fragment) {
        val containerId = (view?.parent as? ViewGroup)?.id ?: return

        parentFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(containerId, fragment)
            .addToBackStack(null)
            .commit()
    }
}