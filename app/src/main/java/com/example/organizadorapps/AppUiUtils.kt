package com.example.organizadorapps

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.content.Context

object AppUiUtils {

    val backgroundColor = Color.parseColor("#0F1115")
    val cardColor = Color.parseColor("#1A1F2B")
    val borderColor = Color.parseColor("#2B3140")
    val subtitleColor = Color.parseColor("#8F96A3")
    val textColor = Color.WHITE

    fun title(context: Context, textValue: String): TextView {
        return TextView(context).apply {
            text = textValue
            textSize = 30f
            setTextColor(textColor)
        }
    }

    fun subtitle(context: Context, textValue: String): TextView {
        return TextView(context).apply {
            text = textValue
            textSize = 14f
            setTextColor(subtitleColor)
            setPadding(0, 8, 0, 24)
        }
    }

    fun searchBox(context: Context, hintText: String): EditText {
        return EditText(context).apply {
            hint = hintText
            textSize = 15f
            setHintTextColor(subtitleColor)
            setTextColor(textColor)
            setSingleLine(true)
            setPadding(28, 0, 28, 0)

            background = GradientDrawable().apply {
                setColor(cardColor)
                cornerRadius = 24f
                setStroke(1, borderColor)
            }

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                96
            ).apply {
                setMargins(0, 0, 0, 24)
            }
        }
    }

    fun roundedCard(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(cardColor)
            cornerRadius = 28f
            setStroke(1, borderColor)
        }
    }
}