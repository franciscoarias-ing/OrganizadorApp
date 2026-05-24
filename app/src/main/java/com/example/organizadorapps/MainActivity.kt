package com.example.organizadorapps

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import com.example.organizadorapps.ui.FavoritesFragment
import com.example.organizadorapps.ui.HomeFragment
import com.example.organizadorapps.ui.UsageFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {

    private val containerId = 1001
    private val navHome = 1

    private val navFavorites = 3
    private val navUsage = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(UiConstants.BACKGROUND)
        }

        val fragmentContainer = FrameLayout(this).apply {
            id = containerId
            setBackgroundColor(UiConstants.BACKGROUND)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val bottomNav = BottomNavigationView(this).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = AppUiUtils.dp(this@MainActivity, 28).toFloat()
                setColor(Color.parseColor("#0F172A"))
                setStroke(
                    AppUiUtils.dp(this@MainActivity, 1),
                    Color.parseColor("#1E293B")
                )
            }

            itemIconTintList = ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf()
                ),
                intArrayOf(
                    UiConstants.ACCENT,
                    UiConstants.TEXT_SECONDARY
                )
            )

            itemTextColor = ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf()
                ),
                intArrayOf(
                    UiConstants.ACCENT,
                    UiConstants.TEXT_SECONDARY
                )
            )

            itemRippleColor = ColorStateList.valueOf(Color.TRANSPARENT)
            itemActiveIndicatorColor = ColorStateList.valueOf(Color.TRANSPARENT)
            labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_LABELED

            menu.add(Menu.NONE, navHome, Menu.NONE, "Inicio")
                .setIcon(android.R.drawable.ic_menu_view)


            menu.add(Menu.NONE, navFavorites, Menu.NONE, "Favoritos")
                .setIcon(android.R.drawable.btn_star_big_off)

            menu.add(Menu.NONE, navUsage, Menu.NONE, "Uso")
                .setIcon(android.R.drawable.ic_menu_sort_by_size)

            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    navHome -> openFragment(HomeFragment())
                    navFavorites -> openFragment(FavoritesFragment())
                    navUsage -> openFragment(UsageFragment())
                }
                true
            }
        }

        root.addView(fragmentContainer)
        root.addView(
            bottomNav,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                AppUiUtils.dp(this, 66)
            ).apply {
                setMargins(
                    AppUiUtils.dp(this@MainActivity, 16),
                    0,
                    AppUiUtils.dp(this@MainActivity, 16),
                    AppUiUtils.dp(this@MainActivity, 8)
                )
            }
        )

        setContentView(root)

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            fragmentContainer.setPadding(
                0,
                systemBars.top,
                0,
                0
            )

            bottomNav.setPadding(
                0,
                0,
                0,
                systemBars.bottom
            )

            insets
        }

        ViewCompat.requestApplyInsets(root)

        if (savedInstanceState == null) {
            bottomNav.selectedItemId = navHome
            openFragment(HomeFragment())
        }
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(containerId, fragment)
            .commit()
    }
}