package com.example.organizadorapps

import android.animation.ValueAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Controlador único para las categorías expandibles del Home y del Compact Mode.
 *
 * Mantiene el estilo visual existente, pero evita tener dos motores distintos
 * para abrir/cerrar folders, insertar paneles y actualizar estados visuales.
 */
class ExpandableCategoryController(
    private val context: Context,
    private val layoutInflater: LayoutInflater,
    private val categories: MutableList<ExpandableCategoryItem>,
    private val columns: Int,
    private val expandedAppsColumns: Int = 4,
    private val folderHeightDp: Int? = null,
    private val folderMarginEndDp: Int = 10,
    private val folderBottomMarginDp: Int = 12,
    private val folderPreviewIconSizeDp: Int = 22,
    private val panelPreviewIconSizeDp: Int = 20,
    private val onAppLongPress: ((InstalledApp) -> Unit)? = null,
    private val onAppClick: (InstalledApp) -> Unit
) {

    private lateinit var categoriesContainer: LinearLayout

    private var expandedPanel: View? = null
    private var expandedItem: ExpandableCategoryItem? = null
    private var isAnimating = false

    private val rowViews = mutableListOf<LinearLayout>()
    private val folderViews = mutableMapOf<ExpandableCategoryItem, LinearLayout>()

    fun attachTo(root: LinearLayout) {
        categoriesContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        rowViews.clear()
        folderViews.clear()
        expandedPanel = null
        expandedItem = null
        isAnimating = false
        categories.forEach { it.isExpanded = false }

        buildFolderRows()
        root.addView(categoriesContainer)
    }

    fun collapseCurrent() {
        closePanelThen(null)
    }

    private fun buildFolderRows() {
        categories.chunked(columns).forEach { rowItems ->
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            rowItems.forEachIndexed { index, item ->
                val folderView = createFolderView(item)

                folderView.layoutParams = LinearLayout.LayoutParams(
                    0,
                    folderHeightDp?.let { AppUiUtils.dp(context, it) }
                        ?: LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    if (index < rowItems.lastIndex) {
                        marginEnd = AppUiUtils.dp(context, folderMarginEndDp)
                    }
                    bottomMargin = AppUiUtils.dp(context, folderBottomMarginDp)
                }

                row.addView(folderView)
            }

            repeat(columns - rowItems.size) {
                row.addView(
                    View(context).apply {
                        layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
                    }
                )
            }

            rowViews.add(row)
            categoriesContainer.addView(row)
        }
    }

    private fun createFolderView(item: ExpandableCategoryItem): View {
        val view = layoutInflater.inflate(
            R.layout.item_expandable_category_folder,
            categoriesContainer,
            false
        )

        val folderCard = view.findViewById<LinearLayout>(R.id.folderCard)
        val iconPreviewGrid = view.findViewById<GridLayout>(R.id.iconPreviewGrid)
        val txtCategoryName = view.findViewById<TextView>(R.id.txtCategoryName)
        val txtCategoryCount = view.findViewById<TextView>(R.id.txtCategoryCount)

        folderCard.background = LauncherUiFactory.categoryFolderBackground(context)

        // En Compact Mode el alto real lo controla el root del folder.
        // El XML tenía folderCard=112dp; si el root era menor, el contenido se cortaba.
        folderCard.layoutParams = folderCard.layoutParams.apply {
            height = if (folderHeightDp != null) {
                ViewGroup.LayoutParams.MATCH_PARENT
            } else {
                AppUiUtils.dp(context, 112)
            }
        }
        if (folderHeightDp != null) {
            folderCard.setPadding(
                AppUiUtils.dp(context, 6),
                AppUiUtils.dp(context, 6),
                AppUiUtils.dp(context, 6),
                AppUiUtils.dp(context, 6)
            )
        }

        txtCategoryName.text = item.category.name
        txtCategoryCount.text = "${item.category.apps.size} apps"

        LauncherUiFactory.fillPreviewIcons(
            context = context,
            grid = iconPreviewGrid,
            apps = item.category.apps,
            iconSizeDp = folderPreviewIconSizeDp
        )

        folderViews[item] = folderCard

        folderCard.setOnClickListener {
            if (isAnimating) return@setOnClickListener

            AnimationUtils.press(folderCard) {
                when {
                    expandedItem == null -> insertPanelBelowItem(item)
                    expandedItem == item -> closePanelThen(null)
                    else -> closePanelThen(item)
                }
            }
        }

        return view
    }

    private fun createExpandedPanel(item: ExpandableCategoryItem): View {
        val panel = layoutInflater.inflate(
            R.layout.item_category_expanded_panel,
            categoriesContainer,
            false
        ).apply {
            tag = "expanded_panel"
            alpha = 0f
            translationY = -AppUiUtils.dp(context, 12).toFloat()
            background = LauncherUiFactory.categoryExpandedBackground(context)
        }

        val expandedPreviewGrid = panel.findViewById<GridLayout>(R.id.expandedPreviewGrid)
        val txtExpandedCategoryName = panel.findViewById<TextView>(R.id.txtExpandedCategoryName)
        val txtExpandedCategoryCount = panel.findViewById<TextView>(R.id.txtExpandedCategoryCount)
        val btnCollapseCategory = panel.findViewById<ImageView>(R.id.btnCollapseCategory)
        val recyclerPanelApps = panel.findViewById<RecyclerView>(R.id.recyclerPanelApps)

        txtExpandedCategoryName.text = item.category.name
        txtExpandedCategoryCount.text = "${item.category.apps.size} apps"
        btnCollapseCategory.setImageResource(R.drawable.ic_chevron_up)
        btnCollapseCategory.setColorFilter(UiConstants.ACCENT)

        LauncherUiFactory.fillPreviewIcons(
            context = context,
            grid = expandedPreviewGrid,
            apps = item.category.apps,
            iconSizeDp = panelPreviewIconSizeDp
        )

        // Usamos una grilla expandida real en vez de un RecyclerView wrap_content dentro de ScrollView.
        // Así se muestran todas las apps de la categoría y el scroll externo puede navegar todo el panel.
        recyclerPanelApps.visibility = View.GONE
        (panel as? LinearLayout)?.addView(
            LauncherUiFactory.expandedAppsGrid(
                context = context,
                apps = item.category.apps,
                columns = expandedAppsColumns,
                iconSizeDp = if (expandedAppsColumns <= 3) UiConstants.COMPACT_SEARCH_GRID_ICON_SIZE_DP else 34,
                tileHeightDp = if (expandedAppsColumns <= 3) UiConstants.COMPACT_SEARCH_GRID_TILE_HEIGHT_DP else 78,
                horizontalGapDp = if (expandedAppsColumns <= 3) 4 else 8,
                bottomGapDp = if (expandedAppsColumns <= 3) 4 else 8,
                closeAfterLaunch = null,
                onAppLongPress = onAppLongPress
            ) { app ->
                onAppClick(app)
            },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = AppUiUtils.dp(context, 12)
            }
        )

        btnCollapseCategory.setOnClickListener {
            if (isAnimating) return@setOnClickListener
            AnimationUtils.press(btnCollapseCategory) {
                closePanelThen(null)
            }
        }

        return panel
    }

    private fun insertPanelBelowItem(item: ExpandableCategoryItem) {
        if (isAnimating) return

        val rowIndex = categories.indexOf(item) / columns
        val rowView = rowViews.getOrNull(rowIndex) ?: return
        val insertIndex = categoriesContainer.indexOfChild(rowView) + 1
        val panel = createExpandedPanel(item)

        categoriesContainer.addView(panel, insertIndex)

        expandedPanel = panel
        expandedItem = item
        categories.forEach { it.isExpanded = it == item }
        updateFolderStates()

        AnimationUtils.expandHeightFadeIn(
            view = panel,
            duration = UiConstants.PANEL_OPEN_DURATION_MS
        )
    }

    private fun closePanelThen(openNext: ExpandableCategoryItem?) {
        if (isAnimating) return

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

        isAnimating = true

        AnimationUtils.collapseHeightFadeOut(
            view = panelToRemove,
            duration = UiConstants.PANEL_CLOSE_DURATION_MS
        ) {
            categoriesContainer.removeView(panelToRemove)
            expandedPanel = null
            expandedItem = null
            categories.forEach { it.isExpanded = false }
            updateFolderStates()
            isAnimating = false

            if (openNext != null) {
                categoriesContainer.post {
                    insertPanelBelowItem(openNext)
                }
            }
        }
    }

    private fun updateFolderStates() {
        folderViews.forEach { (item, folderCard) ->
            val isExpanded = item == expandedItem

            folderCard.animate()
                .alpha(if (isExpanded) 0.90f else 1f)
                .scaleX(if (isExpanded) 0.985f else 1f)
                .scaleY(if (isExpanded) 0.985f else 1f)
                .setDuration(120)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
    }
}
