package com.example.weatherapp.views

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.Constants
import com.example.weatherapp.R
import com.example.weatherapp.adapter.HourlyWeatherAdapter
import com.example.weatherapp.databinding.FragmentTodayBinding
import com.example.weatherapp.models.AirPollution
import com.example.weatherapp.models.AirPollutionResponse
import com.example.weatherapp.models.HourlyWeather
import com.example.weatherapp.models.OpenMeteoResponse
import com.example.weatherapp.network.WeatherService
import com.example.weatherapp.viewmodel.LocationViewModel
import com.example.weatherapp.viewmodel.WeatherViewModel
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
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.TimeZone
import kotlin.math.roundToInt

abstract class BaseWeatherFragment : Fragment() {
    private lateinit var binding: FragmentTodayBinding
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var weatherViewModel: WeatherViewModel
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
        binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        locationViewModel = ViewModelProvider(requireActivity())[LocationViewModel::class.java]
        weatherViewModel = ViewModelProvider(requireActivity())[WeatherViewModel::class.java]
        weatherViewModel.setTargetOneWeek(false)
        barChart = binding.barChart
        lineChart = binding.lineChart
        locationViewModel.location.observe(viewLifecycleOwner) { (lat, lon) ->
            getLocationOpenMeteoWeatherDetails(lat, lon)
            getAirPollutionForecast(lat, lon)
        }
        currentHourStr = if (getTargetDate() == 0) {
            getLocaleDate().hour.toString().padStart(2, '0') + ":00"
        } else {
            "00:00"
        }
        lifecycleScope.launch(Dispatchers.Main) {
            MobileAds.initialize(requireContext()) {
                val adRequest = AdRequest.Builder().build()
                binding.bannerAdView.loadAd(adRequest)
                binding.bannerAdView2.loadAd(adRequest)
            }
        }
    }

    private fun getAirPollutionForecast(lat: Double, lon: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL_OPEN_WEATHER)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WeatherService::class.java)
        val call = service.getAirPollutionForecast(
            lat = lat,
            lon = lon,
            appid = Constants.APP_ID
        )

        call.enqueue(object : Callback<AirPollutionResponse> {
            override fun onResponse(
                call: Call<AirPollutionResponse>,
                response: Response<AirPollutionResponse>
            ) {
                if (response.isSuccessful) {
                    val airData = response.body()
                    val targetDate = getLocaleDate().toLocalDate()
                    val istanbulZone = ZoneId.of("Europe/Istanbul")
                    airData?.list?.forEach { item ->
                        val dateTime = Instant.ofEpochSecond(item.dt)
                            .atZone(istanbulZone)
                            .toLocalDateTime()

                        if (dateTime.toLocalDate() == targetDate) {
                            val readableTime = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))

                            airPollutionList.add(
                                AirPollution(
                                    time = readableTime,
                                    aqi = item.main.aqi,
                                    pm25 = item.components.pm2_5,
                                    co = item.components.co,
                                    no = item.components.no,
                                    no2 = item.components.no2,
                                    o3 = item.components.o3,
                                    so2 = item.components.so2,
                                    pm10 = item.components.pm10,
                                    nh3 = item.components.nh3
                                )
                            )
                        }
                    }

                    setupAqiChart(airPollutionList)
                }
            }

            override fun onFailure(call: Call<AirPollutionResponse>, t: Throwable) {
                Log.e("AIR", "Hava kirliliği verisi alınamadı: ${t.message}")
            }
        })
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
            binding.aqiValue.text = aqi.toString()
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
                        binding.aqiValue.text = aqi.toString()
                        airPopulationIndex(it.aqi)
                    }
                }
            }

            override fun onNothingSelected() {
                binding.textCoValue.text = ""
            }

        })

    }

    private fun airPopulationIndex(aqi: Int) {
        when (aqi) {
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

    private fun getLocationOpenMeteoWeatherDetails(latitude: Double, longitude: Double) {
        val retrofit: Retrofit =
            Retrofit.Builder().baseUrl(Constants.BASE_URL_OPEN_METEO).addConverterFactory(
                GsonConverterFactory.create()
            ).build()
        val service: WeatherService =
            retrofit.create<WeatherService>(WeatherService::class.java)
        val call = service.getOpenMeteoWeather(
            latitude,
            longitude,
            current = "temperature_2m",
            hourly = "temperature_2m,precipitation,weather_code,rain,precipitation_probability,wind_speed_10m,wind_direction_10m",
            daily = "weather_code,wind_gusts_10m_mean,uv_index_max,relative_humidity_2m_mean,sunrise,sunset,temperature_2m_max,temperature_2m_min",
            timezone = "Europe/Moscow"

        )
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        call.enqueue(object : Callback<OpenMeteoResponse> {

            override fun onResponse(
                call: Call<OpenMeteoResponse>,
                response: Response<OpenMeteoResponse>
            ) {
                if (response.isSuccessful) {
                    val weatherData = response.body()
                    weatherData?.let {
                        val hourly = it.hourly

                        val size = hourly?.time?.size

                        for (i in 0 until size!!) {
                            val time = hourly.time[i]
                            val windSpeed = hourly.wind_speed_10m?.get(i)
                            val windDirection = hourly?.wind_direction_10m?.get(i)
                            val temperature = hourly?.temperature_2m?.get(i)
                            val precipitation = hourly?.precipitation_probability?.get(i)
                            val weatherCode = hourly?.weather_code?.get(i)
                            val dateTime = LocalDateTime.parse(time, formatter)


                            if (dateTime.toLocalDate() == getLocaleDate().toLocalDate()) {
                                val hour = dateTime.hour
                                windSpeedList.add(Entry(hour.toFloat(), windSpeed?.toFloat() ?: 0f))
                                timesForLabels.add(hour.toString().padStart(2, '0') + ":00")
                                weatherWindDirection.add(windDirection!!)

                                val splitTime = time.split("T").get(1)
                                hourlyList.add(
                                    HourlyWeather(
                                        time = splitTime,
                                        temperature = temperature!!,
                                        precipitation = precipitation!!,
                                        weatherCode = weatherCode!!
                                    )
                                )
                            }

                        }
                        val recyclerView = binding.hourlyRecyclerView
                        recyclerView.layoutManager = LinearLayoutManager(
                            recyclerView.context,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )

                        val selectedIndex =
                            hourlyList.indexOfFirst { it.time.startsWith(currentHourStr) }
                        setUpMateoUI(it)
                        recyclerView.adapter = HourlyWeatherAdapter(hourlyList, currentHourStr)
                        if (selectedIndex != -1 && getShouldScrollToHour()) {
                            recyclerView.scrollToPosition(selectedIndex)
                        }
                    }
                    val lineDataSet = LineDataSet(windSpeedList, "Wind Speed (10m)")
                    lineDataSet.color = ContextCompat.getColor(requireContext(), R.color.purple)
                    lineDataSet.setCircleColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.purple
                        )
                    )
                    lineDataSet.lineWidth = 2f
                    lineDataSet.circleRadius = 4f
                    lineDataSet.setDrawValues(false)

                    setupWindSpeedChart(lineDataSet)


                }
            }

            override fun onFailure(call: Call<OpenMeteoResponse>, t: Throwable) {
                // Handle failure
            }
        })


    }

    private fun setupWindSpeedChart(lineDataSet: LineDataSet) {
        val lineData = LineData(lineDataSet)
        lineChart.data = lineData
        lineChart.setVisibleXRangeMaximum(6f)
        if (getShouldScrollToHour()) {
            val currentHour = getLocaleDate().hour.toFloat()
            lineChart.moveViewToX(currentHour)
            val selectedEntry = windSpeedList.firstOrNull { it.x.toInt().toFloat() == currentHour }
            selectedEntry?.let {
                lineChart.highlightValue(it.x, it.y, 0)
            }
        }
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(false)
        lineChart.setPinchZoom(false)
        lineChart.description.text = "Wind Speed throughout the Day"
        lineChart.invalidate()

        lineChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(timesForLabels)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            isGranularityEnabled = true
            setDrawGridLines(false)
            textColor = Color.DKGRAY
            textSize = 12f
            labelRotationAngle = 0f
            setLabelCount(6, false)
        }


        lineChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val hour = value.toInt().toString().padStart(2, '0')
                return "$hour:00"
            }
        }
        lineChart.axisRight.isEnabled = false
        lineChart.axisLeft.textColor = Color.DKGRAY
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(false)
        lineChart.setScaleEnabled(false)
        lineChart.legend.isEnabled = false
        lineChart.description.isEnabled = false

        lineChart.invalidate()

        val markerView = LineChartMarkerView(
            requireContext(),
            R.layout.line_chart_marker_view,
            timesForLabels,
            weatherWindDirection
        )
        markerView.chartView = lineChart
        lineChart.marker = markerView
    }

    private fun setUpMateoUI(it: OpenMeteoResponse) {
        val currentHourItem = hourlyList.firstOrNull { it.time.startsWith(currentHourStr) }
        val weatherCode = it.daily?.weather_code?.get(getTargetDate())
        val temperature = it.hourly?.temperature_2m?.get(getTargetDate())
        weatherCode?.let { code ->
            weatherViewModel.setWeatherCode(code)
        }
        temperature?.let { code ->
            weatherViewModel.setTemperature(code.roundToInt().toString())
        }
        weatherViewModel.setOneWeekWeatherCode(it.daily?.weather_code!!)
        weatherViewModel.setOneWeekTimes(it.daily?.time!!)
        weatherViewModel.setOneWeekMaxTemperature(it.daily.temperature_2m_max)
        weatherViewModel.setOneWeekMinTemperature(it.daily.temperature_2m_min)
        weatherViewModel.setTemperature2mMax(it.daily.temperature_2m_max.get(getTargetDate()).roundToInt().toString())
        weatherViewModel.setTemperature2mMin(it.daily.temperature_2m_min.get(getTargetDate()).roundToInt().toString())
        binding.windSpeed.text =
            it.daily?.wind_gusts_10m_mean?.get(getTargetDate()).toString() + getString(R.string.kmh)
        binding.uvIndex.text = it.daily?.uv_index_max?.get(getTargetDate()).toString()
        binding.humidty.text = it.daily?.relative_humidity_2m_mean?.get(getTargetDate()).toString()
        binding.tvRainChange.text = "% ${currentHourItem?.precipitation.toString()}"
        binding.tvSunsetTime.text = unixTime(it.daily?.sunset?.get(getTargetDate())!!)
        binding.tvSunriseTime.text = unixTime(it.daily?.sunrise?.get(getTargetDate())!!)

    }


    private fun unixTime(timex: String): String? {
        val parsed = LocalDateTime.parse(timex)
        val timeFormatted = parsed.format(DateTimeFormatter.ofPattern("HH:mm"))
        return timeFormatted
    }
}