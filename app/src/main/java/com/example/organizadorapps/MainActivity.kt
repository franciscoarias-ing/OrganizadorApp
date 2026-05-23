package com.example.organizadorapps
import android.annotation.SuppressLint
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

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0F1115"))
        }

        val fragmentContainer = FrameLayout(this).apply {
            id = fragmentContainerId
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val bottomNavigation = BottomNavigationView(this).apply {
            setBackgroundColor(Color.parseColor("#151922"))
            itemIconTintList = null

            menu.add(0, 1, 0, "Inicio")
                .setIcon(android.R.drawable.ic_menu_view)

            menu.add(0, 2, 1, "Categorías")
                .setIcon(android.R.drawable.ic_menu_sort_by_size)

            menu.add(0, 3, 2, "Favoritos")
                .setIcon(android.R.drawable.star_big_on)

            menu.add(0, 4, 3, "Uso")
                .setIcon(android.R.drawable.ic_menu_recent_history)

            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    1 -> showFragment(HomeFragment())
                    2 -> showFragment(CategoriesFragment())
                    3 -> showFragment(FavoritesFragment())
                    4 -> showFragment(UsageFragment())
                }
                true
            }
        }

        root.addView(fragmentContainer)
        root.addView(bottomNavigation)

        setContentView(root)

        bottomNavigation.selectedItemId = 2
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(fragmentContainerId, fragment)
            .commit()
    }
}