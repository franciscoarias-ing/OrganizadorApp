package com.example.organizadorapps.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
            text = "Hub inteligente en construcción"
            textSize = 14f
            setTextColor(Color.parseColor("#8F96A3"))
            setPadding(0, 8, 0, 32)
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
                parentFragmentManager.beginTransaction()
                    .replace((requireView().parent as ViewGroup).id, AllAppsFragment())
                    .addToBackStack(null)
                    .commit()
            }
        })

        return root
    }
}