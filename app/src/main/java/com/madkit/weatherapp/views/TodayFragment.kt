package com.madkit.weatherapp.views

import java.time.LocalDateTime

class TodayFragment :  BaseWeatherFragment()  {

    override fun getTargetDate(): Int {
        return 0
    }

    override fun getLocaleDate(): LocalDateTime {
        return LocalDateTime.now()
    }

}