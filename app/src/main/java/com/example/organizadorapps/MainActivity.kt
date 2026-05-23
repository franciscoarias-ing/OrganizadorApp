package com.example.organizadorapps

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.organizadorapps.ui.CategoriesFragment
import com.example.organizadorapps.ui.FavoritesFragment
import com.example.organizadorapps.ui.HomeFragment
import com.example.organizadorapps.ui.UsageFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : FragmentActivity() {

    private val fragmentContainerId = 1001

    private val menuHome = 1
    private val menuCategories = 2
    private val menuFavorites = 3
    private val menuUsage = 4

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(UiConstants.BACKGROUND)
        }

        val fragmentContainer = FrameLayout(this).apply {
            id = fragmentContainerId
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val navColors = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf()
            ),
            intArrayOf(
                UiConstants.ACCENT,
                UiConstants.TEXT_MUTED
            )
        )

        val bottomNavigation = BottomNavigationView(this).apply {
            setBackgroundColor(Color.parseColor("#111827"))
            itemIconTintList = navColors
            itemTextColor = navColors
            itemRippleColor = ColorStateList.valueOf(UiConstants.ACCENT_SOFT)
            labelVisibilityMode =
                com.google.android.material.navigation.NavigationBarView.LABEL_VISIBILITY_LABELED

            menu.add(0, menuHome, 0, "Inicio")
                .setIcon(android.R.drawable.ic_menu_view)

            menu.add(0, menuCategories, 1, "Categorías")
                .setIcon(android.R.drawable.ic_menu_sort_by_size)

            menu.add(0, menuFavorites, 2, "Favoritos")
                .setIcon(android.R.drawable.star_big_on)

            menu.add(0, menuUsage, 3, "Uso")
                .setIcon(android.R.drawable.ic_menu_recent_history)

            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    menuHome -> showFragment(HomeFragment())
                    menuCategories -> showFragment(CategoriesFragment())
                    menuFavorites -> showFragment(FavoritesFragment())
                    menuUsage -> showFragment(UsageFragment())
                }
                true
            }
        }

        root.addView(fragmentContainer)
        root.addView(bottomNavigation)

        setContentView(root)

        bottomNavigation.selectedItemId = menuHome
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(fragmentContainerId, fragment)
            .commit()
    }
}