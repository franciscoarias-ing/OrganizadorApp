package com.example.organizadorapps
import android.text.Editable
import android.text.TextWatcher
import android.R.attr.singleLine
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

object AppUiUtils {

    fun dp(context: Context, value: Int): Int {
        return (value * context.resources.displayMetrics.density).toInt()
    }

    fun title(context: Context, textValue: String): TextView {
        return TextView(context).apply {
            text = textValue
            textSize = 34f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(UiConstants.TEXT_PRIMARY)
            includeFontPadding = false
            setPadding(0, dp(context, 8), 0, 0)
        }
    }

    fun smallGreeting(context: Context, textValue: String): TextView {
        return TextView(context).apply {
            text = textValue
            textSize = 16f
            setTextColor(UiConstants.TEXT_PRIMARY)
            includeFontPadding = false
        }
    }
    fun subtitleColor(): Int {
        return UiConstants.TEXT_SECONDARY
    }
    fun subtitle(context: Context, textValue: String): TextView {
        return TextView(context).apply {
            text = textValue
            textSize = 16f
            setTextColor(UiConstants.TEXT_SECONDARY)
            includeFontPadding = false
            setPadding(0, dp(context, 12), 0, dp(context, 24))
        }
    }
    fun kicker(context: Context, textValue: String): TextView {
        return TextView(context).apply {
            text = textValue
            textSize = 14f
            setTextColor(UiConstants.ACCENT)
            includeFontPadding = false
            setPadding(0, dp(context, 6), 0, dp(context, 8))
        }
    }

    fun searchBox(
        context: Context,
        hintValue: String,
        onTextChanged: (String) -> Unit
    ): EditText {
        return EditText(context).apply {
            hint = hintValue
            textSize = 16f
            setTextColor(UiConstants.TEXT_PRIMARY)
            setHintTextColor(UiConstants.TEXT_MUTED)
            isSingleLine = true
            background = glassCard()
            setPadding(
                dp(context, 20),
                0,
                dp(context, 20),
                0
            )

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(context, 58)
            ).apply {
                setMargins(0, 0, 0, dp(context, 18))
            }

            addTextChangedListenerCompat {
                onTextChanged(it)
            }
        }
    }

    fun EditText.addTextChangedListenerCompat(onChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onChanged(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun glassCard(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(UiConstants.SURFACE)
            cornerRadius = 32f
            setStroke(1, UiConstants.BORDER)
        }
    }

    fun roundedCard(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(UiConstants.SURFACE)
            cornerRadius = 28f
            setStroke(1, UiConstants.BORDER)
        }
    }

    fun favoriteCard(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(UiConstants.SURFACE_ALT)
            cornerRadius = 28f
            setStroke(2, UiConstants.GOLD)
        }
    }



    fun searchButton(
        context: Context,
        hint: String,
        onClick: () -> Unit
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(context, 22), 0, dp(context, 22), 0)
            background = glassCard()
            isClickable = true
            isFocusable = true

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(context, 64)
            ).apply {
                setMargins(0, 0, 0, dp(context, 18))
            }

            setOnClickListener {
                AnimationUtils.press(this) { onClick() }
            }

            addView(TextView(context).apply {
                text = "⌕"
                textSize = 32f
                setTextColor(UiConstants.ACCENT)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(dp(context, 48), LinearLayout.LayoutParams.WRAP_CONTENT)
            })

            addView(TextView(context).apply {
                text = hint
                textSize = 18f
                setTextColor(UiConstants.TEXT_MUTED)
                includeFontPadding = false
            })
        }
    }

    fun quickAction(
        context: Context,
        icon: String,
        label: String,
        onClick: () -> Unit
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )

            setOnClickListener {
                AnimationUtils.press(this) { onClick() }
            }

            addView(TextView(context).apply {
                text = icon
                textSize = 28f
                setTextColor(UiConstants.ACCENT)
                gravity = Gravity.CENTER
                background = GradientDrawable().apply {
                    setColor(UiConstants.ACCENT_SOFT)
                    cornerRadius = dp(context, 24).toFloat()
                }
                layoutParams = LinearLayout.LayoutParams(dp(context, 52), dp(context, 52))
            })

            addView(TextView(context).apply {
                text = label
                textSize = 13f
                setTextColor(UiConstants.TEXT_PRIMARY)
                gravity = Gravity.CENTER
                includeFontPadding = false
                maxLines = 2
                setPadding(0, dp(context, 10), 0, 0)
            })
        }
    }

    fun sectionRow(
        context: Context,
        title: String,
        actionText: String,
        onActionClick: () -> Unit
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(context, 18), 0, dp(context, 12))
            }

            addView(TextView(context).apply {
                text = title
                textSize = 21f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(UiConstants.TEXT_PRIMARY)
                includeFontPadding = false
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })

            addView(TextView(context).apply {
                text = actionText
                textSize = 15f
                setTextColor(UiConstants.ACCENT)
                includeFontPadding = false
                setPadding(dp(context, 16), dp(context, 8), 0, dp(context, 8))
                setOnClickListener { onActionClick() }
            })
        }
    }

    fun miniEmpty(context: Context, textValue: String): TextView {
        return TextView(context).apply {
            text = textValue
            textSize = 14f
            setTextColor(UiConstants.TEXT_SECONDARY)
            setPadding(0, dp(context, 8), 0, dp(context, 18))
        }
    }


    fun verticalDivider(context: Context): View {
        return View(context).apply {
            setBackgroundColor(UiConstants.BORDER)
            layoutParams = LinearLayout.LayoutParams(dp(context, 1), dp(context, 56)).apply {
                setMargins(dp(context, 8), 0, dp(context, 8), 0)
            }
        }
    }
}