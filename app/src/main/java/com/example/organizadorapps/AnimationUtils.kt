package com.example.organizadorapps

import android.view.View

object AnimationUtils {

    fun press(view: View, endAction: (() -> Unit)? = null) {
        view.animate()
            .scaleX(0.96f)
            .scaleY(0.96f)
            .alpha(0.92f)
            .setDuration(70)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(90)
                    .withEndAction {
                        endAction?.invoke()
                    }
                    .start()
            }
            .start()
    }

    fun pop(view: View) {
        view.animate()
            .scaleX(1.04f)
            .scaleY(1.04f)
            .setDuration(90)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(90)
                    .start()
            }
            .start()
    }

    fun fadeIn(view: View, delay: Long = 0L) {
        view.alpha = 0f
        view.translationY = 18f

        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(delay)
            .setDuration(220)
            .start()
    }
}