package com.madkit.weatherapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madkit.weatherapp.models.AirPollutionResponse
import com.madkit.weatherapp.models.DailyData
import com.madkit.weatherapp.models.HourlyData
import com.madkit.weatherapp.models.uistate.DailyWeatherUIState
import com.madkit.weatherapp.models.OpenMeteoResponse
import com.madkit.weatherapp.models.WeatherResponse
import com.madkit.weatherapp.models.uistate.HourlyWeatherUIState
import com.madkit.weatherapp.models.uistate.WeeklyWeatherUIState
import com.madkit.weatherapp.repository.WeatherRepository
import com.madkit.weatherapp.utils.DayType
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    val meteoData = MutableLiveData<OpenMeteoResponse>()
    val airPollutionData = MutableLiveData<AirPollutionResponse>()
    val currentWeather = MutableLiveData<WeatherResponse>()

    private val _dailyUIState = MutableLiveData<DailyWeatherUIState>()
    val dailyUIState: LiveData<DailyWeatherUIState> = _dailyUIState

    private val _weeklyUIState = MutableLiveData<WeeklyWeatherUIState>()
    val weeklyUIState: LiveData<WeeklyWeatherUIState> = _weeklyUIState

    private val _hourlyUIState = MutableLiveData<HourlyWeatherUIState>()
    val hourlyUIState: LiveData<HourlyWeatherUIState> = _hourlyUIState

    private val _formattedDailyDate = MutableLiveData<String>()
    val formattedDailyDate: LiveData<String> = _formattedDailyDate


    private val _targetDayType = MutableLiveData<DayType>()
    val targetDayType: LiveData<DayType> = _targetDayType

    fun updateHourlyUI(hourly: HourlyData, indexFourHourly: Int) {
        val hourlyTemperature = hourly.temperature_2m?.getOrNull(indexFourHourly)?.roundToInt().toString()

        _hourlyUIState.value = HourlyWeatherUIState(hourlyTemperature)
    }

    fun updateDailyUI(daily: DailyData, index: Int) {
        val weatherCode = daily.weather_code?.getOrNull(index)?.toString() ?: "0"

        val temperature2mMax = daily.temperature_2m_max?.getOrNull(index)?.roundToInt().toString()
        val temperature2mMin = daily.temperature_2m_min?.getOrNull(index)?.roundToInt().toString()
        val apparentTemperature =
            daily?.apparent_temperature_mean?.getOrNull(index)?.roundToInt().toString()
        val windGusts10mMean = daily.wind_gusts_10m_mean?.getOrNull(index)?.roundToInt().toString()
        val uvIndex = daily.uv_index_max?.getOrNull(index)?.roundToInt().toString()
        val humidity = daily.relative_humidity_2m_mean?.getOrNull(index).toString()
        val sunset = daily.sunset?.getOrNull(index).toString()
        val sunrise = daily.sunrise?.getOrNull(index).toString()
        val rainChange = daily.precipitation_probability_mean.getOrNull(index).toString()

        _dailyUIState.value = DailyWeatherUIState(
            weatherCode,
            temperature2mMax,
            temperature2mMin,
            apparentTemperature,
            windGusts10mMean,
            uvIndex, humidity,
            sunset, sunrise,
            rainChange
        )


    }

    fun updateWeeklyUI(daily: DailyData) {
        val weeklyWeatherCode = daily.weather_code
        val weeklyMaxTemperature = daily.temperature_2m_max
        val weeklyMinTemperature = daily.temperature_2m_min
        val weeklyTimes = daily.time
        val weeklyRelativeHumidity = daily.relative_humidity_2m_mean
        val weeklyApparentTemperature = daily.apparent_temperature_mean

        _weeklyUIState.value = WeeklyWeatherUIState(
            weeklyWeatherCode, weeklyMaxTemperature, weeklyMinTemperature, weeklyTimes, weeklyRelativeHumidity, weeklyApparentTemperature
        )
    }

    fun loadWeatherData(lat: Double, lon: Double) {
        viewModelScope.launch {
            val meteo = repository.getOpenMeteoWeather(lat, lon)
            val air = repository.getAirPollutionForecast(lat, lon)
            val current = repository.getCurrentWeather(lat, lon)

            meteo?.let { meteoData.postValue(it) }
            air?.let { airPollutionData.postValue(it) }
            current?.let { currentWeather.postValue(it) }
        }
    }


    fun setFormattedDate(code: String) {
        _formattedDailyDate.value = code
    }


    fun setTargetDayType(type: DayType) {
        _targetDayType.value = type
    }

}