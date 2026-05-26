package com.example.organizadorapps

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Componente reutilizable para la sección "Favoritos rápidos".
 *
 * Centraliza la lógica que antes estaba duplicada entre HomeFragment y CompactLauncherActivity:
 * - Header con acción Editar/Cerrar.
 * - Render de favoritos vacíos/con apps.
 * - Editor inline con búsqueda.
 * - Animación de apertura/cierre.
 * - Ocultamiento de teclado al cerrar.
 *
 * La diferencia visual entre Home y Compact se controla mediante [mode].
 */
class FavoritesSectionController(
    private val context: Context,
    private val inflater: LayoutInflater,
    private val allAppsProvider: () -> List<InstalledApp>,
    private val mode: Mode,
    private val displayLimit: Int,
    private val maxFavorites: Int = UiConstants.MAX_FAVORITES,
    private val closeAfterLaunch: (() -> Unit)? = null,
    private val bottomMarginDp: Int = 12
) {

    enum class Mode {
        HOME,
        COMPACT
    }

    private lateinit var rowHost: LinearLayout
    private lateinit var editorHost: FrameLayout
    private lateinit var actionText: TextView

    private var isEditorExpanded = false
    private var editorAdapter: FavoriteEditorAdapter? = null

    fun addTo(root: LinearLayout) {
        root.addView(buildHeader())

        rowHost = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        root.addView(rowHost)

        editorHost = FrameLayout(context).apply {
            visibility = View.GONE
            alpha = 0f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0
            ).apply {
                bottomMargin = AppUiUtils.dp(context, bottomMarginDp)
            }
        }
        root.addView(editorHost)

        refresh()
    }

    fun refresh() {
        if (!::rowHost.isInitialized) return

        rowHost.removeAllViews()

        val favoritePackages = FavoritesManager.getFavoritePackageList(context)
        val appsByPackage = allAppsProvider().associateBy { it.packageName }
        val favoriteApps = favoritePackages
            .mapNotNull { appsByPackage[it] }
            .take(displayLimit)

        if (favoriteApps.isEmpty()) {
            rowHost.addView(emptyFavoritesView())
            return
        }

        rowHost.addView(
            when (mode) {
                Mode.HOME -> homeFavoritesRow(favoriteApps)
                Mode.COMPACT -> compactFavoritesRow(favoriteApps)
            }
        )
    }

    fun collapse() {
        if (!isEditorExpanded || !::editorHost.isInitialized) return

        val searchInput = editorHost.findViewById<EditText>(R.id.editSearchFavorites)
        searchInput?.clearFocus()
        searchInput?.let { hideKeyboard(it) }

        isEditorExpanded = false
        actionText.text = "Editar"

        val startHeight = editorHost.height

        ValueAnimator.ofInt(startHeight, 0).apply {
            duration = UiConstants.COLLAPSE_DURATION_MS
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener { animator ->
                val value = animator.animatedValue as Int

                editorHost.layoutParams = editorHost.layoutParams.apply {
                    height = value
                }

                editorHost.alpha = 1f - animator.animatedFraction
                editorHost.translationY = -AppUiUtils.dp(context, 8).toFloat() * animator.animatedFraction
            }

            addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        editorHost.visibility = View.GONE
                        editorHost.alpha = 0f
                        editorHost.translationY = 0f
                        editorHost.layoutParams = editorHost.layoutParams.apply {
                            height = 0
                        }
                    }
                }
            )

            start()
        }
    }

    private fun buildHeader(): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                0,
                if (mode == Mode.COMPACT) AppUiUtils.dp(context, 12) else 0,
                0,
                AppUiUtils.dp(context, 10)
            )

            addView(
                TextView(context).apply {
                    text = "Favoritos rápidos"
                    textSize = 18f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(if (mode == Mode.HOME) Color.WHITE else UiConstants.TEXT_PRIMARY)
                    includeFontPadding = false

                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }
            )

            actionText = TextView(context).apply {
                text = "Editar"
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(UiConstants.ACCENT)
                includeFontPadding = false
                gravity = Gravity.CENTER
                setPadding(
                    AppUiUtils.dp(context, 12),
                    AppUiUtils.dp(context, 6),
                    0,
                    AppUiUtils.dp(context, 6)
                )

                setOnClickListener {
                    toggleEditor()
                }
            }

            addView(actionText)
        }
    }

    private fun emptyFavoritesView(): View {
        return when (mode) {
            Mode.HOME -> AppUiUtils.miniEmpty(
                context,
                "Agrega tus apps favoritas desde Editar."
            ).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = AppUiUtils.dp(context, 12)
                }
            }

            Mode.COMPACT -> TextView(context).apply {
                text = "Agrega tus apps favoritas desde Editar."
                textSize = 13f
                setTextColor(UiConstants.TEXT_SECONDARY)
                setPadding(0, AppUiUtils.dp(context, 4), 0, AppUiUtils.dp(context, 8))
            }
        }
    }

    private fun homeFavoritesRow(apps: List<InstalledApp>): RecyclerView {
        return RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )

            adapter = AppAdapter(
                apps = apps,
                mode = AppAdapter.Mode.FAVORITE,
                showAddFavorite = false,
                onAddFavoriteClick = null
            )

            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            isNestedScrollingEnabled = false

            setPadding(0, 0, AppUiUtils.dp(context, 6), 0)
            clipToPadding = false

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                AppUiUtils.dp(context, 66)
            ).apply {
                bottomMargin = AppUiUtils.dp(context, 12)
            }
        }
    }

    private fun compactFavoritesRow(apps: List<InstalledApp>): View {
        return LauncherUiFactory.compactAppRow(
            context = context,
            apps = apps,
            closeAfterLaunch = closeAfterLaunch
        )
    }

    private fun toggleEditor() {
        if (isEditorExpanded) {
            collapse()
        } else {
            expand()
        }
    }

    private fun expand() {
        if (isEditorExpanded) return

        ensureEditorBuilt()

        isEditorExpanded = true
        actionText.text = "Cerrar"

        editorHost.visibility = View.VISIBLE
        editorHost.alpha = 0f
        editorHost.translationY = -AppUiUtils.dp(context, 8).toFloat()

        val parentWidth = (editorHost.parent as? View)?.width
            ?: context.resources.displayMetrics.widthPixels

        editorHost.measure(
            View.MeasureSpec.makeMeasureSpec(parentWidth, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        val targetHeight = editorHost.measuredHeight.coerceAtLeast(
            AppUiUtils.dp(context, 360)
        )

        editorHost.layoutParams = editorHost.layoutParams.apply {
            height = 0
        }

        ValueAnimator.ofInt(0, targetHeight).apply {
            duration = UiConstants.EXPAND_DURATION_MS
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener { animator ->
                val value = animator.animatedValue as Int

                editorHost.layoutParams = editorHost.layoutParams.apply {
                    height = value
                }

                editorHost.alpha = animator.animatedFraction
                editorHost.translationY =
                    -AppUiUtils.dp(context, 8).toFloat() * (1f - animator.animatedFraction)
            }

            start()
        }
    }

    private fun ensureEditorBuilt() {
        if (editorHost.childCount > 0) return

        val editorView = inflater.inflate(
            R.layout.layout_inline_favorites_editor,
            editorHost,
            false
        )

        val btnCollapse = editorView.findViewById<FrameLayout>(R.id.btnCollapseFavoritesEditor)
        val searchInput = editorView.findViewById<EditText>(R.id.editSearchFavorites)
        val recyclerApps = editorView.findViewById<RecyclerView>(R.id.recyclerFavoriteEditorApps)

        val adapter = FavoriteEditorAdapter(
            allApps = allAppsProvider().sortedBy { it.name.lowercase() },
            maxFavorites = maxFavorites,
            onLimitReached = {
                android.widget.Toast.makeText(
                    context,
                    "Puedes tener hasta $maxFavorites favoritos.",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            },
            onFavoriteChanged = {
                refresh()
                editorAdapter?.refreshFavoritesState()
            }
        )

        editorAdapter = adapter

        recyclerApps.apply {
            layoutManager = LinearLayoutManager(context)
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

        searchInput.addTextChangedListener(
            object : android.text.TextWatcher {
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

                override fun afterTextChanged(s: android.text.Editable?) {
                }
            }
        )

        btnCollapse.setOnClickListener {
            collapse()
        }

        editorHost.addView(
            editorView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
