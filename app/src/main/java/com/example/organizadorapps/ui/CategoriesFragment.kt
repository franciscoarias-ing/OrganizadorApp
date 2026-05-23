package com.example.organizadorapps.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.organizadorapps.AppCategory
import com.example.organizadorapps.InstalledApp

class CategoriesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
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

        val title = TextView(requireContext()).apply {
            text = "Categorías"
            textSize = 28f
            setTextColor(Color.WHITE)
        }

        val subtitle = TextView(requireContext()).apply {
            text = "${apps.size} apps detectadas"
            textSize = 14f
            setTextColor(Color.parseColor("#9AA0A6"))
            setPadding(0, 8, 0, 32)
        }

        layout.addView(title)
        layout.addView(subtitle)

        val categories = listOf(

            AppCategory(
                "Social",
                apps.filter {
                    it.name.contains("WhatsApp", true) ||
                            it.name.contains("Instagram", true) ||
                            it.name.contains("Telegram", true) ||
                            it.name.contains("Facebook", true) ||
                            it.name.contains("TikTok", true)
                }
            ),

            AppCategory(
                "Google",
                apps.filter {
                    it.name.contains("Google", true) ||
                            it.name.contains("Chrome", true) ||
                            it.name.contains("Gmail", true) ||
                            it.name.contains("Maps", true) ||
                            it.name.contains("Drive", true) ||
                            it.name.contains("YouTube", true)
                }
            ),

            AppCategory(
                "Multimedia",
                apps.filter {
                    it.name.contains("Spotify", true) ||
                            it.name.contains("Netflix", true) ||
                            it.name.contains("Prime", true) ||
                            it.name.contains("Music", true)
                }
            )
        )

        categories.forEach { category ->

            if (category.apps.isNotEmpty()) {

                val categoryTitle = TextView(requireContext()).apply {
                    text = category.name
                    textSize = 20f
                    setTextColor(Color.WHITE)
                    setPadding(0, 24, 0, 18)
                }

                layout.addView(categoryTitle)

                val grid = GridLayout(requireContext()).apply {
                    columnCount = 4
                }

                category.apps.forEach { app ->

                    val card = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER
                        setPadding(16, 16, 16, 16)
                        background = android.graphics.drawable.GradientDrawable().apply {
                            setColor(Color.parseColor("#1A1F2B"))
                            cornerRadius = 28f
                            setStroke(1, Color.parseColor("#2B3140"))
                        }

                        setOnClickListener {

                            val launchIntent =
                                requireContext().packageManager
                                    .getLaunchIntentForPackage(app.packageName)

                            if (launchIntent != null) {
                                startActivity(launchIntent)
                            }
                        }
                    }

                    val params = GridLayout.LayoutParams().apply {
                        width = 220
                        height = 260
                        setMargins(12, 12, 12, 12)
                    }

                    card.layoutParams = params

                    val icon = ImageView(requireContext()).apply {
                        setImageDrawable(app.icon)
                        layoutParams = LinearLayout.LayoutParams(96, 96)
                    }

                    val name = TextView(requireContext()).apply {
                        text = app.name
                        textSize = 12f
                        setTextColor(Color.parseColor("#E5E7EB"))
                        gravity = Gravity.CENTER
                        setPadding(0, 16, 0, 0)
                    }

                    card.addView(icon)
                    card.addView(name)

                    grid.addView(card)
                }

                layout.addView(grid)
            }
        }

        rootScroll.addView(layout)

        return rootScroll
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