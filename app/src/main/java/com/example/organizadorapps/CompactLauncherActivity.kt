package com.example.organizadorapps
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CompactLauncherActivity : AppCompatActivity() {

    private lateinit var floatingCard: LinearLayout
    private lateinit var categoriesContainer: LinearLayout

    private var expandedPanel: View? = null
    private var expandedItem: ExpandableCategoryItem? = null

    private val folderViews = mutableMapOf<ExpandableCategoryItem, LinearLayout>()
    private val rowViews = mutableListOf<LinearLayout>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.setDimAmount(0f)

        val allApps = AppRepository.getInstalledLaunchableApps(this)

        val recentApps = RecentAppsManager
            .getRecentPackageNames(this)
            .mapNotNull { pkg -> allApps.find { it.packageName == pkg } }
            .take(4)

        val favoritePackages = FavoritesManager.getFavoritePackages(this)

        val favoriteApps = allApps
            .filter { favoritePackages.contains(it.packageName) }
            .take(4)

        val categories = CategorySuggestionEngine
            .categorizeApps(allApps)
            .filter { it.apps.isNotEmpty() }
            .map { ExpandableCategoryItem(it, false) }
            .toMutableList()

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(dp(20), dp(28), dp(20), dp(28))
        }

        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            overScrollMode = View.OVER_SCROLL_NEVER
            isFillViewport = false
        }

        floatingCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = compactBackground()
            setPadding(dp(14), dp(16), dp(14), dp(8
            ))
        }

        floatingCard.addView(buildHeader())
        addAppSection(floatingCard, "Recientes", recentApps)
        addAppSection(floatingCard, "Favoritos rápidos", favoriteApps)
        addCategorySection(floatingCard, categories)

        scroll.addView(
            floatingCard,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            scroll,
            FrameLayout.LayoutParams(
                (resources.displayMetrics.widthPixels * 0.84f).toInt(),
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        setContentView(root)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            if (!isTouchInsideView(floatingCard, ev.rawX.toInt(), ev.rawY.toInt())) {
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

    private fun buildHeader(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, dp(8))

            addView(TextView(this@CompactLauncherActivity).apply {
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
            })

            addView(FrameLayout(this@CompactLauncherActivity).apply {
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
            })
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
            row.addView(TextView(this).apply {
                text = "Sin apps aún"
                textSize = 13f
                setTextColor(UiConstants.TEXT_SECONDARY)
                setPadding(0, dp(4), 0, dp(8))
            })
        } else {
            val useWeightedItems = apps.size >= 4

            apps.forEach { app ->
                row.addView(appIcon(app, useWeightedItems))
            }
        }

        root.addView(row)
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

            addView(ImageView(this@CompactLauncherActivity).apply {
                setImageDrawable(app.icon)
                layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))
            })

            addView(TextView(this@CompactLauncherActivity).apply {
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
            })
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
                row.addView(View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
                })
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
            iconGrid.addView(ImageView(this).apply {
                setImageDrawable(app.icon)
                scaleType = ImageView.ScaleType.FIT_CENTER
                layoutParams = ViewGroup.MarginLayoutParams(dp(18), dp(18)).apply {
                    setMargins(dp(2), dp(2), dp(2), dp(2))
                }
            })
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

        card.addView(TextView(this).apply {
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
        })

        card.addView(TextView(this).apply {
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
        })

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

            addView(LinearLayout(this@CompactLauncherActivity).apply {
                gravity = Gravity.CENTER_VERTICAL

                addView(TextView(this@CompactLauncherActivity).apply {
                    text = item.category.name
                    textSize = 15f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(UiConstants.TEXT_PRIMARY)
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                })

                addView(TextView(this@CompactLauncherActivity).apply {
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
                })
            })

            addView(RecyclerView(this@CompactLauncherActivity).apply {
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
            })
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
    private fun dp(value: Int): Int = AppUiUtils.dp(this, value)
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

