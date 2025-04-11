package com.example.weatherapp.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class LoadingDotsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val dotCount = 3
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val animators = mutableListOf<ValueAnimator>()
    private val scales = FloatArray(dotCount) { 1f }

    private val dotColor = Color.WHITE
    private val dotRadius = 20f
    private val dotSpacing = 50f
    private val animationDuration = 500L

    init {
        paint.color = dotColor
        startAnimations()
    }

    private fun startAnimations() {
        for (i in 0 until dotCount) {
            val animator = ValueAnimator.ofFloat(0.3f, 1f).apply {
                duration = animationDuration
                repeatMode = ValueAnimator.REVERSE
                repeatCount = ValueAnimator.INFINITE
                startDelay = i * 150L
                addUpdateListener {
                    scales[i] = it.animatedValue as Float
                    invalidate()
                }
            }
            animator.start()
            animators.add(animator)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerY = height / 2f
        val startX = (width - (dotCount - 1) * dotSpacing) / 2f
        for (i in 0 until dotCount) {
            val x = startX + i * dotSpacing
            canvas.drawCircle(x, centerY, dotRadius * scales[i], paint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animators.forEach { it.cancel() }
    }
}
