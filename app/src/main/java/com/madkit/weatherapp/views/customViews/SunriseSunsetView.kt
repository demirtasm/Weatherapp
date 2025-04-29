package com.madkit.weatherapp.views.customViews

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.madkit.weatherapp.R
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SunriseSunsetView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    init {
        layoutDirection = LAYOUT_DIRECTION_LTR
    }
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 15f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.LTGRAY
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 15f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.YELLOW
    }

    private val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.YELLOW
        setShadowLayer(15f, 0f, 0f, Color.argb(100, 255, 215, 0))
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context,R.color.purple)
        textAlign = Paint.Align.CENTER
        textSize = 40f
    }

    private val formatterInput = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    private val formatterOutput = DateTimeFormatter.ofPattern("HH:mm")

    private var sunriseTime: LocalDateTime? = null
    private var sunsetTime: LocalDateTime? = null
    private var currentTime: LocalDateTime = LocalDateTime.now()

    fun setSunriseSunset(sunrise: String, sunset: String) {
        sunriseTime = LocalDateTime.parse(sunrise, formatterInput)
        sunsetTime = LocalDateTime.parse(sunset, formatterInput)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val desiredHeight = (width / 3) + 120
        setMeasuredDimension(width, desiredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        val w = width.toFloat()
        val h = height.toFloat()

        val horizontalPadding = 40f
        val lineWidth = w - (horizontalPadding * 2)
        val lineY = h  * 0.75f
        val cx = w / 2
        val cy = lineY

        val arcRadius = lineWidth / 3
        val arcRect = RectF(cx - arcRadius, cy - arcRadius, cx + arcRadius, cy + arcRadius)

        val thinLinePaint = Paint(arcPaint).apply {
            style = Paint.Style.STROKE
            strokeWidth = 8f
            color = ContextCompat.getColor(context, R.color.purple)
        }

        canvas.drawLine(horizontalPadding, lineY+8f, w - horizontalPadding, lineY+5f, thinLinePaint)
        canvas.drawArc(arcRect, 180f, 180f, false, arcPaint)

        val progress = calculateProgress()
        canvas.drawArc(arcRect, 180f, 180f * progress, false, progressPaint)

        val angle = Math.toRadians((180f + 180f * progress).toDouble())
        val sunX = (cx + arcRadius * Math.cos(angle)).toFloat()
        val sunY = (cy + arcRadius * Math.sin(angle)).toFloat()
        canvas.drawCircle(sunX, sunY, 18f, sunPaint)

        textPaint.textAlign = Paint.Align.LEFT

        sunriseTime?.let {
            canvas.drawText(
                context.getString(R.string.sunrise_txt),
                horizontalPadding,
                lineY + 50f,
                textPaint
            )
            canvas.drawText(
                formatterOutput.format(it),
                horizontalPadding,
                lineY + 95f,
                textPaint
            )
        }

        sunsetTime?.let {
            val sunsetTitle = context.getString(R.string.sunset_txt)
            val sunsetTimeStr = formatterOutput.format(it)

            val sunsetTitleWidth = textPaint.measureText(sunsetTitle)
            val sunsetTimeWidth = textPaint.measureText(sunsetTimeStr)

            canvas.drawText(
                sunsetTitle,
                w - horizontalPadding - sunsetTitleWidth,
                lineY + 50f,
                textPaint
            )
            canvas.drawText(
                sunsetTimeStr,
                w - horizontalPadding - sunsetTimeWidth,
                lineY + 95f,
                textPaint
            )
        }

    }



    private fun calculateProgress(): Float {
        if (sunriseTime == null || sunsetTime == null) return 0f

        val sunriseMillis = sunriseTime!!.atZone(ZoneId.systemDefault()).toEpochSecond()
        val sunsetMillis = sunsetTime!!.atZone(ZoneId.systemDefault()).toEpochSecond()
        val currentMillis = currentTime.atZone(ZoneId.systemDefault()).toEpochSecond()

        return when {
            currentMillis <= sunriseMillis -> 0f
            currentMillis >= sunsetMillis -> 1f
            else -> ((currentMillis - sunriseMillis).toFloat() / (sunsetMillis - sunriseMillis).toFloat())
        }
    }

    fun updateCurrentTime() {
        currentTime = LocalDateTime.now()
        invalidate()
    }
}
