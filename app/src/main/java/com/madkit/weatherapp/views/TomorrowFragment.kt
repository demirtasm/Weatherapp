package com.madkit.weatherapp.views

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