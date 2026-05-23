package com.example.organizadorapps

import android.os.Bundle
import android.view.Menu
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.organizadorapps.ui.CategoriesFragment
import com.example.organizadorapps.ui.FavoritesFragment
import com.example.organizadorapps.ui.HomeFragment
import com.example.organizadorapps.ui.UsageFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val containerId = 1001
    private val navHome = 1
    private val navCategories = 2
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
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val bottomNav = BottomNavigationView(this).apply {
            setBackgroundColor(UiConstants.SURFACE)
            itemIconTintList = null
            itemTextColor = android.content.res.ColorStateList.valueOf(UiConstants.TEXT_SECONDARY)
            labelVisibilityMode = com.google.android.material.navigation.NavigationBarView.LABEL_VISIBILITY_LABELED

            menu.add(Menu.NONE, navHome, Menu.NONE, "Inicio")
                .setIcon(android.R.drawable.ic_menu_view)

            menu.add(Menu.NONE, navCategories, Menu.NONE, "Categorías")
                .setIcon(android.R.drawable.ic_dialog_dialer)

            menu.add(Menu.NONE, navFavorites, Menu.NONE, "Favoritos")
                .setIcon(android.R.drawable.btn_star_big_off)

            menu.add(Menu.NONE, navUsage, Menu.NONE, "Uso")
                .setIcon(android.R.drawable.ic_menu_sort_by_size)

            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    navHome -> openFragment(HomeFragment())
                    navCategories -> openFragment(CategoriesFragment())
                    navFavorites -> openFragment(FavoritesFragment())
                    navUsage -> openFragment(UsageFragment())
                }
                true
            }
        }

        root.addView(fragmentContainer)
        root.addView(bottomNav)

        setContentView(root)

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