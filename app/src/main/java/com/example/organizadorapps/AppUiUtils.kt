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
    fun smallGreeting(context: Context, textValue: String): TextView {
        return TextView(context).apply {
            text = textValue
            textSize = 16f
            setTextColor(UiConstants.TEXT_PRIMARY)
            setPadding(0, 0, 0, 10)
        }
    }
    fun favoriteCard(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(UiConstants.SURFACE_ALT)
            cornerRadius = UiConstants.SMALL_CARD_RADIUS
            setStroke(2, UiConstants.GOLD)
        }
    }

    fun glassCard(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(UiConstants.SURFACE)
            cornerRadius = 32f
            setStroke(1, UiConstants.BORDER)
        }
    }
    fun kicker(context: Context, textValue: String): TextView {
        return TextView(context).apply {
            text = textValue.uppercase()
            textSize = 12f
            letterSpacing = 0.12f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(UiConstants.ACCENT)
            setPadding(0, 0, 0, 8)
        }
    }

    fun searchButton(
        context: Context,
        hint: String,
        onClick: () -> Unit
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(24, 0, 24, 0)
            isClickable = true
            isFocusable = true
            background = glassCard()

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                88
            ).apply {
                setMargins(0, 0, 0, 22)
            }

            setOnClickListener {
                AnimationUtils.press(this) {
                    onClick()
                }
            }

            addView(TextView(context).apply {
                text = "⌕"
                textSize = 30f
                setTextColor(UiConstants.ACCENT)
                layoutParams = LinearLayout.LayoutParams(62, LinearLayout.LayoutParams.WRAP_CONTENT)
            })

            addView(TextView(context).apply {
                text = hint
                textSize = 18f
                setTextColor(UiConstants.TEXT_MUTED)
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
            gravity = android.view.Gravity.CENTER
            isClickable = true
            isFocusable = true

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )

            setOnClickListener {
                AnimationUtils.press(this) {
                    onClick()
                }
            }

            addView(TextView(context).apply {
                text = icon
                textSize = 27f
                setTextColor(UiConstants.ACCENT)
                gravity = android.view.Gravity.CENTER

                background = GradientDrawable().apply {
                    setColor(UiConstants.ACCENT_SOFT)
                    cornerRadius = 28f
                }

                layoutParams = LinearLayout.LayoutParams(56, 56)
            })

            addView(TextView(context).apply {
                text = label
                textSize = 12f
                setTextColor(UiConstants.TEXT_PRIMARY)
                gravity = android.view.Gravity.CENTER
                setPadding(0, 8, 0, 0)
                maxLines = 1
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
            gravity = android.view.Gravity.CENTER_VERTICAL

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 12)
            }

            addView(TextView(context).apply {
                text = title
                textSize = 19f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setTextColor(UiConstants.TEXT_PRIMARY)

                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            })

            addView(TextView(context).apply {
                text = actionText
                textSize = 14f
                setTextColor(UiConstants.ACCENT)
                setPadding(16, 8, 0, 8)
                setOnClickListener {
                    onActionClick()
                }
            })
        }
    }

    fun miniEmpty(context: Context, textValue: String): TextView {
        return TextView(context).apply {
            text = textValue
            textSize = 13f
            setTextColor(UiConstants.TEXT_SECONDARY)
            setPadding(0, 0, 0, 18)
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

    fun actionCard(
        context: Context,
        title: String,
        subtitle: String,
        icon: String,
        onClick: () -> Unit
    ): LinearLayout {

        return LinearLayout(context).apply {

            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL

            setPadding(24, 22, 24, 22)

            isClickable = true
            isFocusable = true

            background = glassCard()

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                120
            ).apply {
                setMargins(0, 0, 0, 16)
            }

            setOnClickListener {
                AnimationUtils.press(this) {
                    onClick()
                }
            }

            addView(TextView(context).apply {

                text = icon
                textSize = 28f

                gravity = android.view.Gravity.CENTER

                setTextColor(UiConstants.ACCENT)

                background = GradientDrawable().apply {
                    setColor(UiConstants.ACCENT_SOFT)
                    cornerRadius = 24f
                }

                layoutParams = LinearLayout.LayoutParams(
                    60,
                    60
                ).apply {
                    marginEnd = 18
                }
            })

            addView(LinearLayout(context).apply {

                orientation = LinearLayout.VERTICAL

                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )

                addView(TextView(context).apply {
                    text = title
                    textSize = 16f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    setTextColor(UiConstants.TEXT_PRIMARY)
                })

                addView(TextView(context).apply {
                    text = subtitle
                    textSize = 13f
                    setTextColor(UiConstants.TEXT_SECONDARY)
                    setPadding(0, 4, 0, 0)
                })
            })

            addView(TextView(context).apply {
                text = "›"
                textSize = 28f
                setTextColor(UiConstants.TEXT_MUTED)
            })
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