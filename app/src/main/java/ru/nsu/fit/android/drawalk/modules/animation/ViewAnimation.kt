package ru.nsu.fit.android.drawalk.modules.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View

class ViewAnimation {
    companion object {
        fun rotateFab(view: View, rotate: Boolean): Boolean {
            view.animate()
                .setDuration(200)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                    }
                })
            view.rotation = if (rotate) {
                135f
            } else {
                0f
            }
            return rotate
        }

        fun showIn(view: View) {
            view.apply {
                visibility = View.VISIBLE
                alpha = 0f
                translationY = height.toFloat()
                animate()
                    .setDuration(200)
                    .translationY(0f)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                        }
                    })
                    .alpha(1f)
                    .start()
            }
        }
        fun showOut(view: View) {
            view.apply {
                visibility = View.VISIBLE
                alpha = 1f
                translationY = 0f
                animate()
                    .setDuration(200)
                    .translationY(height.toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            view.visibility = View.GONE
                            super.onAnimationEnd(animation)
                        }
                    })
                    .alpha(0f)
                    .start()
            }
        }

        fun init (view: View) {
            view.apply {
                visibility = View.GONE
                alpha = 0f
                translationY = height.toFloat()
            }
        }
    }
}