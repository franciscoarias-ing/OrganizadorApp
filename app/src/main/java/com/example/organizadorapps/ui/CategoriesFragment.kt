package com.example.organizadorapps.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadorapps.AppCategory
import com.example.organizadorapps.CategoryAdapter
import com.example.organizadorapps.CategoryRules
import com.example.organizadorapps.InstalledApp

class CategoriesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var allApps: List<InstalledApp>
    private lateinit var rootLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        allApps = getInstalledLaunchableApps()

        rootLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0F1115"))
            setPadding(24, 42, 24, 24)
        }

        rootLayout.addView(TextView(requireContext()).apply {
            text = "Categorías"
            textSize = 30f
            setTextColor(Color.WHITE)
        })

        rootLayout.addView(TextView(requireContext()).apply {
            text = "${allApps.size} apps detectadas"
            textSize = 14f
            setTextColor(Color.parseColor("#8F96A3"))
            setPadding(0, 8, 0, 24)
        })

        rootLayout.addView(searchBox())
        recyclerView = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(requireContext())
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        rootLayout.addView(recyclerView)

        renderCategories(allApps)

        return rootLayout
    }

    private fun searchBox(): EditText {

        return EditText(requireContext()).apply {

            hint = "Buscar apps..."
            textSize = 15f

            setHintTextColor(Color.parseColor("#777E8C"))
            setTextColor(Color.WHITE)

            setSingleLine(true)

            setPadding(28, 0, 28, 0)

            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.parseColor("#1A1F2B"))
                cornerRadius = 24f
                setStroke(1, Color.parseColor("#2B3140"))
            }

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                96
            ).apply {
                setMargins(0, 0, 0, 24)
            }

            addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    val query = s.toString().trim().lowercase()

                    val filteredApps = if (query.isEmpty()) {
                        allApps
                    } else {
                        allApps.filter {
                            it.name.lowercase().contains(query) ||
                                    it.packageName.lowercase().contains(query)
                        }
                    }

                    renderCategories(filteredApps)
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun renderCategories(apps: List<InstalledApp>) {

        val categories = buildCategories(apps)

        recyclerView.adapter = CategoryAdapter(
            categories.filter { it.apps.isNotEmpty() }
        )
    }

    private fun buildCategories(apps: List<InstalledApp>): List<AppCategory> {

        val dynamicCategories = CategoryRules.rules.map { rule ->

            AppCategory(
                rule.key,

                apps.filter { app ->

                    val searchable =
                        "${app.name} ${app.packageName}".lowercase()

                    rule.value.any { keyword ->
                        searchable.contains(keyword)
                    }
                }
            )
        }

        return dynamicCategories + AppCategory(
            "Todas las apps",
            apps
        )
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