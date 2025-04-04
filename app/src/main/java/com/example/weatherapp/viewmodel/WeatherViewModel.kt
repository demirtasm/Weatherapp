package com.example.weatherapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WeatherViewModel: ViewModel() {
    private val _weatherCode = MutableLiveData<String>()
    val weatherCode: LiveData<String> = _weatherCode

    fun setWeatherCode(code: String) {
        _weatherCode.value = code
    }
}