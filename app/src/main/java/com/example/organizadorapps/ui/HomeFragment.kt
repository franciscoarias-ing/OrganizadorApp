package com.example.organizadorapps.ui

import com.example.organizadorapps.AnimationUtils
import com.example.organizadorapps.ExpandedAppsAdapter
import android.graphics.Color
import android.graphics.Typeface

import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.transition.AutoTransition
import android.transition.TransitionManager
import androidx.recyclerview.widget.DefaultItemAnimator
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
                setMargins(0, 0, 0, AppUiUtils.dp(requireContext(), 12))
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
            TextView(requireContext()).apply {
                text = "Recientes"
                textSize = 18f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(Color.WHITE)

                setPadding(
                    0,
                    0,
                    0,
                    AppUiUtils.dp(requireContext(), 10)
                )
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

                adapter = AppAdapter(
                    apps.take(10),
                    mode = AppAdapter.Mode.RECENT
                )

                overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                isNestedScrollingEnabled = false

                setPadding(0, 0, AppUiUtils.dp(requireContext(), 6), 0)
                clipToPadding = false

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    AppUiUtils.dp(requireContext(), 66)
                ).apply {
                    bottomMargin = AppUiUtils.dp(requireContext(), 12)
                }
            }
        )
    }
    private fun addFavoritesSection(
        root: LinearLayout,
        apps: List<InstalledApp>
    ) {

        root.addView(
            TextView(requireContext()).apply {
                text = "Favoritos rápidos"
                textSize = 18f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(Color.WHITE)

                setPadding(
                    0,
                    0,
                    0,
                    AppUiUtils.dp(requireContext(), 10)
                )
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

                setPadding(
                    0,
                    0,
                    AppUiUtils.dp(requireContext(), 6),
                    0
                )

                clipToPadding = false

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    AppUiUtils.dp(requireContext(), 66)
                ).apply {
                    bottomMargin = AppUiUtils.dp(requireContext(), 12)
                }
            }
        )
    }


    private fun addCategorySection(
        root: LinearLayout,
        categories: MutableList<ExpandableCategoryItem>
    ) {
        root.addView(
            AppUiUtils.sectionRow(requireContext(), "Mis categorías", "Editar", 0, 4) {
                Toast.makeText(
                    requireContext(),
                    "Edición de categorías próximamente",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        val categoriesContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val rowViews = mutableListOf<LinearLayout>()
        val folderViews = mutableMapOf<ExpandableCategoryItem, LinearLayout>()
        var expandedPanel: View? = null
        var expandedItem: ExpandableCategoryItem? = null

        fun fillPreviewIcons(grid: GridLayout, apps: List<InstalledApp>, iconSizeDp: Int) {
            grid.removeAllViews()

            apps.take(4).forEach { app ->
                val icon = ImageView(requireContext()).apply {
                    setImageDrawable(app.icon)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    layoutParams = ViewGroup.MarginLayoutParams(
                        AppUiUtils.dp(requireContext(), iconSizeDp),
                        AppUiUtils.dp(requireContext(), iconSizeDp)
                    ).apply {
                        setMargins(
                            AppUiUtils.dp(requireContext(), 2),
                            AppUiUtils.dp(requireContext(), 2),
                            AppUiUtils.dp(requireContext(), 2),
                            AppUiUtils.dp(requireContext(), 2)
                        )
                    }
                }

                grid.addView(icon)
            }
        }

        fun updateFolderStates() {
            folderViews.forEach { (item, folderCard) ->
                folderCard.alpha = if (item == expandedItem) 0.92f else 1f
            }
        }

        fun createExpandedPanel(item: ExpandableCategoryItem): View {
            val panel = layoutInflater.inflate(
                R.layout.item_category_expanded_panel,
                categoriesContainer,
                false
            ).apply {
                tag = "expanded_panel"
                alpha = 0f
                translationY = -AppUiUtils.dp(requireContext(), 14).toFloat()
            }

            val expandedPreviewGrid = panel.findViewById<GridLayout>(R.id.expandedPreviewGrid)
            val txtExpandedCategoryName = panel.findViewById<TextView>(R.id.txtExpandedCategoryName)
            val txtExpandedCategoryCount =
                panel.findViewById<TextView>(R.id.txtExpandedCategoryCount)
            val btnCollapseCategory = panel.findViewById<TextView>(R.id.btnCollapseCategory)
            val recyclerPanelApps = panel.findViewById<RecyclerView>(R.id.recyclerPanelApps)

            txtExpandedCategoryName.text = item.category.name
            txtExpandedCategoryCount.text = "${item.category.apps.size} apps"
            btnCollapseCategory.text = "⌃"

            fillPreviewIcons(expandedPreviewGrid, item.category.apps, 20)

            recyclerPanelApps.apply {
                layoutManager = GridLayoutManager(requireContext(), 4)
                adapter = ExpandedAppsAdapter(item.category.apps) { app: InstalledApp ->
                    RecentAppsManager.registerAppOpen(requireContext(), app)
                    AppLauncher.openApp(requireContext(), app.packageName, app.name)
                }
                overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                isNestedScrollingEnabled = false
            }

            btnCollapseCategory.setOnClickListener {
                val panelToRemove = expandedPanel ?: return@setOnClickListener

                panelToRemove.animate()
                    .alpha(0f)
                    .translationY(-AppUiUtils.dp(requireContext(), 12).toFloat())
                    .setDuration(180)
                    .withEndAction {
                        categoriesContainer.removeView(panelToRemove)
                        expandedPanel = null
                        expandedItem = null
                        categories.forEach { it.isExpanded = false }
                        updateFolderStates()
                    }
                    .start()
            }

            panel.post {
                panel.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(230)
                    .start()
            }

            return panel
        }

        fun insertPanelBelowItem(item: ExpandableCategoryItem) {
            val rowIndex = categories.indexOf(item) / 4
            val rowView = rowViews.getOrNull(rowIndex) ?: return

            val rowPositionInContainer = categoriesContainer.indexOfChild(rowView)
            val insertIndex = rowPositionInContainer + 1

            val newPanel = createExpandedPanel(item)

            TransitionManager.beginDelayedTransition(
                categoriesContainer,
                AutoTransition().apply {
                    duration = 220
                }
            )

            categoriesContainer.addView(newPanel, insertIndex)

            expandedPanel = newPanel
            expandedItem = item
            categories.forEach { it.isExpanded = it == item }

            updateFolderStates()
        }

        fun closePanelThen(openNext: ExpandableCategoryItem? = null) {
            val panelToRemove = expandedPanel

            if (panelToRemove == null) {
                expandedPanel = null
                expandedItem = null
                categories.forEach { it.isExpanded = false }
                updateFolderStates()

                if (openNext != null) {
                    insertPanelBelowItem(openNext)
                }

                return
            }

            panelToRemove.animate()
                .alpha(0f)
                .translationY(-AppUiUtils.dp(requireContext(), 12).toFloat())
                .setDuration(160)
                .withEndAction {
                    TransitionManager.beginDelayedTransition(
                        categoriesContainer,
                        AutoTransition().apply {
                            duration = 200
                        }
                    )

                    categoriesContainer.removeView(panelToRemove)
                    expandedPanel = null
                    expandedItem = null
                    categories.forEach { it.isExpanded = false }
                    updateFolderStates()

                    if (openNext != null) {
                        categoriesContainer.post {
                            insertPanelBelowItem(openNext)
                        }
                    }
                }
                .start()
        }

        fun createFolderView(item: ExpandableCategoryItem): View {
            val view = layoutInflater.inflate(
                R.layout.item_expandable_category_folder,
                categoriesContainer,
                false
            )

            val folderCard = view.findViewById<LinearLayout>(R.id.folderCard)
            val iconPreviewGrid = view.findViewById<GridLayout>(R.id.iconPreviewGrid)
            val txtCategoryName = view.findViewById<TextView>(R.id.txtCategoryName)
            val txtCategoryCount = view.findViewById<TextView>(R.id.txtCategoryCount)

            txtCategoryName.text = item.category.name
            txtCategoryCount.text = "${item.category.apps.size} apps"

            fillPreviewIcons(iconPreviewGrid, item.category.apps, 22)

            folderViews[item] = folderCard

            folderCard.setOnClickListener {
                AnimationUtils.press(folderCard) {
                    if (item.category.name == "Todas las apps") {
                        openFragment(AllAppsFragment())
                        return@press
                    }

                    when {
                        expandedItem == null -> {
                            insertPanelBelowItem(item)
                        }

                        expandedItem == item -> {
                            closePanelThen(null)
                        }

                        else -> {
                            closePanelThen(item)
                        }
                    }
                }
            }

            return view
        }

        categories.chunked(4).forEach { rowItems ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            rowItems.forEach { item ->
                val folderView = createFolderView(item)

                folderView.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    marginEnd = AppUiUtils.dp(requireContext(), 10)
                    bottomMargin = AppUiUtils.dp(requireContext(), 12)
                }

                row.addView(folderView)
            }

            repeat(4 - rowItems.size) {
                row.addView(
                    View(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            1,
                            1f
                        )
                    }
                )
            }

            rowViews.add(row)
            categoriesContainer.addView(row)
        }

        root.addView(categoriesContainer)
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