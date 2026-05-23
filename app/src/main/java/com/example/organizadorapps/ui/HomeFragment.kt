package com.example.organizadorapps.ui

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return TextView(requireContext()).apply {
            text = "Inicio\nPróximamente hub inteligente"
            textSize = 22f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(40, 80, 40, 40)
        }
    }
}