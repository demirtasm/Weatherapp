package com.madkit.weatherapp.views.fragments

import com.madkit.weatherapp.views.BaseWeatherFragment
import java.time.LocalDateTime


class TomorrowFragment  :  BaseWeatherFragment()  {
    override fun getTargetDate(): Int {
        return 1
    }

    override fun getLocaleDate(): LocalDateTime {
        return LocalDateTime.now().plusDays(1)
    }

    override fun getShouldHighlightHour(): Boolean {
        return false
    }

    override fun getShouldScrollToHour(): Boolean {
        return false
    }

}