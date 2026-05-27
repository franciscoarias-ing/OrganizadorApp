package com.example.organizadorapps

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout

object QuickActionsBar {

    fun build(context: Context, compact: Boolean = true): LinearLayout {
        val size = if (compact) 34 else 40
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(button(context, R.drawable.ic_wifi, "Abrir Wi‑Fi", size) {
                QuickSettingsNavigator.openWifi(context)
            })
            addView(button(context, R.drawable.ic_bluetooth, "Abrir Bluetooth", size) {
                QuickSettingsNavigator.openBluetooth(context)
            })
            addView(button(context, R.drawable.ic_mobile_data, "Abrir datos móviles", size) {
                QuickSettingsNavigator.openMobileData(context)
            })
            addView(button(context, R.drawable.ic_camera, "Abrir cámara", size) {
                QuickSettingsNavigator.openCamera(context)
            })
            addView(button(context, R.drawable.ic_flashlight, "Cambiar linterna", size) {
                QuickSettingsNavigator.toggleFlashlight(context)
            })
            addView(button(context, R.drawable.ic_spotify, "Abrir Spotify", size) {
                QuickSettingsNavigator.openSpotify(context)
            })
            addView(button(context, R.drawable.ic_youtube, "Abrir YouTube", size) {
                QuickSettingsNavigator.openYouTube(context)
            })
        }
    }

    private fun button(context: Context, iconRes: Int, description: String, sizeDp: Int, onClick: () -> Unit): FrameLayout {
        return FrameLayout(context).apply {
            isClickable = true
            isFocusable = true
            contentDescription = description
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = AppUiUtils.dp(context, sizeDp / 2).toFloat()
                setOrientation(GradientDrawable.Orientation.TL_BR)
                setColors(intArrayOf(
                    Color.argb(210, 30, 41, 68),
                    Color.argb(190, 24, 18, 43)
                ))
                setStroke(AppUiUtils.dp(context, 1), Color.argb(120, 168, 85, 247))
            }
            setOnClickListener { AnimationUtils.press(this) { onClick() } }
            layoutParams = LinearLayout.LayoutParams(
                AppUiUtils.dp(context, sizeDp),
                AppUiUtils.dp(context, sizeDp)
            ).apply { leftMargin = AppUiUtils.dp(context, 6) }

            addView(ImageView(context).apply {
                setImageResource(iconRes)
                setColorFilter(UiConstants.ACCENT)
                layoutParams = FrameLayout.LayoutParams(
                    AppUiUtils.dp(context, 18),
                    AppUiUtils.dp(context, 18),
                    Gravity.CENTER
                )
            })
        }
    }
}
