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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadorapps.AppAdapter
import com.example.organizadorapps.AppRepository
import com.example.organizadorapps.FavoritesManager

class HomeFragment : Fragment() {

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
            text = "Inicio"
            textSize = 30f
            setTextColor(Color.WHITE)
        })

        root.addView(TextView(requireContext()).apply {
            text = "Tu hub inteligente de acceso rápido"
            textSize = 14f
            setTextColor(Color.parseColor("#8F96A3"))
            setPadding(0, 8, 0, 28)
        })

        root.addView(TextView(requireContext()).apply {
            text = "Ver todas las apps"
            textSize = 18f
            setTextColor(Color.WHITE)
            setPadding(32, 28, 32, 28)

            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1A1F2B"))
                cornerRadius = 30f
                setStroke(1, Color.parseColor("#2B3140"))
            }

            setOnClickListener {
                val containerId = (view?.parent as? ViewGroup)?.id ?: return@setOnClickListener

                parentFragmentManager.beginTransaction()
                    .replace(containerId, AllAppsFragment())
                    .addToBackStack(null)
                    .commit()
            }
        })

        root.addView(sectionTitle("Favoritos rápidos"))

        if (favoriteApps.isEmpty()) {
            root.addView(helperText("Mantén presionada una app en Categorías para agregarla aquí."))
        } else {
            root.addView(RecyclerView(requireContext()).apply {
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                adapter = AppAdapter(favoriteApps.take(10))
                overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    235
                )
            })
        }

        root.addView(sectionTitle("Próximamente"))

        root.addView(helperText("Recientes, más usadas y sugerencias contextuales."))

        return root
    }

    private fun sectionTitle(textValue: String): TextView {
        return TextView(requireContext()).apply {
            text = textValue
            textSize = 18f
            setTextColor(Color.WHITE)
            setPadding(0, 30, 0, 14)
        }
    }

    private fun helperText(textValue: String): TextView {
        return TextView(requireContext()).apply {
            text = textValue
            textSize = 14f
            setTextColor(Color.parseColor("#8F96A3"))
            setPadding(0, 4, 0, 12)
        }
    }
}