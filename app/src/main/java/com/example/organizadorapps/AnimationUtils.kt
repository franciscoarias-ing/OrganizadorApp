package com.example.organizadorapps

import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator

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

    fun expandHeightFadeIn(
        view: View,
        duration: Long = UiConstants.PANEL_OPEN_DURATION_MS,
        endAction: (() -> Unit)? = null
    ) {
        val parentWidth = (view.parent as? View)?.width ?: 0

        view.measure(
            View.MeasureSpec.makeMeasureSpec(parentWidth, View.MeasureSpec.AT_MOST),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        val targetHeight = view.measuredHeight.coerceAtLeast(1)

        view.layoutParams = view.layoutParams.apply {
            height = 0
        }

        view.alpha = 0f
        view.translationY = -18f
        view.visibility = View.VISIBLE

        ValueAnimator.ofInt(0, targetHeight).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                view.layoutParams = view.layoutParams.apply {
                    height = animator.animatedValue as Int
                }
                view.requestLayout()
            }
            start()
        }

        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(duration)
            .withEndAction {
                view.layoutParams = view.layoutParams.apply {
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                }
                view.requestLayout()
                endAction?.invoke()
            }
            .start()
    }

    fun collapseHeightFadeOut(
        view: View,
        duration: Long = UiConstants.PANEL_CLOSE_DURATION_MS,
        endAction: (() -> Unit)? = null
    ) {
        val initialHeight = if (view.height > 0) {
            view.height
        } else {
            val parentWidth = (view.parent as? View)?.width ?: 0

            view.measure(
                View.MeasureSpec.makeMeasureSpec(parentWidth, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )

            view.measuredHeight.coerceAtLeast(1)
        }

        ValueAnimator.ofInt(initialHeight, 0).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                view.layoutParams = view.layoutParams.apply {
                    height = animator.animatedValue as Int
                }
                view.requestLayout()
            }
            start()
        }

        view.animate()
            .alpha(0f)
            .translationY(-14f)
            .setDuration(duration)
            .withEndAction {
                endAction?.invoke()
            }
            .start()
    }
}