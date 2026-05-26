package com.example.organizadorapps

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CompactLauncherActivity : AppCompatActivity() {

    private lateinit var floatingCard: LinearLayout
    private lateinit var categoriesContainer: LinearLayout
    private lateinit var rootContainer: FrameLayout

    private lateinit var allApps: List<InstalledApp>

    private lateinit var compactFavoritesRowHost: LinearLayout
    private lateinit var compactFavoritesEditorHost: FrameLayout
    private lateinit var compactFavoritesActionText: TextView

    private var isCompactFavoritesEditorExpanded = false
    private var compactFavoriteEditorAdapter: FavoriteEditorAdapter? = null

    private var lastPermissionState: Boolean? = null
    private var expandedPanel: View? = null
    private var expandedItem: ExpandableCategoryItem? = null

    private val folderViews = mutableMapOf<ExpandableCategoryItem, LinearLayout>()
    private val rowViews = mutableListOf<LinearLayout>()

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
            setPadding(dp(20), dp(28), dp(20), dp(28))
        }

        setContentView(rootContainer)
        renderContent()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            if (::floatingCard.isInitialized &&
                !isTouchInsideView(floatingCard, ev.rawX.toInt(), ev.rawY.toInt())
            ) {
                finish()
                return true
            }
        }

        return super.dispatchTouchEvent(ev)
    }

    private fun isTouchInsideView(view: View, x: Int, y: Int): Boolean {
        val rect = Rect()
        view.getGlobalVisibleRect(rect)
        return rect.contains(x, y)
    }

    private fun renderContent() {
        lastPermissionState = UsageStatsHelper.hasUsageStatsPermission(this)

        expandedPanel = null
        expandedItem = null
        folderViews.clear()
        rowViews.clear()

        isCompactFavoritesEditorExpanded = false
        compactFavoriteEditorAdapter = null

        allApps = AppRepository.getInstalledLaunchableApps(this)

        val recentApps = SmartRecentAppsManager.getRecentApps(
            context = this,
            allApps = allApps,
            limit = 4,
            daysBack = 7
        )

        val categories = CategorySuggestionEngine
            .categorizeApps(allApps)
            .filter { it.apps.isNotEmpty() }
            .map { ExpandableCategoryItem(it, false) }
            .toMutableList()

        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            overScrollMode = View.OVER_SCROLL_NEVER
            isFillViewport = false
        }

        floatingCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = compactBackground()
            setPadding(dp(14), dp(16), dp(14), dp(8))
        }

        floatingCard.addView(buildHeader())
        addAppSection(floatingCard, "Recientes", recentApps)
        addFavoritesSection(floatingCard)
        addCategorySection(floatingCard, categories)

        scroll.addView(
            floatingCard,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        rootContainer.removeAllViews()

        rootContainer.addView(
            scroll,
            FrameLayout.LayoutParams(
                (resources.displayMetrics.widthPixels * 0.84f).toInt(),
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
        )
    }

    private fun buildHeader(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, dp(8))

            addView(
                TextView(this@CompactLauncherActivity).apply {
                    text = "Buscar apps"
                    textSize = 13f
                    gravity = Gravity.CENTER_VERTICAL
                    setTextColor(UiConstants.TEXT_SECONDARY)
                    background = searchBackground()
                    isClickable = true
                    isFocusable = true

                    setPadding(
                        dp(14),
                        0,
                        dp(14),
                        0
                    )

                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        dp(42),
                        1f
                    ).apply {
                        marginEnd = dp(10)
                    }

                    setOnClickListener {
                        startActivity(Intent(this@CompactLauncherActivity, MainActivity::class.java))
                        finish()
                    }
                }
            )

            addView(
                FrameLayout(this@CompactLauncherActivity).apply {
                    background = expandButtonBackground()
                    isClickable = true
                    isFocusable = true

                    layoutParams = LinearLayout.LayoutParams(dp(48), dp(42))

                    addView(
                        ExpandDiagonalIconView(this@CompactLauncherActivity),
                        FrameLayout.LayoutParams(
                            dp(26),
                            dp(26),
                            Gravity.CENTER
                        )
                    )

                    setOnClickListener {
                        startActivity(Intent(this@CompactLauncherActivity, MainActivity::class.java))
                        finish()
                    }
                }
            )
        }
    }

    private fun addAppSection(
        root: LinearLayout,
        title: String,
        apps: List<InstalledApp>
    ) {
        root.addView(sectionTitle(title))

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }

        if (apps.isEmpty()) {
            row.addView(
                TextView(this).apply {
                    text = "Sin apps aún"
                    textSize = 13f
                    setTextColor(UiConstants.TEXT_SECONDARY)
                    setPadding(0, dp(4), 0, dp(8))
                }
            )
        } else {
            val useWeightedItems = apps.size >= 4

            apps.forEach { app ->
                row.addView(appIcon(app, useWeightedItems))
            }
        }

        root.addView(row)
    }

    private fun addFavoritesSection(root: LinearLayout) {
        root.addView(buildCompactFavoritesHeader())

        compactFavoritesRowHost = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        root.addView(compactFavoritesRowHost)

        compactFavoritesEditorHost = FrameLayout(this).apply {
            visibility = View.GONE
            alpha = 0f

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0
            ).apply {
                bottomMargin = dp(10)
            }
        }

        root.addView(compactFavoritesEditorHost)

        refreshCompactFavoritesRow()
    }

    private fun buildCompactFavoritesHeader(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(12), 0, dp(10))

            addView(
                TextView(this@CompactLauncherActivity).apply {
                    text = "Favoritos rápidos"
                    textSize = 18f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(UiConstants.TEXT_PRIMARY)
                    includeFontPadding = false

                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }
            )

            compactFavoritesActionText = TextView(this@CompactLauncherActivity).apply {
                text = "Editar"
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(UiConstants.ACCENT)
                includeFontPadding = false
                gravity = Gravity.CENTER
                setPadding(dp(12), dp(6), 0, dp(6))

                setOnClickListener {
                    toggleCompactFavoritesEditor()
                }
            }

            addView(compactFavoritesActionText)
        }
    }

    private fun refreshCompactFavoritesRow() {
        compactFavoritesRowHost.removeAllViews()

        val favoritePackages = FavoritesManager.getFavoritePackages(this)

        val favoriteApps = allApps
            .filter { it.packageName in favoritePackages }
            .take(4)

        if (favoriteApps.isEmpty()) {
            compactFavoritesRowHost.addView(
                TextView(this).apply {
                    text = "Agrega tus apps favoritas desde Editar."
                    textSize = 13f
                    setTextColor(UiConstants.TEXT_SECONDARY)
                    setPadding(0, dp(4), 0, dp(8))
                }
            )
            return
        }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }

        val useWeightedItems = favoriteApps.size >= 4

        favoriteApps.forEach { app ->
            row.addView(appIcon(app, useWeightedItems))
        }

        compactFavoritesRowHost.addView(row)
    }

    private fun toggleCompactFavoritesEditor() {
        if (isCompactFavoritesEditorExpanded) {
            collapseCompactFavoritesEditor()
        } else {
            expandCompactFavoritesEditor()
        }
    }

    private fun expandCompactFavoritesEditor() {
        if (isCompactFavoritesEditorExpanded) return

        ensureCompactFavoritesEditorBuilt()

        isCompactFavoritesEditorExpanded = true
        compactFavoritesActionText.text = "Cerrar"

        compactFavoritesEditorHost.visibility = View.VISIBLE
        compactFavoritesEditorHost.alpha = 0f
        compactFavoritesEditorHost.translationY = -dp(8).toFloat()

        val parentWidth = (compactFavoritesEditorHost.parent as? View)?.width
            ?: resources.displayMetrics.widthPixels

        compactFavoritesEditorHost.measure(
            View.MeasureSpec.makeMeasureSpec(parentWidth, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        val targetHeight = compactFavoritesEditorHost.measuredHeight.coerceAtLeast(dp(360))

        compactFavoritesEditorHost.layoutParams =
            compactFavoritesEditorHost.layoutParams.apply {
                height = 0
            }

        ValueAnimator.ofInt(0, targetHeight).apply {
            duration = 240L
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener { animator ->
                val value = animator.animatedValue as Int

                compactFavoritesEditorHost.layoutParams =
                    compactFavoritesEditorHost.layoutParams.apply {
                        height = value
                    }

                compactFavoritesEditorHost.alpha = animator.animatedFraction
                compactFavoritesEditorHost.translationY =
                    -dp(8).toFloat() * (1f - animator.animatedFraction)
            }

            start()
        }
    }

    private fun collapseCompactFavoritesEditor() {
        if (!isCompactFavoritesEditorExpanded) return

        val searchInput = compactFavoritesEditorHost.findViewById<EditText>(R.id.editSearchFavorites)
        searchInput?.clearFocus()

        if (searchInput != null) {
            hideKeyboard(searchInput)
        }

        isCompactFavoritesEditorExpanded = false
        compactFavoritesActionText.text = "Editar"

        val startHeight = compactFavoritesEditorHost.height

        ValueAnimator.ofInt(startHeight, 0).apply {
            duration = 220L
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener { animator ->
                val value = animator.animatedValue as Int

                compactFavoritesEditorHost.layoutParams =
                    compactFavoritesEditorHost.layoutParams.apply {
                        height = value
                    }

                compactFavoritesEditorHost.alpha = 1f - animator.animatedFraction
                compactFavoritesEditorHost.translationY =
                    -dp(8).toFloat() * animator.animatedFraction
            }

            addListener(
                object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        compactFavoritesEditorHost.visibility = View.GONE
                        compactFavoritesEditorHost.alpha = 0f
                        compactFavoritesEditorHost.translationY = 0f

                        compactFavoritesEditorHost.layoutParams =
                            compactFavoritesEditorHost.layoutParams.apply {
                                height = 0
                            }
                    }
                }
            )

            start()
        }
    }

    private fun ensureCompactFavoritesEditorBuilt() {
        if (compactFavoritesEditorHost.childCount > 0) return

        val editorView = layoutInflater.inflate(
            R.layout.layout_inline_favorites_editor,
            compactFavoritesEditorHost,
            false
        )

        val btnCollapse = editorView.findViewById<FrameLayout>(R.id.btnCollapseFavoritesEditor)
        val searchInput = editorView.findViewById<EditText>(R.id.editSearchFavorites)
        val recyclerApps = editorView.findViewById<RecyclerView>(R.id.recyclerFavoriteEditorApps)

        val adapter = FavoriteEditorAdapter(
            allApps = allApps.sortedBy { it.name.lowercase() },
            onFavoriteChanged = {
                refreshCompactFavoritesRow()
                compactFavoriteEditorAdapter?.refreshFavoritesState()
            }
        )

        compactFavoriteEditorAdapter = adapter

        recyclerApps.apply {
            layoutManager = LinearLayoutManager(this@CompactLauncherActivity)
            this.adapter = adapter
            overScrollMode = RecyclerView.OVER_SCROLL_IF_CONTENT_SCROLLS
            isNestedScrollingEnabled = true
        }

        recyclerApps.setOnTouchListener { view, event ->
            view.parent.requestDisallowInterceptTouchEvent(true)

            if (
                event.action == MotionEvent.ACTION_UP ||
                event.action == MotionEvent.ACTION_CANCEL
            ) {
                view.parent.requestDisallowInterceptTouchEvent(false)
            }

            false
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                adapter.filter(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        btnCollapse.setOnClickListener {
            collapseCompactFavoritesEditor()
        }

        compactFavoritesEditorHost.addView(
            editorView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun appIcon(
        app: InstalledApp,
        useWeight: Boolean = true
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true

            layoutParams = if (useWeight) {
                LinearLayout.LayoutParams(
                    0,
                    dp(82),
                    1f
                )
            } else {
                LinearLayout.LayoutParams(
                    dp(86),
                    dp(82)
                ).apply {
                    marginEnd = dp(8)
                }
            }

            setOnClickListener {
                RecentAppsManager.registerAppOpen(this@CompactLauncherActivity, app)
                AppLauncher.openApp(this@CompactLauncherActivity, app.packageName, app.name)
                finish()
            }

            addView(
                ImageView(this@CompactLauncherActivity).apply {
                    setImageDrawable(app.icon)
                    layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))
                }
            )

            addView(
                TextView(this@CompactLauncherActivity).apply {
                    text = app.name
                    textSize = 11f
                    maxLines = 1
                    gravity = Gravity.CENTER
                    setTextColor(UiConstants.TEXT_PRIMARY)
                    includeFontPadding = false

                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = dp(6)
                    }
                }
            )
        }
    }

    private fun addCategorySection(
        root: LinearLayout,
        categories: MutableList<ExpandableCategoryItem>
    ) {
        root.addView(sectionTitle("Categorías"))

        categoriesContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        categories.chunked(3).forEach { rowItems ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            rowItems.forEachIndexed { index, item ->
                val folder = createFolderView(item, categories)

                folder.layoutParams = LinearLayout.LayoutParams(
                    0,
                    dp(104),
                    1f
                ).apply {
                    if (index < rowItems.lastIndex) marginEnd = dp(8)
                    bottomMargin = dp(8)
                }

                row.addView(folder)
            }

            repeat(3 - rowItems.size) {
                row.addView(
                    View(this).apply {
                        layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
                    }
                )
            }

            rowViews.add(row)
            categoriesContainer.addView(row)
        }

        root.addView(categoriesContainer)
    }

    private fun createFolderView(
        item: ExpandableCategoryItem,
        categories: MutableList<ExpandableCategoryItem>
    ): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            background = folderBackground()
            isClickable = true
            isFocusable = true
            setPadding(dp(10), dp(10), dp(10), dp(10))
        }

        val previewHolder = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(42)
            )
        }

        val iconGrid = GridLayout(this).apply {
            columnCount = 2
            rowCount = 2
            useDefaultMargins = false
        }

        item.category.apps.take(4).forEach { app ->
            iconGrid.addView(
                ImageView(this).apply {
                    setImageDrawable(app.icon)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    layoutParams = ViewGroup.MarginLayoutParams(dp(18), dp(18)).apply {
                        setMargins(dp(2), dp(2), dp(2), dp(2))
                    }
                }
            )
        }

        previewHolder.addView(
            iconGrid,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        )

        card.addView(previewHolder)

        card.addView(
            TextView(this).apply {
                text = item.category.name
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                maxLines = 1
                setTextColor(UiConstants.TEXT_PRIMARY)
                includeFontPadding = false

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(6)
                }
            }
        )

        card.addView(
            TextView(this).apply {
                text = "${item.category.apps.size} apps"
                textSize = 11f
                gravity = Gravity.CENTER
                setTextColor(UiConstants.TEXT_SECONDARY)
                includeFontPadding = false

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(4)
                }
            }
        )

        folderViews[item] = card

        card.setOnClickListener {
            when {
                expandedItem == null -> insertPanelBelowItem(item, categories)
                expandedItem == item -> closePanel(categories, null)
                else -> closePanel(categories, item)
            }
        }

        return card
    }

    private fun insertPanelBelowItem(
        item: ExpandableCategoryItem,
        categories: MutableList<ExpandableCategoryItem>
    ) {
        val rowIndex = categories.indexOf(item) / 3
        val rowView = rowViews.getOrNull(rowIndex) ?: return
        val insertIndex = categoriesContainer.indexOfChild(rowView) + 1

        val panel = createExpandedPanel(item, categories)

        TransitionManager.beginDelayedTransition(
            categoriesContainer,
            AutoTransition().apply { duration = 180 }
        )

        categoriesContainer.addView(panel, insertIndex)

        expandedPanel = panel
        expandedItem = item

        categories.forEach { it.isExpanded = it == item }
        updateFolderStates()
    }

    private fun createExpandedPanel(
        item: ExpandableCategoryItem,
        categories: MutableList<ExpandableCategoryItem>
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = expandedBackground()
            setPadding(dp(14), dp(12), dp(14), dp(14))

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }

            addView(
                LinearLayout(this@CompactLauncherActivity).apply {
                    gravity = Gravity.CENTER_VERTICAL

                    addView(
                        TextView(this@CompactLauncherActivity).apply {
                            text = item.category.name
                            textSize = 15f
                            typeface = Typeface.DEFAULT_BOLD
                            setTextColor(UiConstants.TEXT_PRIMARY)

                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                        }
                    )

                    addView(
                        TextView(this@CompactLauncherActivity).apply {
                            text = "⌃"
                            textSize = 20f
                            gravity = Gravity.CENTER
                            setTextColor(UiConstants.TEXT_PRIMARY)
                            background = expandButtonBackground()
                            isClickable = true
                            isFocusable = true

                            layoutParams = LinearLayout.LayoutParams(dp(42), dp(36))

                            setOnClickListener {
                                closePanel(categories, null)
                            }
                        }
                    )
                }
            )

            addView(
                RecyclerView(this@CompactLauncherActivity).apply {
                    layoutManager = GridLayoutManager(this@CompactLauncherActivity, 4)

                    adapter = ExpandedAppsAdapter(item.category.apps) { app ->
                        RecentAppsManager.registerAppOpen(this@CompactLauncherActivity, app)
                        AppLauncher.openApp(this@CompactLauncherActivity, app.packageName, app.name)
                        finish()
                    }

                    overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                    isNestedScrollingEnabled = false

                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = dp(10)
                    }
                }
            )
        }
    }

    private fun closePanel(
        categories: MutableList<ExpandableCategoryItem>,
        openNext: ExpandableCategoryItem?
    ) {
        val panel = expandedPanel ?: return

        TransitionManager.beginDelayedTransition(
            categoriesContainer,
            AutoTransition().apply { duration = 160 }
        )

        categoriesContainer.removeView(panel)
        expandedPanel = null
        expandedItem = null

        categories.forEach { it.isExpanded = false }
        updateFolderStates()

        if (openNext != null) {
            categoriesContainer.post {
                insertPanelBelowItem(openNext, categories)
            }
        }
    }

    private fun updateFolderStates() {
        folderViews.forEach { (item, card) ->
            card.alpha = if (item == expandedItem) 0.88f else 1f
        }
    }

    private fun sectionTitle(textValue: String): TextView {
        return TextView(this).apply {
            text = textValue
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(UiConstants.TEXT_PRIMARY)
            includeFontPadding = false
            setPadding(0, dp(12), 0, dp(10))
        }
    }

    private fun compactBackground(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(UiConstants.BACKGROUND)
            cornerRadius = dp(26).toFloat()
            setStroke(dp(1), UiConstants.BORDER)
        }
    }

    private fun folderBackground(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(UiConstants.SURFACE)
            cornerRadius = dp(20).toFloat()
            setStroke(dp(1), UiConstants.BORDER)
        }
    }

    private fun expandedBackground(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(UiConstants.SURFACE_ALT)
            cornerRadius = dp(22).toFloat()
            setStroke(dp(1), UiConstants.BORDER)
        }
    }

    private fun expandButtonBackground(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(UiConstants.SURFACE_ALT)
            cornerRadius = dp(22).toFloat()
            setStroke(dp(1), UiConstants.BORDER)
        }
    }

    private fun searchBackground(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(UiConstants.SURFACE_ALT)
            cornerRadius = dp(22).toFloat()
            setStroke(dp(1), UiConstants.BORDER)
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun dp(value: Int): Int = AppUiUtils.dp(this, value)

    override fun onResume() {
        super.onResume()

        val currentPermissionState = UsageStatsHelper.hasUsageStatsPermission(this)

        if (lastPermissionState != null && lastPermissionState != currentPermissionState) {
            renderContent()
        }
    }

    private class ExpandDiagonalIconView(context: Context) : View(context) {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = UiConstants.TEXT_PRIMARY
            strokeWidth = 4.2f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val w = width.toFloat()
            val h = height.toFloat()

            val startX = w * 0.22f
            val startY = h * 0.78f
            val endX = w * 0.78f
            val endY = h * 0.22f

            canvas.drawLine(startX, startY, endX, endY, paint)

            canvas.drawLine(startX, startY, startX, h * 0.55f, paint)
            canvas.drawLine(startX, startY, w * 0.45f, startY, paint)

            canvas.drawLine(endX, endY, endX, h * 0.45f, paint)
            canvas.drawLine(endX, endY, w * 0.55f, endY, paint)
        }
    }
}