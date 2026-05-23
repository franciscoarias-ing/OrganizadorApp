package com.example.organizadorapps.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadorapps.AppAdapter
import com.example.organizadorapps.AppCategory
import com.example.organizadorapps.AppRepository
import com.example.organizadorapps.AppUiUtils
import com.example.organizadorapps.CategoryFolderAdapter
import com.example.organizadorapps.CategorySuggestionEngine
import com.example.organizadorapps.FavoritesManager
import com.example.organizadorapps.RecentAppsManager
import com.example.organizadorapps.UiConstants

class HomeFragment : Fragment() {

    private lateinit var allApps: List<com.example.organizadorapps.InstalledApp>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        allApps = AppRepository.getInstalledLaunchableApps(requireContext())

        val favoritePackages = FavoritesManager.getFavoritePackages(requireContext())
        val favoriteApps = allApps.filter { favoritePackages.contains(it.packageName) }

        val recentPackages = RecentAppsManager.getRecentPackageNames(requireContext())
        val recentApps = recentPackages.mapNotNull { packageName ->
            allApps.find { it.packageName == packageName }
        }

        val categories = CategorySuggestionEngine
            .categorizeApps(allApps)
            .filter { it.apps.isNotEmpty() }
            .toMutableList()

        categories.add(
            AppCategory("Todas las apps", allApps)
        )

        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(UiConstants.BACKGROUND)
            setPadding(24, 62, 24, 18)
        }

        root.addView(AppUiUtils.smallGreeting(requireContext(), "¡Buenos días! 👋"))
        root.addView(AppUiUtils.title(requireContext(), "Inicio inteligente"))
        root.addView(
            AppUiUtils.subtitle(
                requireContext(),
                "Accede rápido a lo que más usas."
            )
        )

        root.addView(
            AppUiUtils.searchButton(
                context = requireContext(),
                hint = "Buscar apps..."
            ) {
                openFragment(AllAppsFragment())
            }
        )

        root.addView(quickActions())

        addRecentSection(root, recentApps)
        addFavoritesSection(root, favoriteApps)
        addCategorySection(root, categories)

        return root
    }

    private fun quickActions(): LinearLayout {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            background = AppUiUtils.glassCard()
            setPadding(18, 18, 18, 18)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                128
            ).apply {
                setMargins(0, 0, 0, 26)
            }

            addView(
                AppUiUtils.quickAction(
                    requireContext(),
                    "⌘",
                    "Todas"
                ) {
                    openFragment(AllAppsFragment())
                }
            )

            addView(
                AppUiUtils.quickAction(
                    requireContext(),
                    "★",
                    "Favorito"
                ) {
                    Toast.makeText(requireContext(), "Mantén presionada una app para agregar favorito", Toast.LENGTH_SHORT).show()
                }
            )

            addView(
                AppUiUtils.quickAction(
                    requireContext(),
                    "⌗",
                    "Escanear"
                ) {
                    Toast.makeText(requireContext(), "Tus apps ya fueron detectadas automáticamente", Toast.LENGTH_SHORT).show()
                }
            )

            addView(
                AppUiUtils.quickAction(
                    requireContext(),
                    "✦",
                    "Sugerir"
                ) {
                    Toast.makeText(requireContext(), "Categorías sugeridas automáticamente", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun addRecentSection(
        root: LinearLayout,
        apps: List<com.example.organizadorapps.InstalledApp>
    ) {
        root.addView(AppUiUtils.sectionRow(requireContext(), "Recientes", "Ver todos") {
            openFragment(AllAppsFragment())
        })

        if (apps.isEmpty()) {
            root.addView(AppUiUtils.miniEmpty(requireContext(), "Abre apps desde OrganizadorApp para verlas aquí."))
            return
        }

        root.addView(
            RecyclerView(requireContext()).apply {
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                    requireContext(),
                    androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
                    false
                )
                adapter = AppAdapter(apps.take(10))
                overScrollMode = RecyclerView.OVER_SCROLL_NEVER

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    205
                )
            }
        )
    }

    private fun addFavoritesSection(
        root: LinearLayout,
        apps: List<com.example.organizadorapps.InstalledApp>
    ) {
        root.addView(AppUiUtils.sectionRow(requireContext(), "Favoritos rápidos", "Ver todos") {
            openFragment(FavoritesFragment())
        })

        if (apps.isEmpty()) {
            root.addView(AppUiUtils.miniEmpty(requireContext(), "Mantén presionada una app para agregarla."))
            return
        }

        root.addView(
            RecyclerView(requireContext()).apply {
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                    requireContext(),
                    androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
                    false
                )
                adapter = AppAdapter(apps.take(10))
                overScrollMode = RecyclerView.OVER_SCROLL_NEVER

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    205
                )
            }
        )
    }

    private fun addCategorySection(
        root: LinearLayout,
        categories: List<AppCategory>
    ) {
        root.addView(AppUiUtils.sectionRow(requireContext(), "Mis categorías", "Editar") {
            Toast.makeText(requireContext(), "Edición de categorías próximamente", Toast.LENGTH_SHORT).show()
        })

        root.addView(
            RecyclerView(requireContext()).apply {
                layoutManager = GridLayoutManager(requireContext(), 3)
                adapter = CategoryFolderAdapter(categories) { category ->
                    if (category.name == "Todas las apps") {
                        openFragment(AllAppsFragment())
                    } else {
                        openFragment(CategoryDetailFragment(category.name, category.apps))
                    }
                }
                overScrollMode = RecyclerView.OVER_SCROLL_NEVER

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            }
        )
    }

    private fun openFragment(fragment: Fragment) {
        val containerId = (view?.parent as? ViewGroup)?.id ?: return

        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(containerId, fragment)
            .addToBackStack(null)
            .commit()
    }
}