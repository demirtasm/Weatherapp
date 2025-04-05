package com.example.weatherapp.views

import android.content.Context
import android.widget.TextView
import com.example.weatherapp.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class LineChartMarkerView(
    context: Context,
    layoutResource: Int,
    private val labels: List<String>,
    private val windDirections: List<Double>
) : MarkerView(context, layoutResource) {
    private val tvLabel: TextView = findViewById(R.id.tvLabel)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let {
            val hour = e.x.toInt()
            val hourStr = String.format("%02d:00", hour)
            val index = labels.indexOfFirst { it == hourStr }

            if (index != -1) {
                val time = labels[index]
                val value = e.y.toInt().toString()
                val degree = windDirections.getOrNull(index)?.toInt() ?: -1
                val direction = getWindDirectionName(degree)

                tvLabel.text = "$direction yönü rüzgar \n$value km/h"
            } else {
                tvLabel.text = "Bilinmiyor\nN/A"
            }
        }
        super.refreshContent(e, highlight)
    }
    fun getWindDirectionName(degree: Int): String {
        return when (degree) {
            in 0..11, in 349..360 -> "Kuzey"
            in 12..33 -> "Kuzey-Kuzeydoğu"
            in 34..56 -> "Kuzeydoğu"
            in 57..78 -> "Doğu-Kuzeydoğu"
            in 79..101 -> "Doğu"
            in 102..123 -> "Doğu-Güneydoğu"
            in 124..146 -> "Güneydoğu"
            in 147..168 -> "Güney-Güneydoğu"
            in 169..191 -> "Güney"
            in 192..213 -> "Güney-Güneybatı"
            in 214..236 -> "Güneybatı"
            in 237..258 -> "Batı-Güneybatı"
            in 259..281 -> "Batı"
            in 282..303 -> "Batı-Kuzeybatı"
            in 304..326 -> "Kuzeybatı"
            in 327..348 -> "Kuzey-Kuzeybatı"
            else -> "Bilinmiyor"
        }
    }


    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2).toFloat(), -height.toFloat())
    }
}