package com.madkit.weatherapp.views.customViews.customTextViewFont

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.madkit.weatherapp.R

class InterTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    init {
        typeface = ResourcesCompat.getFont(context, R.font.inter)
    }
}