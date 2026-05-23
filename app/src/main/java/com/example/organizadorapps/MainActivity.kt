package com.example.organizadorapps

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apps = getInstalledLaunchableApps()

        val rootScroll = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#0F1115"))
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 48)
        }

        val title = TextView(this).apply {
            text = "OrganizadorApp"
            textSize = 28f
            setTextColor(Color.WHITE)
        }

        val subtitle = TextView(this).apply {
            text = "${apps.size} apps detectadas"
            textSize = 14f
            setTextColor(Color.parseColor("#9AA0A6"))
            setPadding(0, 8, 0, 32)
        }

        container.addView(title)
        container.addView(subtitle)

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
            ),

            AppCategory(
                "Herramientas",
                apps.filter {
                    it.name.contains("Camera", true) ||
                            it.name.contains("Clock", true) ||
                            it.name.contains("Settings", true) ||
                            it.name.contains("Files", true)
                }
            )
        )

        categories.forEach { category ->

            if (category.apps.isNotEmpty()) {

                val categoryTitle = TextView(this).apply {
                    text = category.name
                    textSize = 20f
                    setTextColor(Color.WHITE)
                    setPadding(0, 24, 0, 18)
                }

                container.addView(categoryTitle)

                val grid = GridLayout(this).apply {
                    columnCount = 4
                }

                category.apps.forEach { app ->

                    val card = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER
                        setPadding(16, 16, 16, 16)
                        background = getDrawable(android.R.drawable.dialog_holo_light_frame)

                        setOnClickListener {

                            val launchIntent =
                                packageManager.getLaunchIntentForPackage(app.packageName)

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

                    val icon = ImageView(this).apply {
                        setImageDrawable(app.icon)
                        layoutParams = LinearLayout.LayoutParams(96, 96)
                    }

                    val name = TextView(this).apply {
                        text = app.name
                        textSize = 12f
                        setTextColor(Color.WHITE)
                        gravity = Gravity.CENTER
                        setPadding(0, 16, 0, 0)
                    }

                    card.addView(icon)
                    card.addView(name)

                    grid.addView(card)
                }

                container.addView(grid)
            }
        }

        rootScroll.addView(container)

        setContentView(rootScroll)
    }

    private fun getInstalledLaunchableApps(): List<InstalledApp> {

        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return packageManager
            .queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .map {

                InstalledApp(
                    name = it.loadLabel(packageManager).toString(),
                    packageName = it.activityInfo.packageName,
                    icon = it.loadIcon(packageManager)
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.name }
    }
}