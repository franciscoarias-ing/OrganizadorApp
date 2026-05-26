package com.example.organizadorapps.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadorapps.AppActionsBottomSheet
import com.example.organizadorapps.AppAdapter
import com.example.organizadorapps.AppLauncher
import com.example.organizadorapps.AppRepository
import com.example.organizadorapps.AppUiUtils
import com.example.organizadorapps.CategorySuggestionEngine
import com.example.organizadorapps.ExpandableCategoryController
import com.example.organizadorapps.ExpandableCategoryItem
import com.example.organizadorapps.FavoritesSectionController
import com.example.organizadorapps.HiddenAppsBottomSheet
import com.example.organizadorapps.InstalledApp
import com.example.organizadorapps.LauncherUiFactory
import com.example.organizadorapps.R
import com.example.organizadorapps.RecentAppsManager
import com.example.organizadorapps.SmartRecentAppsManager
import com.example.organizadorapps.UiConstants
import com.example.organizadorapps.UsageStatsHelper

class HomeFragment : Fragment() {

    private lateinit var allApps: List<InstalledApp>
    private var lastPermissionState: Boolean? = null
    private var favoritesController: FavoritesSectionController? = null
    private var categoryController: ExpandableCategoryController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        allApps = AppRepository.getInstalledLaunchableApps(requireContext())
        lastPermissionState = UsageStatsHelper.hasUsageStatsPermission(requireContext())

        val recentApps = SmartRecentAppsManager.getRecentApps(
            context = requireContext(),
            allApps = allApps,
            limit = 4,
            daysBack = 7
        )

        val categories = CategorySuggestionEngine
            .categorizeApps(allApps)
            .filter { it.apps.isNotEmpty() }
            .map { category -> ExpandableCategoryItem(category = category, isExpanded = false) }
            .toMutableList()

        val scroll = ScrollView(requireContext()).apply {
            setBackgroundColor(Color.TRANSPARENT)
            overScrollMode = View.OVER_SCROLL_NEVER
            isFillViewport = true
        }

        val root = LauncherUiFactory.screenRoot(requireContext())

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

        fun renderSearchResults(query: String) {
            if (ignoreSearchChange) return

            searchContent.removeAllViews()

            val cleanQuery = query.trim()
            searchContent.addView(
                AppUiUtils.sectionRow(
                    requireContext(),
                    if (cleanQuery.isBlank()) "Todas las apps" else "Resultados",
                    "Volver",
                    0,
                    8
                ) {
                    if (isSearchMode) {
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
                }
            )

            val results = allApps
                .filter { app ->
                    cleanQuery.isBlank() || app.name.contains(cleanQuery, ignoreCase = true)
                }
                .sortedBy { it.name.lowercase() }

            searchContent.addView(
                LauncherUiFactory.emptyText(
                    context = requireContext(),
                    textValue = if (cleanQuery.isBlank()) {
                        "Mostrando todas las apps (${results.size})"
                    } else {
                        "Coincidencias encontradas (${results.size})"
                    },
                    bottomPaddingDp = 8
                )
            )

            if (results.isEmpty()) {
                searchContent.addView(
                    AppUiUtils.miniEmpty(requireContext(), "No se encontraron apps.")
                )
                return
            }

            searchContent.addView(
                LauncherUiFactory.expandedAppsGrid(
                    context = requireContext(),
                    apps = results,
                    columns = 4,
                    iconSizeDp = 36,
                    tileHeightDp = 78,
                    horizontalGapDp = 8,
                    bottomGapDp = 10,
                    closeAfterLaunch = null,
                    onAppLongPress = { app -> showAppActions(app) }
                ) { app ->
                    RecentAppsManager.registerAppOpen(requireContext(), app)
                    AppLauncher.openApp(requireContext(), app.packageName, app.name)
                }.apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = AppUiUtils.dp(requireContext(), 8)
                        bottomMargin = AppUiUtils.dp(requireContext(), 24)
                    }
                }
            )
        }

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

        fun enterSearchMode(shouldShowKeyboard: Boolean = true) {
            favoritesController?.collapse()
            categoryController?.collapseCurrent()

            isSearchMode = true
            backCallback.isEnabled = true

            normalContent.visibility = View.GONE
            searchContent.visibility = View.VISIBLE

            renderSearchResults(searchEditText.text?.toString().orEmpty())

            if (shouldShowKeyboard) {
                showKeyboard(searchEditText)
            }
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

        root.addView(
            buildHomeHeader(
                onShowAllApps = {
                    enterSearchMode(shouldShowKeyboard = true)
                },
                onShowHiddenApps = {
                    HiddenAppsBottomSheet.show(requireContext()) {
                        reloadHome()
                    }
                }
            )
        )

        searchEditText = AppUiUtils.searchBox(
            context = requireContext(),
            hintValue = "Buscar apps..."
        ) { query ->
            if (!isSearchMode) {
                enterSearchMode(shouldShowKeyboard = false)
            } else {
                renderSearchResults(query)
            }
        }

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !isSearchMode) {
                enterSearchMode(shouldShowKeyboard = false)
            }
        }

        root.addView(searchEditText)
        root.addView(normalContent)
        root.addView(searchContent)

        addRecentSection(normalContent, recentApps)
        addFavoritesSection(normalContent)
        addCategorySection(normalContent, categories)

        scroll.addView(root)
        return scroll
    }

    override fun onResume() {
        super.onResume()

        val currentPermissionState = UsageStatsHelper.hasUsageStatsPermission(requireContext())
        if (lastPermissionState != null && lastPermissionState != currentPermissionState) {
            view?.post {
                parentFragmentManager.beginTransaction()
                    .replace(id, HomeFragment())
                    .commitAllowingStateLoss()
            }
        }
    }

    private fun buildHomeHeader(
        onShowAllApps: () -> Unit,
        onShowHiddenApps: () -> Unit
    ): LinearLayout {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, AppUiUtils.dp(requireContext(), 10))

            addView(
                LauncherUiFactory.headerTitle(
                    context = requireContext(),
                    title = "Inicio"
                )
            )

            addView(
                TextView(requireContext()).apply {
                    text = "Ver todas las apps"
                    textSize = 13f
                    setTextColor(UiConstants.ACCENT)
                    gravity = Gravity.CENTER
                    includeFontPadding = false
                    isClickable = true
                    isFocusable = true
                    background = LauncherUiFactory.pillBackground(requireContext())
                    setPadding(
                        AppUiUtils.dp(requireContext(), 12),
                        0,
                        AppUiUtils.dp(requireContext(), 12),
                        0
                    )
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        AppUiUtils.dp(requireContext(), UiConstants.HEADER_BUTTON_HEIGHT_DP)
                    )
                    setOnClickListener {
                        com.example.organizadorapps.AnimationUtils.press(this) {
                            onShowAllApps()
                        }
                    }
                }
            )

            addView(
                TextView(requireContext()).apply {
                    text = "Ocultas"
                    textSize = 13f
                    setTextColor(UiConstants.ACCENT)
                    gravity = Gravity.CENTER
                    includeFontPadding = false
                    isClickable = true
                    isFocusable = true
                    background = LauncherUiFactory.pillBackground(requireContext())
                    setPadding(
                        AppUiUtils.dp(requireContext(), 12),
                        0,
                        AppUiUtils.dp(requireContext(), 12),
                        0
                    )
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        AppUiUtils.dp(requireContext(), UiConstants.HEADER_BUTTON_HEIGHT_DP)
                    ).apply {
                        marginStart = AppUiUtils.dp(requireContext(), 8)
                    }
                    setOnClickListener {
                        com.example.organizadorapps.AnimationUtils.press(this) {
                            onShowHiddenApps()
                        }
                    }
                }
            )
        }
    }

    private fun addRecentSection(root: LinearLayout, apps: List<InstalledApp>) {
        root.addView(
            LauncherUiFactory.sectionTitle(
                context = requireContext(),
                textValue = "Recientes",
                topPaddingDp = 0,
                bottomPaddingDp = 10
            )
        )

        if (apps.isEmpty()) {
            root.addView(
                AppUiUtils.miniEmpty(requireContext(), "Sin apps recientes por ahora.")
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
                    apps = apps,
                    mode = AppAdapter.Mode.RECENT,
                    showAddFavorite = false,
                    onAddFavoriteClick = null,
                    onLongPress = { app -> showAppActions(app) }
                )
                overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                isNestedScrollingEnabled = false
                setPadding(0, 0, AppUiUtils.dp(requireContext(), 6), 0)
                clipToPadding = false
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    AppUiUtils.dp(requireContext(), 72)
                ).apply {
                    bottomMargin = AppUiUtils.dp(requireContext(), 12)
                }
            }
        )
    }

    private fun addFavoritesSection(root: LinearLayout) {
        favoritesController = FavoritesSectionController(
            context = requireContext(),
            inflater = layoutInflater,
            allAppsProvider = { allApps },
            mode = FavoritesSectionController.Mode.HOME,
            displayLimit = UiConstants.MAX_FAVORITES,
            maxFavorites = UiConstants.MAX_FAVORITES,
            closeAfterLaunch = null,
            bottomMarginDp = 12,
            onAppLongPress = { app -> showAppActions(app) }
        ).also { controller ->
            controller.addTo(root)
        }
    }

    private fun addCategorySection(
        root: LinearLayout,
        categories: MutableList<ExpandableCategoryItem>
    ) {
        root.addView(
            LauncherUiFactory.sectionHeader(
                context = requireContext(),
                title = "Categorías",
                actionText = "Editar",
                topPaddingDp = 0,
                bottomPaddingDp = 4
            ) {
                Toast.makeText(
                    requireContext(),
                    "Edición de categorías próximamente",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        categoryController = ExpandableCategoryController(
            context = requireContext(),
            layoutInflater = layoutInflater,
            categories = categories,
            columns = UiConstants.HOME_CATEGORY_COLUMNS,
            expandedAppsColumns = UiConstants.HOME_CATEGORY_COLUMNS,
            folderHeightDp = null,
            folderMarginEndDp = 10,
            folderBottomMarginDp = 12,
            folderPreviewIconSizeDp = 22,
            panelPreviewIconSizeDp = 20,
            onAppLongPress = { app -> showAppActions(app) }
        ) { app ->
            RecentAppsManager.registerAppOpen(requireContext(), app)
            AppLauncher.openApp(requireContext(), app.packageName, app.name)
        }.also { controller ->
            controller.attachTo(root)
        }
    }

    private fun showAppActions(app: InstalledApp) {
        AppActionsBottomSheet.show(
            context = requireContext(),
            app = app,
            onFavoritesChanged = {
                favoritesController?.refresh()
            },
            onHiddenChanged = {
                reloadHome()
            }
        )
    }

    private fun reloadHome() {
        if (!isAdded) return
        parentFragmentManager.beginTransaction()
            .replace(id, HomeFragment())
            .commitAllowingStateLoss()
    }

    private fun showKeyboard(view: View) {
        view.requestFocus()
        view.post {
            val inputMethodManager =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
