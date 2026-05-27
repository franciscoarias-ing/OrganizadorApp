package com.example.organizadorapps

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CategoryWidgetConfigureActivity : AppCompatActivity() {

    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        buildUi()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#070B16"))
            setPadding(dp(18), dp(18), dp(18), dp(18))
        }

        root.addView(TextView(this).apply {
            text = "Elige una categoría"
            setTextColor(Color.parseColor("#F8FAFC"))
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
        })

        root.addView(TextView(this).apply {
            text = "Este widget mostrará solo apps de la categoría seleccionada. Puedes agregar más widgets para otras categorías."
            setTextColor(Color.parseColor("#CBD5E1"))
            textSize = 13f
            setPadding(0, dp(6), 0, dp(16))
        })

        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        CategoryWidgetPrefs.getAvailableCategories().forEach { categoryName ->
            list.addView(categoryButton(categoryName))
        }

        root.addView(ScrollView(this).apply {
            addView(list)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        })

        setContentView(root)
    }

    private fun categoryButton(categoryName: String): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            isClickable = true
            isFocusable = true
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(18).toFloat()
                setColor(Color.parseColor("#121A2E"))
                setStroke(dp(1), Color.parseColor("#334155"))
            }
            setPadding(dp(16), dp(14), dp(16), dp(14))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(10)
            }

            addView(TextView(this@CategoryWidgetConfigureActivity).apply {
                text = categoryName
                setTextColor(Color.parseColor("#F8FAFC"))
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
            })

            addView(TextView(this@CategoryWidgetConfigureActivity).apply {
                text = "Crear widget de $categoryName"
                setTextColor(Color.parseColor("#94A3B8"))
                textSize = 12f
                setPadding(0, dp(3), 0, 0)
            })

            setOnClickListener {
                selectCategory(categoryName)
            }
        }
    }

    private fun selectCategory(categoryName: String) {
        CategoryWidgetPrefs.saveCategory(this, appWidgetId, categoryName)

        val manager = AppWidgetManager.getInstance(this)
        FixedCategoriesWidgetProvider.updateWidget(this, manager, appWidgetId)

        val resultIntent = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
