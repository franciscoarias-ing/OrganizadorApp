package com.example.organizadorapps.ui

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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadorapps.AppAdapter
import com.example.organizadorapps.AppRepository
import com.example.organizadorapps.InstalledApp

class AllAppsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var allApps: List<InstalledApp>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        allApps = AppRepository.getInstalledLaunchableApps(requireContext())

        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0F1115"))
            setPadding(24, 42, 24, 24)
        }

        root.addView(TextView(requireContext()).apply {
            text = "Todas las apps"
            textSize = 30f
            setTextColor(Color.WHITE)
        })

        root.addView(TextView(requireContext()).apply {
            text = "${allApps.size} apps detectadas"
            textSize = 14f
            setTextColor(Color.parseColor("#8F96A3"))
            setPadding(0, 8, 0, 24)
        })

        root.addView(searchBox())

        recyclerView = RecyclerView(requireContext()).apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = AppAdapter(allApps)
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

                    recyclerView.adapter = AppAdapter(filteredApps)
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }
}