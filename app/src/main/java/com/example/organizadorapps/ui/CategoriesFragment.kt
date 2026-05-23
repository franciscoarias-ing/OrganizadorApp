package com.example.organizadorapps.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.organizadorapps.AppCategory
import com.example.organizadorapps.AppLauncher
import com.example.organizadorapps.CategoryRules
import com.example.organizadorapps.InstalledApp

class CategoriesFragment : Fragment() {

    private lateinit var layout: LinearLayout
    private lateinit var allApps: List<InstalledApp>

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        allApps = getInstalledLaunchableApps()

        val rootScroll = ScrollView(requireContext()).apply {
            setBackgroundColor(Color.parseColor("#0F1115"))
        }

        layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 48)
        }

        renderContent(allApps)

        rootScroll.addView(layout)
        return rootScroll
    }

    private fun renderContent(apps: List<InstalledApp>) {
        layout.removeAllViews()

        layout.addView(TextView(requireContext()).apply {
            text = "Categorías"
            textSize = 28f
            setTextColor(Color.WHITE)
        })

        layout.addView(TextView(requireContext()).apply {
            text = "${apps.size} apps detectadas"
            textSize = 14f
            setTextColor(Color.parseColor("#9AA0A6"))
            setPadding(0, 8, 0, 24)
        })

        layout.addView(searchBox())

        val categories = buildCategories(apps)

        categories.forEach { category ->
            if (category.apps.isNotEmpty()) {
                addCategorySection(category)
            }
        }
    }

    private fun searchBox(): EditText {
        return EditText(requireContext()).apply {
            hint = "Buscar apps..."
            textSize = 15f
            setHintTextColor(Color.parseColor("#777E8C"))
            setTextColor(Color.WHITE)
            setSingleLine(true)
            setPadding(28, 0, 28, 0)
            background = roundedBox("#1A1F2B", "#2B3140", 24f)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                96
            ).apply {
                setMargins(0, 0, 0, 24)
            }

            addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val query = s.toString().trim().lowercase()

                    val filteredApps = if (query.isEmpty()) {
                        allApps
                    } else {
                        allApps.filter {
                            it.name.lowercase().contains(query) ||
                                    it.packageName.lowercase().contains(query)
                        }
                    }

                    renderContent(filteredApps)
                }

                override fun afterTextChanged(s: android.text.Editable?) {}
            })
        }
    }

    private fun buildCategories(apps: List<InstalledApp>): List<AppCategory> {
        val ruleCategories = CategoryRules.rules.map { rule ->
            AppCategory(
                rule.key,
                apps.filter { app ->
                    val searchable = "${app.name} ${app.packageName}".lowercase()
                    rule.value.any { keyword -> searchable.contains(keyword) }
                }
            )
        }

        return ruleCategories + AppCategory("Todas las apps", apps)
    }

    private fun addCategorySection(category: AppCategory) {
        layout.addView(TextView(requireContext()).apply {
            text = category.name
            textSize = 20f
            setTextColor(Color.WHITE)
            setPadding(0, 24, 0, 18)
        })

        val grid = GridLayout(requireContext()).apply {
            columnCount = 4
        }

        category.apps.forEach { app ->
            grid.addView(appCard(app))
        }

        layout.addView(grid)
    }

    private fun appCard(app: InstalledApp): View {
        val card = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
            background = roundedBox("#1A1F2B", "#2B3140", 28f)
            isClickable = true
            isFocusable = true

            setOnClickListener {
                AppLauncher.openApp(requireContext(), app.packageName, app.name)
            }
        }

        card.layoutParams = GridLayout.LayoutParams().apply {
            width = 220
            height = 260
            setMargins(12, 12, 12, 12)
        }

        card.addView(ImageView(requireContext()).apply {
            setImageDrawable(app.icon)
            layoutParams = LinearLayout.LayoutParams(96, 96)
        })

        card.addView(TextView(requireContext()).apply {
            text = app.name
            textSize = 12f
            setTextColor(Color.parseColor("#E5E7EB"))
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 0)
            maxLines = 2
        })

        return card
    }

    private fun roundedBox(bgColor: String, strokeColor: String, radius: Float): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.parseColor(bgColor))
            cornerRadius = radius
            setStroke(1, Color.parseColor(strokeColor))
        }
    }

    private fun getInstalledLaunchableApps(): List<InstalledApp> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return requireContext()
            .packageManager
            .queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .map {
                InstalledApp(
                    name = it.loadLabel(requireContext().packageManager).toString(),
                    packageName = it.activityInfo.packageName,
                    icon = it.loadIcon(requireContext().packageManager)
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.name.lowercase() }
    }
}