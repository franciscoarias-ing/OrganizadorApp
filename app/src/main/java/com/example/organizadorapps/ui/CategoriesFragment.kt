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
import com.example.organizadorapps.CategoryRules
import com.example.organizadorapps.InstalledApp

class CategoriesFragment : Fragment() {

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val apps = getInstalledLaunchableApps()

        val rootScroll = ScrollView(requireContext()).apply {
            setBackgroundColor(Color.parseColor("#0F1115"))
        }

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 48)
        }

        layout.addView(TextView(requireContext()).apply {
            text = "Categorías"
            textSize = 28f
            setTextColor(Color.WHITE)
        })

        layout.addView(TextView(requireContext()).apply {
            text = "${apps.size} apps detectadas"
            textSize = 14f
            setTextColor(Color.parseColor("#9AA0A6"))
            setPadding(0, 8, 0, 32)
        })

        val categories = CategoryRules.rules.map { rule ->
            AppCategory(
                rule.key,
                apps.filter { app ->
                    val searchable = "${app.name} ${app.packageName}".lowercase()
                    rule.value.any { keyword -> searchable.contains(keyword) }
                }
            )
        }

        categories.forEach { category ->
            if (category.apps.isNotEmpty()) {
                addCategorySection(layout, category)
            }
        }

        rootScroll.addView(layout)
        return rootScroll
    }

    private fun addCategorySection(layout: LinearLayout, category: AppCategory) {
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
            background = roundedCard()
            isClickable = true
            isFocusable = true

            setOnClickListener {
                val launchIntent =
                    requireContext().packageManager.getLaunchIntentForPackage(app.packageName)

                if (launchIntent != null) {
                    startActivity(launchIntent)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "No se pudo abrir ${app.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
        })

        return card
    }

    private fun roundedCard(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.parseColor("#1A1F2B"))
            cornerRadius = 28f
            setStroke(1, Color.parseColor("#2B3140"))
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
            .sortedBy { it.name }
    }
}