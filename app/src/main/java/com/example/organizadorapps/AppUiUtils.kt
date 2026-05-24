package com.example.organizadorapps

import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.content.res.ColorStateList
import android.text.TextUtils
import android.util.TypedValue
import android.widget.ImageView
import androidx.core.widget.TextViewCompat
object AppUiUtils {

    fun dp(context: Context, value: Int): Int {
        return (value * context.resources.displayMetrics.density).toInt()
    }

    fun title(context: Context, textValue: String): TextView {
        return TextView(context).apply {
            text = textValue
            textSize = 32f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(UiConstants.TEXT_PRIMARY)
            includeFontPadding = false
            setPadding(0, dp(context, 6), 0, dp(context, 12))
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
            setPadding(0, dp(context, 10), 0, dp(context, 22))
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

    fun searchButton(
        context: Context,
        hint: String,
        onClick: () -> Unit
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(context, 18), 0, dp(context, 18), 0)
            background = glassCard()
            isClickable = true
            isFocusable = true

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(context, 52)
            ).apply {
                setMargins(0, 0, 0, dp(context, 18))
            }

            setOnClickListener {
                AnimationUtils.press(this) { onClick() }
            }

            addView(TextView(context).apply {
                text = "⌕"
                textSize = 27f
                setTextColor(UiConstants.ACCENT)
                gravity = Gravity.CENTER
                includeFontPadding = false
                layoutParams = LinearLayout.LayoutParams(
                    dp(context, 44),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            })

            addView(TextView(context).apply {
                text = hint
                textSize = 17f
                setTextColor(UiConstants.TEXT_MUTED)
                includeFontPadding = false
            })
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
            setPadding(dp(context, 20), 0, dp(context, 20), 0)

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
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                onChanged(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun quickAction(
        context: Context,
        iconRes: Int,
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
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )

            setOnClickListener {
                AnimationUtils.press(this) { onClick() }
            }

            addView(ImageView(context).apply {
                setImageResource(iconRes)
                imageTintList = ColorStateList.valueOf(UiConstants.ACCENT)
                background = circleAccentSoft(context)
                scaleType = ImageView.ScaleType.CENTER
                adjustViewBounds = false

                setPadding(
                    quickActionIconInnerPadding(context),
                    quickActionIconInnerPadding(context),
                    quickActionIconInnerPadding(context),
                    quickActionIconInnerPadding(context)
                )

                layoutParams = LinearLayout.LayoutParams(
                    quickActionIconSize(context),
                    quickActionIconSize(context)
                )
            })

            addView(TextView(context).apply {
                text = label
                gravity = Gravity.CENTER
                setTextColor(UiConstants.TEXT_PRIMARY)
                includeFontPadding = false
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END

                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                    this,
                    9,
                    12,
                    1,
                    TypedValue.COMPLEX_UNIT_SP
                )

                setPadding(0, quickActionTextTopPadding(context), 0, 0)

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            })
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
            gravity = Gravity.CENTER_VERTICAL
            background = roundedCard()
            isClickable = true
            isFocusable = true

            setPadding(
                dp(context, 18),
                dp(context, 16),
                dp(context, 18),
                dp(context, 16)
            )

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(context, 10), 0, dp(context, 10))
            }

            setOnClickListener {
                AnimationUtils.press(this) { onClick() }
            }

            addView(TextView(context).apply {
                text = icon
                textSize = 28f
                setTextColor(UiConstants.ACCENT)
                gravity = Gravity.CENTER
                background = circleAccentSoft(context)
                layoutParams = LinearLayout.LayoutParams(
                    dp(context, 52),
                    dp(context, 52)
                )
            })

            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(context, 14), 0, 0, 0)

                addView(TextView(context).apply {
                    text = title
                    textSize = 16f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(UiConstants.TEXT_PRIMARY)
                    includeFontPadding = false
                })

                addView(TextView(context).apply {
                    text = subtitle
                    textSize = 13f
                    setTextColor(UiConstants.TEXT_SECONDARY)
                    includeFontPadding = false
                    setPadding(0, dp(context, 6), 0, 0)
                })
            })
        }
    }

    fun sectionRow(
        context: Context,
        title: String,
        actionText: String,
        topMarginDp: Int = 18,
        bottomMarginDp: Int = 12,
        onActionClick: () -> Unit
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(context, topMarginDp), 0, dp(context, bottomMarginDp))
            }

            addView(TextView(context).apply {
                text = title
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(UiConstants.TEXT_PRIMARY)
                includeFontPadding = false
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
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

            layoutParams = LinearLayout.LayoutParams(
                dp(context, 1),
                quickActionDividerHeight(context)
            ).apply {
                setMargins(
                    quickActionDividerMargin(context),
                    0,
                    quickActionDividerMargin(context),
                    0
                )
            }
        }
    }
    fun quickActionsHorizontalPadding(context: Context): Int {
        return (context.resources.displayMetrics.widthPixels * 0.025f).toInt()
            .coerceIn(dp(context, 8), dp(context, 14))
    }

    fun quickActionsVerticalPadding(context: Context): Int {
        return (quickActionIconSize(context) * 0.20f).toInt()
            .coerceIn(dp(context, 9), dp(context, 13))
    }

    fun quickActionsMinHeight(context: Context): Int {
        return quickActionIconSize(context) +
                quickActionTextTopPadding(context) +
                dp(context, 20)
    }

    private fun quickActionIconSize(context: Context): Int {
        return (context.resources.displayMetrics.widthPixels * 0.122f).toInt()
            .coerceIn(dp(context, 45), dp(context, 52))
    }

    private fun quickActionIconInnerPadding(context: Context): Int {
        return (quickActionIconSize(context) * 0.24f).toInt()
    }

    private fun quickActionTextTopPadding(context: Context): Int {
        return (quickActionIconSize(context) * 0.13f).toInt()
            .coerceIn(dp(context, 6), dp(context, 9))
    }

    private fun quickActionDividerHeight(context: Context): Int {
        return (quickActionIconSize(context) * 1.12f).toInt()
    }

    private fun quickActionDividerMargin(context: Context): Int {
        return (context.resources.displayMetrics.widthPixels * 0.004f).toInt()
            .coerceIn(dp(context, 1), dp(context, 3))
    }

    fun glassCard(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(UiConstants.SURFACE)
            cornerRadius = 30f
            setStroke(1, UiConstants.BORDER)
        }
    }

    fun roundedCard(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(UiConstants.SURFACE)
            cornerRadius = 32f
            setStroke(1, UiConstants.BORDER)
        }
    }

    fun roundedCardAlt(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(UiConstants.SURFACE_ALT)
            cornerRadius = 28f
            setStroke(1, UiConstants.BORDER)
        }
    }

    fun favoriteCard(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(UiConstants.SURFACE_ALT)
            cornerRadius = 28f
            setStroke(1, UiConstants.BORDER)
        }
    }

    fun appIconTile(context: Context): GradientDrawable {
        return GradientDrawable().apply {
            setColor(UiConstants.SURFACE_ALT)
            cornerRadius = dp(context, 18).toFloat()
            setStroke(1, UiConstants.BORDER)
        }
    }

    fun addFavoriteCard(context: Context): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.TRANSPARENT)
            cornerRadius = dp(context, 18).toFloat()
            setStroke(
                dp(context, 1),
                UiConstants.ACCENT,
                dp(context, 8).toFloat(),
                dp(context, 6).toFloat()
            )
        }
    }

    fun circleAccentSoft(context: Context): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(UiConstants.ACCENT_SOFT)
        }
    }
}