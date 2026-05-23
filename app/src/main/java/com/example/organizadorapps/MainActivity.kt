package com.example.organizadorapps

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this).apply {
            text = "Organizador Apps"
            textSize = 24f
            setPadding(40, 80, 40, 40)
        }

        setContentView(textView)
    }
}