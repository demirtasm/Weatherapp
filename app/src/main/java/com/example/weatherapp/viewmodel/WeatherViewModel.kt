package com.example.weatherapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.models.AirPollutionResponse
import com.example.weatherapp.models.OpenMeteoResponse
import com.example.weatherapp.models.WeatherResponse
import com.example.weatherapp.repository.WeatherRepository
import kotlinx.coroutines.launch

class WeatherViewModel(private val repository: WeatherRepository): ViewModel() {

    val meteoData = MutableLiveData<OpenMeteoResponse>()
    val airPollutionData = MutableLiveData<AirPollutionResponse>()
    val currentWeather = MutableLiveData<WeatherResponse>()


    private val _weatherCode = MutableLiveData<String>()
    val weatherCode: LiveData<String> = _weatherCode

    private val _formattedDailyDate = MutableLiveData<String>()
    val formattedDailyDate:LiveData<String> = _formattedDailyDate

    private val _temperature = MutableLiveData<String>()
    val temperature: LiveData<String> = _temperature

    private val _temperature2mMax = MutableLiveData<String>()
    val temperature2mMax: LiveData<String> = _temperature2mMax

    private val _temperature2mMin = MutableLiveData<String>()
    val temperature2mMin: LiveData<String> = _temperature2mMin

    private val _oneWeekWeatherCode = MutableLiveData<List<String>>(emptyList())
    val oneWeekWeatherCode: LiveData<List<String>> = _oneWeekWeatherCode

    private val _isTargetOneWeek = MutableLiveData<Boolean>()
    val isTargetOneWeek: LiveData<Boolean> = _isTargetOneWeek


    private val _oneWeekTimes =  MutableLiveData<List<String>>(emptyList())
    val oneWeekTimes : LiveData<List<String>> = _oneWeekTimes

    private val _oneWeekMaxTemperature = MutableLiveData<List<Double>>()
    val oneWeekMaxTemperature : LiveData<List<Double>> = _oneWeekMaxTemperature

    private val _oneWeekMinTemperature = MutableLiveData<List<Double>>()
    val oneWeekMinTemperature : LiveData<List<Double>> = _oneWeekMinTemperature


    fun loadWeatherData(lat: Double, lon: Double){
        viewModelScope.launch {
            val meteo = repository.getOpenMeteoWeather(lat, lon)
            val air = repository.getAirPollutionForecast(lat, lon)
            Log.d("TAGX", "air response: $air")
            val current = repository.getCurrentWeather(lat, lon)

            meteo?.let { meteoData.postValue(it) }
            air?.let { airPollutionData.postValue(it) }
            current?.let { currentWeather.postValue(it) }
        }
    }


    fun setWeatherCode(code: String) {
        _weatherCode.value = code
    }

    fun setFormattedDate(code: String){
        _formattedDailyDate.value = code
    }

    fun setTemperature(code: String) {
        _temperature.value = code
    }

    fun setTargetOneWeek(code:Boolean){
        _isTargetOneWeek.value = code
    }

    fun setTemperature2mMin(code: String) {
        _temperature2mMin.value = code
    }

    fun setTemperature2mMax(code: String) {
        _temperature2mMax.value = code
    }

    fun setOneWeekWeatherCode(code: List<String>){
        _oneWeekWeatherCode.value = code
    }

    fun setOneWeekTimes(code: List<String>){
        _oneWeekTimes.value = code
    }

    fun setOneWeekMaxTemperature(code: List<Double>){
        _oneWeekMaxTemperature.value = code
    }

    fun setOneWeekMinTemperature(code: List<Double>){
        _oneWeekMinTemperature.value = code
    }


}