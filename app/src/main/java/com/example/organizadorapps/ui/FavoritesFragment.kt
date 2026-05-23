package com.example.organizadorapps.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadorapps.AppAdapter
import com.example.organizadorapps.AppRepository
import com.example.organizadorapps.FavoritesManager

class FavoritesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val allApps = AppRepository.getInstalledLaunchableApps(requireContext())
        val favoritePackages = FavoritesManager.getFavoritePackages(requireContext())
        val favoriteApps = allApps.filter { favoritePackages.contains(it.packageName) }

        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0F1115"))
            setPadding(24, 42, 24, 24)
        }

        root.addView(TextView(requireContext()).apply {
            text = "Favoritos"
            textSize = 30f
            setTextColor(Color.WHITE)
        })

        root.addView(TextView(requireContext()).apply {
            text = if (favoriteApps.isEmpty()) {
                "Accesos rápidos todavía vacíos"
            } else {
                "${favoriteApps.size} apps favoritas"
            }
            textSize = 14f
            setTextColor(Color.parseColor("#8F96A3"))
            setPadding(0, 8, 0, 24)
        })

        if (favoriteApps.isEmpty()) {
            root.addView(emptyState())
        } else {
            root.addView(RecyclerView(requireContext()).apply {
                layoutManager = GridLayoutManager(requireContext(), 4)
                adapter = AppAdapter(favoriteApps)
                overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            })
        }

        return root
    }

    private fun emptyState(): LinearLayout {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 36, 32, 36)

            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1A1F2B"))
                cornerRadius = 30f
                setStroke(1, Color.parseColor("#2B3140"))
            }

            addView(TextView(requireContext()).apply {
                text = "★"
                textSize = 34f
                setTextColor(Color.parseColor("#D6A84F"))
            })

            addView(TextView(requireContext()).apply {
                text = "Todavía no tienes favoritos"
                textSize = 19f
                setTextColor(Color.WHITE)
                setPadding(0, 14, 0, 8)
            })

            addView(TextView(requireContext()).apply {
                text = "Mantén presionada una app en Categorías o Todas las apps para guardarla aquí."
                textSize = 14f
                setTextColor(Color.parseColor("#8F96A3"))
            })
        }
    }
}