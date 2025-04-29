package com.madkit.weatherapp.views.fragments

import com.madkit.weatherapp.views.BaseWeatherFragment
import java.time.LocalDateTime

class TodayFragment :  BaseWeatherFragment()  {

    override fun getTargetDate(): Int {
        return 0
    }

    override fun getLocaleDate(): LocalDateTime {
        return LocalDateTime.now()
    }

}