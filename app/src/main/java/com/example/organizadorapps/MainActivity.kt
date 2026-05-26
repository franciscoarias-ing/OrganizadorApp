package com.example.organizadorapps

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.organizadorapps.ui.HomeFragment
import com.example.organizadorapps.ui.UsageFragment

class MainActivity : AppCompatActivity() {

    private val containerId = 1001

    private val navHome = 1

    private val navUsage = 3

    private lateinit var homeItem: LinearLayout
    private lateinit var usageItem: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        WallpaperBackgroundManager.prepareWallpaperWindow(window)

        val wallpaperRoot = WallpaperBackgroundManager.buildWallpaperLayer(this)

        val wallpaperScrim = View(this).apply {
            setBackgroundColor(WallpaperBackgroundManager.wallpaperScrimColor())
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.TRANSPARENT)
        }

        val fragmentContainer = FrameLayout(this).apply {
            id = containerId
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val bottomBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(
                AppUiUtils.dp(this@MainActivity, 10),
                AppUiUtils.dp(this@MainActivity, 6),
                AppUiUtils.dp(this@MainActivity, 10),
                AppUiUtils.dp(this@MainActivity, 6)
            )

            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = AppUiUtils.dp(this@MainActivity, 18).toFloat()
                setColor(Color.parseColor("#0F172A"))
                setStroke(
                    AppUiUtils.dp(this@MainActivity, 1),
                    Color.parseColor("#1E293B")
                )
            }
        }

        homeItem = createNavItem(
            title = "Inicio",
            iconRes = R.drawable.ic_nav_home
        ) {
            selectNav(navHome)
            openFragment(HomeFragment())
        }



        usageItem = createNavItem(
            title = "Uso",
            iconRes = R.drawable.ic_nav_usage
        ) {
            selectNav(navUsage)
            openFragment(UsageFragment())
        }

        bottomBar.addView(homeItem)
        bottomBar.addView(usageItem)

        root.addView(fragmentContainer)

        root.addView(
            bottomBar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                AppUiUtils.dp(this, 64)
            ).apply {
                setMargins(
                    AppUiUtils.dp(this@MainActivity, 14),
                    0,
                    AppUiUtils.dp(this@MainActivity, 14),
                    AppUiUtils.dp(this@MainActivity, 8)
                )
            }
        )

        wallpaperRoot.addView(
            wallpaperScrim,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        wallpaperRoot.addView(
            root,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        setContentView(wallpaperRoot)

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            fragmentContainer.setPadding(
                0,
                systemBars.top,
                0,
                0
            )

            root.setPadding(
                0,
                0,
                0,
                systemBars.bottom
            )

            insets
        }

        ViewCompat.requestApplyInsets(root)

        if (savedInstanceState == null) {
            selectNav(navHome)
            openFragment(HomeFragment())
        }
    }

    private fun createNavItem(
        title: String,
        iconRes: Int,
        onClick: () -> Unit
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )

            addView(
                ImageView(this@MainActivity).apply {
                    setImageResource(iconRes)
                    tag = "icon"

                    layoutParams = LinearLayout.LayoutParams(
                        AppUiUtils.dp(this@MainActivity, 20),
                        AppUiUtils.dp(this@MainActivity, 20)
                    )
                }
            )

            addView(
                TextView(this@MainActivity).apply {
                    text = title
                    tag = "label"
                    textSize = 11f
                    includeFontPadding = false
                    gravity = Gravity.CENTER

                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = AppUiUtils.dp(this@MainActivity, 4)
                    }
                }
            )
        }
    }

    private fun selectNav(selected: Int) {
        updateNavItem(homeItem, selected == navHome)
        updateNavItem(usageItem, selected == navUsage)
    }

    private fun updateNavItem(item: LinearLayout, selected: Boolean) {
        val color = if (selected) UiConstants.ACCENT else UiConstants.TEXT_SECONDARY

        item.background = if (selected) {
            GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = AppUiUtils.dp(this@MainActivity, 22).toFloat()
                setColor(Color.parseColor("#1E1238"))
            }
        } else {
            null
        }

        item.setPadding(
            0,
            AppUiUtils.dp(this, 4),
            0,
            AppUiUtils.dp(this, 4)
        )

        for (i in 0 until item.childCount) {
            val child = item.getChildAt(i)

            if (child is ImageView) {
                child.setColorFilter(color)
            }

            if (child is TextView) {
                child.setTextColor(color)
                child.typeface = if (selected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            }
        }
    }

    private fun openFragment(fragment: Fragment) {
        val currentFragment = supportFragmentManager.findFragmentById(containerId)
        if (currentFragment != null && currentFragment::class == fragment::class) return

        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(containerId, fragment, fragment::class.java.simpleName)
            .commit()
    }
}