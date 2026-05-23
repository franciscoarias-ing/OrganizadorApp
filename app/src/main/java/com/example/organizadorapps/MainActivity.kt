package com.example.organizadorapps

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apps = getInstalledLaunchableApps()

        val scrollView = ScrollView(this)

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 60, 32, 32)
        }

        val title = TextView(this).apply {
            text = "Organizador Apps"
            textSize = 28f
            setPadding(0, 0, 0, 40)
        }

        container.addView(title)

        apps.forEach { app ->

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 16, 0, 16)
            }

            val iconView = ImageView(this).apply {
                setImageDrawable(app.icon)

                layoutParams = LinearLayout.LayoutParams(96, 96)
            }

            val nameView = TextView(this).apply {
                text = app.name
                textSize = 18f
                setPadding(24, 0, 0, 0)
            }

            row.addView(iconView)
            row.addView(nameView)

            row.setOnClickListener {

                val launchIntent =
                    packageManager.getLaunchIntentForPackage(app.packageName)

                if (launchIntent != null) {
                    startActivity(launchIntent)
                } else {
                    Toast.makeText(
                        this,
                        "No se pudo abrir ${app.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            container.addView(row)
        }

        scrollView.addView(container)

        setContentView(scrollView)
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
            .sortedBy { it.name.lowercase() }
    }
}