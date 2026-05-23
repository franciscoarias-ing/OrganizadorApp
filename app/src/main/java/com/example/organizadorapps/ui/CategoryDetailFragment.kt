package com.example.organizadorapps.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadorapps.AppAdapter
import com.example.organizadorapps.AppUiUtils
import com.example.organizadorapps.InstalledApp

class CategoryDetailFragment(
    private val categoryName: String,
    private val apps: List<InstalledApp>
) : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private var filteredApps: List<InstalledApp> = apps

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(com.example.organizadorapps.UiConstants.BACKGROUND)
            setPadding(24, 64, 24, 24)
        }

        root.addView(AppUiUtils.title(requireContext(), categoryName))
        root.addView(AppUiUtils.subtitle(requireContext(), "${apps.size} apps disponibles"))

        root.addView(searchBox())

        recyclerView = RecyclerView(requireContext()).apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = AppAdapter(filteredApps)
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        root.addView(
            recyclerView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        return root
    }

    private fun searchBox(): EditText {
        return AppUiUtils.searchBox(
            requireContext(),
            "Buscar en $categoryName..."
        ).apply {
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

                    filteredApps = if (query.isEmpty()) {
                        apps
                    } else {
                        apps.filter {
                            it.name.lowercase().contains(query) ||
                                    it.packageName.lowercase().contains(query)
                        }
                    }

                    recyclerView.adapter = AppAdapter(filteredApps)
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }
}