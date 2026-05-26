package com.example.organizadorapps

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity

class CompactLauncherActivity : AppCompatActivity() {

    private lateinit var rootContainer: FrameLayout
    private lateinit var floatingCard: LinearLayout
    private lateinit var allApps: List<InstalledApp>

    private var lastPermissionState: Boolean? = null
    private var favoritesController: FavoritesSectionController? = null
    private var categoryController: ExpandableCategoryController? = null
    private var searchController: CompactSearchController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.setDimAmount(0f)

        rootContainer = FrameLayout(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            setPadding(
                AppUiUtils.dp(
                    this@CompactLauncherActivity,
                    UiConstants.COMPACT_OUTER_HORIZONTAL_PADDING_DP
                ),
                AppUiUtils.dp(this@CompactLauncherActivity, 12),
                AppUiUtils.dp(
                    this@CompactLauncherActivity,
                    UiConstants.COMPACT_OUTER_HORIZONTAL_PADDING_DP
                ),
                AppUiUtils.dp(this@CompactLauncherActivity, 12)
            )
        }

        setContentView(rootContainer)
        renderContent()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            if (
                ::floatingCard.isInitialized &&
                !isTouchInsideView(floatingCard, ev.rawX.toInt(), ev.rawY.toInt())
            ) {
                searchController?.clearAndHideKeyboard()
                finish()
                return true
            }
        }

        return super.dispatchTouchEvent(ev)
    }

    override fun onBackPressed() {
        if (searchController != null) {
            searchController?.clearAndHideKeyboard()
            renderNormalContent()
            return
        }

        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()

        val currentPermissionState = UsageStatsHelper.hasUsageStatsPermission(this)

        if (lastPermissionState != null && lastPermissionState != currentPermissionState) {
            renderContent()
        }
    }

    private fun renderContent() {
        lastPermissionState = UsageStatsHelper.hasUsageStatsPermission(this)
        allApps = AppRepository.getInstalledLaunchableApps(this)

        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
            isVerticalScrollBarEnabled = true
            isFillViewport = false
            clipToPadding = false

            setPadding(
                0,
                AppUiUtils.dp(this@CompactLauncherActivity, 10),
                0,
                AppUiUtils.dp(this@CompactLauncherActivity, 18)
            )
        }

        floatingCard = LauncherUiFactory.compactCard(this)

        scroll.addView(
            floatingCard,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        rootContainer.removeAllViews()

        val compactWidth =
            (resources.displayMetrics.widthPixels * UiConstants.COMPACT_WIDTH_PERCENT).toInt()

        val scrollParams = FrameLayout.LayoutParams(
            compactWidth,
            FrameLayout.LayoutParams.MATCH_PARENT
        ).apply {
            gravity = Gravity.CENTER
        }

        rootContainer.addView(scroll, scrollParams)

        renderNormalContent()
    }

    private fun renderNormalContent() {
        searchController = null
        hideKeyboardFromCurrentFocus()

        floatingCard.removeAllViews()
        favoritesController = null
        categoryController = null

        floatingCard.addView(buildHeader())

        val recentApps = SmartRecentAppsManager.getRecentApps(
            context = this,
            allApps = allApps,
            limit = 4,
            daysBack = 7
        )

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

        addAppSection(
            root = floatingCard,
            title = "Recientes",
            apps = recentApps
        )

        addFavoritesSection(floatingCard)

        addCategorySection(
            root = floatingCard,
            categories = categories
        )
    }

    private fun buildHeader(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            setPadding(
                0,
                0,
                0,
                AppUiUtils.dp(this@CompactLauncherActivity, 6)
            )

            addView(
                LauncherUiFactory.compactSearchButton(
                    context = this@CompactLauncherActivity,
                    textValue = "Buscar apps"
                ) {
                    showCompactSearch()
                }
            )

            addView(
                LauncherUiFactory.iconButton(
                    context = this@CompactLauncherActivity,
                    iconRes = R.drawable.ic_expand_diagonal,
                    sizeDp = 44,
                    heightDp = 40,
                    iconSizeDp = 24
                ) {
                    startActivity(Intent(this@CompactLauncherActivity, MainActivity::class.java))
                    finish()
                }
            )
        }
    }

    private fun showCompactSearch() {
        favoritesController?.collapse()
        categoryController?.collapseCurrent()

        searchController = CompactSearchController(
            context = this,
            host = floatingCard,
            allAppsProvider = { allApps },
            onBack = {
                renderNormalContent()
            },
            onAppClick = { app ->
                RecentAppsManager.registerAppOpen(this, app)
                AppLauncher.openApp(this, app.packageName, app.name)
                finish()
            }
        ).also { controller ->
            controller.show()
        }
    }

    private fun addAppSection(
        root: LinearLayout,
        title: String,
        apps: List<InstalledApp>
    ) {
        root.addView(
            LauncherUiFactory.sectionTitle(
                context = this,
                textValue = title,
                topPaddingDp = 10,
                bottomPaddingDp = 8
            )
        )

        root.addView(
            LauncherUiFactory.compactAppRow(
                context = this,
                apps = apps,
                closeAfterLaunch = {
                    finish()
                }
            )
        )
    }

    private fun addFavoritesSection(root: LinearLayout) {
        favoritesController = FavoritesSectionController(
            context = this,
            inflater = layoutInflater,
            allAppsProvider = { allApps },
            mode = FavoritesSectionController.Mode.COMPACT,
            displayLimit = 4,
            maxFavorites = UiConstants.MAX_FAVORITES,
            closeAfterLaunch = {
                finish()
            },
            bottomMarginDp = 8
        ).also { controller ->
            controller.addTo(root)
        }
    }

    private fun addCategorySection(
        root: LinearLayout,
        categories: MutableList<ExpandableCategoryItem>
    ) {
        root.addView(
            LauncherUiFactory.sectionTitle(
                context = this,
                textValue = "Categorías",
                topPaddingDp = 10,
                bottomPaddingDp = 8
            )
        )

        categoryController = ExpandableCategoryController(
            context = this,
            layoutInflater = layoutInflater,
            categories = categories,
            columns = UiConstants.COMPACT_COLUMNS,
            expandedAppsColumns = UiConstants.COMPACT_COLUMNS,
            folderHeightDp = 96,
            folderMarginEndDp = 8,
            folderBottomMarginDp = 8,
            folderPreviewIconSizeDp = 17,
            panelPreviewIconSizeDp = 17
        ) { app ->
            RecentAppsManager.registerAppOpen(this, app)
            AppLauncher.openApp(this, app.packageName, app.name)
            finish()
        }.also { controller ->
            controller.attachTo(root)
        }
    }

    private fun isTouchInsideView(view: View, x: Int, y: Int): Boolean {
        val rect = Rect()
        view.getGlobalVisibleRect(rect)
        return rect.contains(x, y)
    }

    private fun hideKeyboardFromCurrentFocus() {
        val focusedView = currentFocus ?: return
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager

        inputMethodManager?.hideSoftInputFromWindow(focusedView.windowToken, 0)
        focusedView.clearFocus()
    }
}