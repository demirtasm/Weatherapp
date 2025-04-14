package com.madkit.weatherapp.views

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
        strokeWidth = 12f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.LTGRAY
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 12f
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
        val desiredHeight = (width / 3) + 120  // genişliğin yarısı + alt metinler için alan
        setMeasuredDimension(width, desiredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        val w = width.toFloat()
        val h = height.toFloat()

        // Ayarlar
        val horizontalPadding = 40f
        val lineWidth = w - (horizontalPadding * 2)
        val lineY = h  * 0.75f// alt çizginin yüksekliği
        val cx = w / 2
        val cy = lineY

        // Yarım daire (daha kısa olacak çizgiye göre)
        val arcRadius = lineWidth / 3  // çizginin yarısı kadar (daha küçük yarım daire için)
        val arcRect = RectF(cx - arcRadius, cy - arcRadius, cx + arcRadius, cy + arcRadius)

        // Uzun ince alt çizgi
        val thinLinePaint = Paint(arcPaint).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = ContextCompat.getColor(context, R.color.purple)
        }

        // Çizgiyi çiz
        canvas.drawLine(horizontalPadding, lineY+8f, w - horizontalPadding, lineY+5f, thinLinePaint)

        // Arka yarım daire
        canvas.drawArc(arcRect, 180f, 180f, false, arcPaint)

        // Progress
        val progress = calculateProgress()
        canvas.drawArc(arcRect, 180f, 180f * progress, false, progressPaint)

        // Güneş pozisyonu
        val angle = Math.toRadians((180f + 180f * progress).toDouble())
        val sunX = (cx + arcRadius * Math.cos(angle)).toFloat()
        val sunY = (cy + arcRadius * Math.sin(angle)).toFloat()
        canvas.drawCircle(sunX, sunY, 18f, sunPaint)

        // Her zaman sol hizalama
        textPaint.textAlign = Paint.Align.LEFT

// Gün doğumu
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

// Gün batımı - metni sağa sabitlemek için metin genişliği kadar geri çek
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
