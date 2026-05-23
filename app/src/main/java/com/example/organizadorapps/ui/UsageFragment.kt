package com.example.organizadorapps.ui

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment

class UsageFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return TextView(requireContext()).apply {
            text = "Uso\nPróximamente hábitos y estadísticas"
            textSize = 22f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(40, 80, 40, 40)
        }
    }
}