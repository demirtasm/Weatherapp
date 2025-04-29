package com.madkit.weatherapp.views

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.madkit.weatherapp.R
import com.madkit.weatherapp.adapter.HourlyWeatherAdapter
import com.madkit.weatherapp.databinding.FragmentBaseWeatherBinding
import com.madkit.weatherapp.domain.model.AirPollution
import com.madkit.weatherapp.data.model.AirPollutionResponse
import com.madkit.weatherapp.domain.model.HourlyWeather
import com.madkit.weatherapp.data.model.OpenMeteoResponse
import com.madkit.weatherapp.utils.WeatherCodeUtils
import com.madkit.weatherapp.viewmodel.LocationViewModel
import com.madkit.weatherapp.viewmodel.WeatherViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.madkit.weatherapp.domain.model.uistate.HourlyWeatherUIState
import com.madkit.weatherapp.utils.DayType
import com.madkit.weatherapp.views.customViews.LineChartMarkerView
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@AndroidEntryPoint
abstract class BaseWeatherFragment : Fragment() {
    private lateinit var binding:FragmentBaseWeatherBinding
    protected val weatherViewModel: WeatherViewModel by viewModels({ requireActivity() })
    protected val locationViewModel: LocationViewModel by viewModels({ requireActivity() })

    val hourlyList = mutableListOf<HourlyWeather>()
    val airPollutionList = mutableListOf<AirPollution>()
    private lateinit var barChart: BarChart
    private lateinit var lineChart: LineChart
    private lateinit var currentHourStr: String

    val windSpeedList = mutableListOf<Entry>()
    val timesForLabels = mutableListOf<String>()
    val weatherWindDirection = mutableListOf<Double>()

    abstract fun getTargetDate(): Int

    abstract fun getLocaleDate(): LocalDateTime

    open fun getShouldScrollToHour(): Boolean = true
    open fun getShouldHighlightHour(): Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBaseWeatherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        barChart = binding.barChart
        lineChart = binding.lineChart
        observeWeatherData()

        currentHourStr = if (getTargetDate() == 0) {
            getLocaleDate().hour.toString().padStart(2, '0') + ":00"
        } else {
            "00:00"
        }
    }

    private  fun observeWeatherData() {
        weatherViewModel.airPollutionData.observe(viewLifecycleOwner) { data ->
            data?.let {
                setupAqiChartWithResponse(it)
            }
        }
        weatherViewModel.meteoData.observe(viewLifecycleOwner) { data ->
            data?.let {
                setupMateoUI(it)
            }
        }
        weatherViewModel.hourlyUIState.observe(viewLifecycleOwner) { state ->
            if (
                state.hourlyAllTemperature.isNotEmpty() &&
                state.hourlyWindSpeed.isNotEmpty() &&
                hourlyList.isEmpty()
            ) {
                setupHourlyList(state)
            }
        }
    }

    private fun setupMateoUI(response: OpenMeteoResponse) {
        val daily = response.daily ?: return
        val hourly = response.hourly ?: return
        val indexForDay = getIndexForTargetDate(false, response.daily?.time ?: emptyList(), getLocaleDate())
        val indexForHour = getIndexForTargetDate(true, response.hourly?.time ?: emptyList(), getLocaleDate())

        val indexForDaily = getIndexForTargetDate(false, response.daily?.time ?: emptyList(), getLocaleDate())
        weatherViewModel.updateDailyUI(daily, indexForDaily)

        val indexFourHourly =  getIndexForTargetDate(true, response.hourly?.time ?: emptyList(), getLocaleDate())
        weatherViewModel.updateHourlyUI(hourly, indexFourHourly)

        weatherViewModel.updateWeeklyUI(daily)


        weatherViewModel.setFormattedDate(getFormattedDateTime())

        weatherViewModel.dailyUIState.observe(requireActivity()) { state ->
            binding.windSpeed.text = state.windGusts10mMean+ getString(R.string.kmh)
            binding.uvIndex.text = state.uvIndex
            binding.humidty.text =state.humidity
            binding.tvSunsetTime.text = unixTime(state.sunsetTime)
            binding.tvSunriseTime.text = unixTime(state.sunriseTime)
            binding.tvRainChange.text = "%"+state.rainChange
            binding?.sunriseSunsetView?.setSunriseSunset(
                sunrise = state.sunriseTime,
                sunset = state.sunsetTime
            )
        }
        indexForDay.let {
            val temp = daily.temperature_2m_max?.getOrNull(it)?.roundToInt()
            val code = daily.weather_code?.getOrNull(it)?.toInt() ?: 0
            val desc = WeatherCodeUtils.getWeatherDescription(requireContext(), code)

            val summary = "$desc, maksimum $temp°C"
            val prefs = requireContext().getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("weather_summary", summary).apply()


        }
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                binding?.sunriseSunsetView?.updateCurrentTime()
                handler.postDelayed(this, 60000)
            }
        }
        handler.post(runnable)
        if (getTargetDate() == 0) {
            weatherViewModel.setTargetDayType(DayType.TODAY)

            binding.sunsetSunriseChart.visibility = View.VISIBLE
            binding.sunriseSunsetTextViewParent.visibility = View.GONE
        } else if(getTargetDate() == 1) {
            weatherViewModel.setTargetDayType(DayType.TOMORROW)

            binding.sunsetSunriseChart.visibility = View.GONE
            binding.sunriseSunsetTextViewParent.visibility = View.VISIBLE
        }

    }
    private fun getFormattedDateTime(): String {
        val targetDate = if (getTargetDate() == 0) {
            LocalDateTime.now()
        } else {
            LocalDateTime.now().plusDays(1)
        }

        val outputFormatter = if (getTargetDate() == 0) {
            DateTimeFormatter.ofPattern("MMMM dd, HH:mm", Locale.getDefault())
        } else {
            DateTimeFormatter.ofPattern("MMMM dd", Locale.getDefault())
        }

        return targetDate.format(outputFormatter).replaceFirstChar { it.uppercase() }
    }
    fun getIndexForTargetDate(
        isHourly: Boolean,
        timeList: List<String>,
        targetDate: LocalDateTime
    ): Int {
        return if (isHourly) {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
            val currentDate = getLocaleDate()
            val targetDate = if (getTargetDate() == 0) currentDate else currentDate.plusDays(1)

            timeList.indexOfFirst { timeStr ->
                val dateTime = LocalDateTime.parse(timeStr, formatter)
                if (getTargetDate() == 0) {
                    dateTime.hour == targetDate.hour && dateTime.toLocalDate() == targetDate.toLocalDate()
                } else {
                    dateTime.hour == 0 && dateTime.toLocalDate() == targetDate.toLocalDate()
                }
            }
        } else {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val targetDateStr = if (getTargetDate() == 0) {
                getLocaleDate().toLocalDate().toString()
            } else {
                getLocaleDate().toLocalDate().toString()
            }

            timeList.indexOfFirst { it == targetDateStr }
        }

    }



    private fun setupWindSpeedChartData(it: OpenMeteoResponse) {

        val lineDataSet = LineDataSet(windSpeedList, "Wind Speed (10m)").apply {
            color = ContextCompat.getColor(requireContext(), R.color.purple)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.purple))
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
        }

        val lineData = LineData(lineDataSet)
        lineChart.data = lineData
        lineChart.setVisibleXRangeMaximum(6f)

        if (getShouldScrollToHour()) {
            val currentHour = getLocaleDate().hour.toFloat()
            lineChart.moveViewToX(currentHour)
            windSpeedList.firstOrNull { it.x == currentHour }?.let {
                lineChart.highlightValue(it.x, it.y, 0)
            }
        }

        lineChart.apply {
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            legend.isEnabled = false
            description.isEnabled = false
            axisRight.isEnabled = false
            axisLeft.textColor = Color.DKGRAY

            xAxis.apply {
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return value.toInt().toString().padStart(2, '0') + ":00"
                    }
                }
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                textColor = Color.DKGRAY
                textSize = 12f
                setDrawGridLines(false)
            }
            invalidate()
        }

        val markerView = LineChartMarkerView(
            requireContext(),
            R.layout.line_chart_marker_view,
            timesForLabels,
            weatherWindDirection
        ).also {
            it.chartView = lineChart
        }

        lineChart.marker = markerView
    }

    private fun setupHourlyList(state: HourlyWeatherUIState) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        val hourlyTimeList = weatherViewModel.meteoData.value?.hourly?.time ?: return
        val targetDate = getLocaleDate().toLocalDate()

        windSpeedList.clear()
        timesForLabels.clear()
        weatherWindDirection.clear()
        hourlyList.clear()

        for (i in hourlyTimeList.indices) {
            val dateTime = LocalDateTime.parse(hourlyTimeList[i], formatter)
            if (dateTime.toLocalDate() == targetDate &&
                i < state.hourlyAllTemperature.size &&
                i < state.hourlyAllWeatherCode.size &&
                i < state.hourlyIsDaY.size &&
                i < state.hourlyPrecipitationProbability.size &&
                i < state.hourlyWindSpeed.size &&
                i < state.hourlyWindDirection.size
            ) {
                val hour = dateTime.hour
                val timeStr = hour.toString().padStart(2, '0') + ":00"

                windSpeedList.add(Entry(hour.toFloat(), state.hourlyWindSpeed[i].toFloat()))
                timesForLabels.add(timeStr)
                weatherWindDirection.add(state.hourlyWindDirection[i])

                hourlyList.add(
                    HourlyWeather(
                        time = timeStr,
                        temperature = state.hourlyAllTemperature[i],
                        precipitation = state.hourlyPrecipitationProbability[i],
                        weatherCode = state.hourlyAllWeatherCode[i],
                        isDay = state.hourlyIsDaY[i] == 1
                    )
                )
            }
        }

        binding.hourlyRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = HourlyWeatherAdapter(hourlyList, currentHourStr)
        }

        val selectedIndex = hourlyList.indexOfFirst { it.time.startsWith(currentHourStr) }
        if (selectedIndex != -1 && getShouldScrollToHour()) {
            binding.hourlyRecyclerView.scrollToPosition(selectedIndex)
        }

        setupWindSpeedChartData(weatherViewModel.meteoData.value!!)
    }

    private fun setupAqiChartWithResponse(response: AirPollutionResponse) {
        val istanbulZone = ZoneId.systemDefault()
        val targetDate = getLocaleDate().toLocalDate()
        val list = response.list.mapNotNull {
            val dateTime = Instant.ofEpochSecond(it.dt).atZone(istanbulZone).toLocalDateTime()
            if (dateTime.toLocalDate() == targetDate) {
                AirPollution(
                    time = dateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    aqi = it.main.aqi,
                    pm25 = it.components.pm2_5,
                    co = it.components.co,
                    no = it.components.no,
                    no2 = it.components.no2,
                    o3 = it.components.o3,
                    so2 = it.components.so2,
                    pm10 = it.components.pm10,
                    nh3 = it.components.nh3
                )
            } else null
        }
        airPollutionList.clear()
        airPollutionList.addAll(list)
        setupAqiChart(list)
    }

    private fun setupAqiChart(data: List<AirPollution>) {
        val chartData = data.take(5).mapIndexed { index, item ->
            BarEntry(index.toFloat(), item.aqi.toFloat())
        }

        val dataSet = BarDataSet(chartData, "AQI").apply {
            color = ContextCompat.getColor(requireContext(), R.color.purple)
            valueTextSize = 12f
            setDrawValues(false)
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.6f
        }

        barChart.data = barData

        val timeLabels = data.take(5).map { it.time }

        barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(timeLabels)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            textSize = 12f
            textColor = Color.DKGRAY
            setDrawGridLines(false)
        }
        barChart.setScaleEnabled(false)
        barChart.setPinchZoom(false)
        barChart.setDoubleTapToZoomEnabled(false)
        barChart.isDragEnabled = false
        barChart.isScaleXEnabled = false
        barChart.isScaleYEnabled = false
        barChart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 5f
            granularity = 1f
            textSize = 12f
            textColor = Color.DKGRAY
            isEnabled = false
        }

        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setTouchEnabled(true)
        barChart.animateY(0)
        barChart.invalidate()
        if (chartData.isNotEmpty()) {
            val firstEntry = chartData[0]
            barChart.highlightValue(Highlight(firstEntry.x, firstEntry.y, 0))
        }
        data.firstOrNull()?.let {
            val coValue = it.co
            val pm10 = it.pm10
            val o3 = it.o3
            val aqi = it.aqi
            binding.textCoValue.text = coValue.roundToInt().toString()
            binding.textPM10Value.text = pm10.roundToInt().toString()
            binding.texto3Value.text = o3.roundToInt().toString()
            binding.aqiValue.text = aqi.roundToInt().toString()
            airPopulationIndex(it.aqi)
        }
        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e != null && h != null) {
                    val index = e.x.toInt()
                    val selectedItem = data.getOrNull(index)
                    selectedItem?.let {
                        val coValue = it.co
                        val pm10 = it.pm10
                        val o3 = it.o3
                        val aqi = it.aqi
                        binding.textCoValue.text = coValue.roundToInt().toString()
                        binding.textPM10Value.text = pm10.roundToInt().toString()
                        binding.texto3Value.text = o3.roundToInt().toString()
                        binding.aqiValue.text = aqi.roundToInt().toString()
                        airPopulationIndex(it.aqi)
                    }
                }
            }

            override fun onNothingSelected() {
                binding.textCoValue.text = ""
            }

        })

    }

    private fun airPopulationIndex(aqi: Double) {
        when (aqi.toInt()) {
            1 -> {
                binding.qualityText.text = getString(R.string.air_quality_index_one_text)
                binding.qualityDescription.text = getString(R.string.air_quality_index_one_text)
            }

            2 -> {
                binding.qualityText.text = getString(R.string.air_quality_index_two_text)
                binding.qualityDescription.text = getString(R.string.air_quality_index_two_desc)
            }

            3 -> {
                binding.qualityText.text = getString(R.string.air_quality_index_three_text)
                binding.qualityDescription.text = getString(R.string.air_quality_index_three_desc)
            }

            4 -> {
                binding.qualityText.text = getString(R.string.air_quality_index_four_text)
                binding.qualityDescription.text = getString(R.string.air_quality_index_four_desc)
            }

            5 -> {
                binding.qualityText.text = getString(R.string.air_quality_index_five_text)
                binding.qualityDescription.text = getString(R.string.air_quality_index_five_desc)
            }
        }

    }

    private fun unixTime(timex: String): String? {
        val parsed = LocalDateTime.parse(timex)
        val timeFormatted = parsed.format(DateTimeFormatter.ofPattern("HH:mm"))
        return timeFormatted
    }

}