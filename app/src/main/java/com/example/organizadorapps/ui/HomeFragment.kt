package com.example.organizadorapps.ui
import com.example.organizadorapps.CategoryGridItem
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
import com.example.organizadorapps.AppLauncher
import com.example.organizadorapps.AppRepository
import com.example.organizadorapps.AppUiUtils
import com.example.organizadorapps.CategoryFolderAdapter
import com.example.organizadorapps.CategorySuggestionEngine
import com.example.organizadorapps.ExpandableCategoryItem
import com.example.organizadorapps.FavoritesManager
import com.example.organizadorapps.InstalledApp
import com.example.organizadorapps.R
import com.example.organizadorapps.RecentAppsManager
import com.example.organizadorapps.UiConstants

class HomeFragment : Fragment() {

    private lateinit var allApps: List<InstalledApp>

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
            .map { category ->
                ExpandableCategoryItem(
                    category = category,
                    isExpanded = false
                )
            }
            .toMutableList()

        categories.add(
            ExpandableCategoryItem(
                category = AppCategory("Todas las apps", allApps),
                isExpanded = false
            )
        )

        val scroll = ScrollView(requireContext()).apply {
            setBackgroundColor(UiConstants.BACKGROUND)
            overScrollMode = View.OVER_SCROLL_NEVER
            isFillViewport = true
        }

        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(
                AppUiUtils.dp(requireContext(), 14),
                AppUiUtils.dp(requireContext(), 12),
                AppUiUtils.dp(requireContext(), 14),
                AppUiUtils.dp(requireContext(), 12)
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
                AppUiUtils.quickActionsHorizontalPadding(requireContext()),
                AppUiUtils.quickActionsVerticalPadding(requireContext()),
                AppUiUtils.quickActionsHorizontalPadding(requireContext()),
                AppUiUtils.quickActionsVerticalPadding(requireContext())
            )

            minimumHeight = AppUiUtils.quickActionsMinHeight(requireContext())

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, AppUiUtils.dp(requireContext(), 18))
            }

            addView(
                AppUiUtils.quickAction(
                    context = requireContext(),
                    iconRes = R.drawable.ic_action_all_apps,
                    label = "Todas las apps"
                ) {
                    openFragment(AllAppsFragment())
                }
            )

            addView(AppUiUtils.verticalDivider(requireContext()))

            addView(
                AppUiUtils.quickAction(
                    context = requireContext(),
                    iconRes = R.drawable.ic_action_favorite,
                    label = "Añadir favorito"
                ) {
                    Toast.makeText(
                        requireContext(),
                        "Mantén presionada una app para agregarla a favoritos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )

            addView(AppUiUtils.verticalDivider(requireContext()))

            addView(
                AppUiUtils.quickAction(
                    context = requireContext(),
                    iconRes = R.drawable.ic_action_scan,
                    label = "Escanear apps"
                ) {
                    Toast.makeText(
                        requireContext(),
                        "Tus apps ya fueron detectadas automáticamente",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )

            addView(AppUiUtils.verticalDivider(requireContext()))

            addView(
                AppUiUtils.quickAction(
                    context = requireContext(),
                    iconRes = R.drawable.ic_action_magic,
                    label = "Más Categorías"
                ) {
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
        apps: List<InstalledApp>
    ) {
        root.addView(
            AppUiUtils.sectionRow(requireContext(), "Recientes", "Ver todos", 18, 12) {
                openFragment(AllAppsFragment())
            }
        )

        if (apps.isEmpty()) {
            root.addView(
                AppUiUtils.miniEmpty(
                    requireContext(),
                    "Abre apps desde OrganizadorApp para verlas aquí."
                )
            )
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
        apps: List<InstalledApp>
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
        categories: MutableList<ExpandableCategoryItem>
    ) {

        root.addView(
            AppUiUtils.sectionRow(
                requireContext(),
                "Mis categorías",
                "Editar",
                24,
                12
            ) {
                Toast.makeText(
                    requireContext(),
                    "Edición de categorías próximamente",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        val categoryRecycler = RecyclerView(requireContext())

        fun buildGridItems(): MutableList<CategoryGridItem> {

            val result = mutableListOf<CategoryGridItem>()

            categories.forEach { category ->

                result.add(
                    CategoryGridItem.Folder(category)
                )

                if (category.isExpanded) {
                    result.add(
                        CategoryGridItem.ExpandedPanel(category)
                    )
                }
            }

            return result
        }

        var gridItems = buildGridItems()

        val gridLayoutManager = GridLayoutManager(requireContext(), 4)

        gridLayoutManager.spanSizeLookup =
            object : GridLayoutManager.SpanSizeLookup() {

                override fun getSpanSize(position: Int): Int {

                    return when (gridItems[position]) {

                        is CategoryGridItem.ExpandedPanel -> 4

                        else -> 1
                    }
                }
            }

        lateinit var adapter: CategoryFolderAdapter

        adapter = CategoryFolderAdapter(
            items = gridItems,

            onFolderClick = { clickedItem ->

                categories.forEach { category ->
                    category.isExpanded = false
                }

                clickedItem.isExpanded = true

                gridItems = buildGridItems()

                categoryRecycler.post {
                    categoryRecycler.adapter =
                        CategoryFolderAdapter(
                            items = gridItems,

                            onFolderClick = adapter.onFolderClick,

                            onCollapseClick = adapter.onCollapseClick,

                            onAppClick = adapter.onAppClick,

                            onAllAppsClick = adapter.onAllAppsClick
                        )
                }
            },

            onCollapseClick = {

                categories.forEach {
                    it.isExpanded = false
                }

                gridItems = buildGridItems()

                categoryRecycler.post {
                    categoryRecycler.adapter =
                        CategoryFolderAdapter(
                            items = gridItems,

                            onFolderClick = adapter.onFolderClick,

                            onCollapseClick = adapter.onCollapseClick,

                            onAppClick = adapter.onAppClick,

                            onAllAppsClick = adapter.onAllAppsClick
                        )
                }
            },

            onAppClick = { app ->

                RecentAppsManager.registerAppOpen(
                    requireContext(),
                    app
                )

                AppLauncher.openApp(
                    requireContext(),
                    app.packageName,
                    app.name
                )
            },

            onAllAppsClick = {
                openFragment(AllAppsFragment())
            }
        )

        categoryRecycler.apply {

            layoutManager = gridLayoutManager

            this.adapter = adapter

            overScrollMode = RecyclerView.OVER_SCROLL_NEVER

            isNestedScrollingEnabled = false

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        root.addView(categoryRecycler)
    }

    private fun openFragment(fragment: Fragment) {
        val containerId = (requireView().parent as ViewGroup).id

        parentFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(containerId, fragment)
            .addToBackStack(null)
            .commit()
    }
}