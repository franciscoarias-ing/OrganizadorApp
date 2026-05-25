package com.example.organizadorapps.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadorapps.AnimationUtils
import com.example.organizadorapps.AppAdapter
import com.example.organizadorapps.AppLauncher
import com.example.organizadorapps.AppRepository
import com.example.organizadorapps.AppUiUtils
import com.example.organizadorapps.CategorySuggestionEngine
import com.example.organizadorapps.CompactLauncherActivity
import com.example.organizadorapps.ExpandableCategoryItem
import com.example.organizadorapps.ExpandedAppsAdapter
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

        root.addView(buildHomeHeader())

        val normalContent = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
        }

        val searchContent = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }

        lateinit var searchEditText: EditText

        var isSearchMode = false
        var ignoreSearchChange = false

        lateinit var backCallback: OnBackPressedCallback

        fun exitSearchMode() {
            if (!isSearchMode) return

            ignoreSearchChange = true
            searchEditText.setText("")
            ignoreSearchChange = false

            searchContent.removeAllViews()
            searchContent.visibility = View.GONE
            normalContent.visibility = View.VISIBLE

            hideKeyboard(searchEditText)
            searchEditText.clearFocus()

            isSearchMode = false
            backCallback.isEnabled = false
        }

        fun enterSearchMode() {
            if (isSearchMode) return

            isSearchMode = true
            backCallback.isEnabled = true

            normalContent.visibility = View.GONE
            searchContent.visibility = View.VISIBLE

            searchContent.removeAllViews()
            searchContent.addView(
                AppUiUtils.sectionRow(requireContext(), "Resultados", "Volver", 0, 8) {
                    exitSearchMode()
                }
            )

            searchContent.addView(
                AppUiUtils.miniEmpty(requireContext(), "Escribe el nombre de una app.")
            )

            showKeyboard(searchEditText)
        }

        fun renderSearchResults(query: String) {
            if (ignoreSearchChange) return

            searchContent.removeAllViews()

            searchContent.addView(
                AppUiUtils.sectionRow(requireContext(), "Resultados", "Volver", 0, 8) {
                    exitSearchMode()
                }
            )

            if (query.isBlank()) {
                searchContent.addView(
                    AppUiUtils.miniEmpty(requireContext(), "Escribe el nombre de una app.")
                )
                return
            }

            val results = allApps.filter {
                it.name.contains(query, ignoreCase = true)
            }

            if (results.isEmpty()) {
                searchContent.addView(
                    AppUiUtils.miniEmpty(requireContext(), "No se encontraron apps.")
                )
                return
            }

            searchContent.addView(
                RecyclerView(requireContext()).apply {
                    layoutManager = GridLayoutManager(requireContext(), 4)

                    adapter = ExpandedAppsAdapter(results) { app: InstalledApp ->
                        RecentAppsManager.registerAppOpen(requireContext(), app)
                        AppLauncher.openApp(requireContext(), app.packageName, app.name)
                    }

                    overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                    isNestedScrollingEnabled = false

                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = AppUiUtils.dp(requireContext(), 8)
                    }
                }
            )
        }

        backCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                exitSearchMode()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            backCallback
        )

        searchEditText = AppUiUtils.searchBox(
            context = requireContext(),
            hintValue = "Buscar apps..."
        ) { query ->
            enterSearchMode()
            renderSearchResults(query)
        }

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                enterSearchMode()
            }
        }

        root.addView(searchEditText)
        root.addView(normalContent)
        root.addView(searchContent)

        addRecentSection(normalContent, recentApps)
        addFavoritesSection(normalContent, favoriteApps)
        addCategorySection(normalContent, categories)

        scroll.addView(root)
        return scroll
    }

    private fun buildHomeHeader(): LinearLayout {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                0,
                0,
                0,
                AppUiUtils.dp(requireContext(), 10)
            )

            addView(
                TextView(requireContext()).apply {
                    text = "Inicio"
                    textSize = 22f
                    setTypeface(typeface, Typeface.BOLD)
                    setTextColor(UiConstants.TEXT_PRIMARY)
                    includeFontPadding = false

                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }
            )

            addView(
                FrameLayout(requireContext()).apply {
                    background = collapseButtonBackground()
                    isClickable = true
                    isFocusable = true

                    layoutParams = LinearLayout.LayoutParams(
                        AppUiUtils.dp(requireContext(), 48),
                        AppUiUtils.dp(requireContext(), 42)
                    )

                    addView(
                        ImageView(requireContext()).apply {
                            setImageResource(R.drawable.ic_collapse_diagonal)
                            scaleType = ImageView.ScaleType.CENTER
                        },
                        FrameLayout.LayoutParams(
                            AppUiUtils.dp(requireContext(), 26),
                            AppUiUtils.dp(requireContext(), 26),
                            Gravity.CENTER
                        )
                    )

                    setOnClickListener {
                        startActivity(
                            Intent(requireContext(), CompactLauncherActivity::class.java)
                        )
                        requireActivity().finish()
                    }
                }
            )
        }
    }

    private fun collapseButtonBackground(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(UiConstants.SURFACE_ALT)
            cornerRadius = AppUiUtils.dp(requireContext(), 22).toFloat()
            setStroke(
                AppUiUtils.dp(requireContext(), 1),
                UiConstants.BORDER
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
                setPadding(0, 0, 0, AppUiUtils.dp(requireContext(), 10))
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
                setPadding(0, 0, 0, AppUiUtils.dp(requireContext(), 10))
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

    private fun addCategorySection(
        root: LinearLayout,
        categories: MutableList<ExpandableCategoryItem>
    ) {
        root.addView(
            AppUiUtils.sectionRow(requireContext(), "Categorías", "Editar", 0, 4) {
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
            val txtExpandedCategoryCount = panel.findViewById<TextView>(R.id.txtExpandedCategoryCount)
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

    private fun showKeyboard(editText: EditText) {
        editText.post {
            editText.requestFocus()
            val imm = requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
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