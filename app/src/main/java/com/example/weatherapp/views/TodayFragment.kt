package com.example.weatherapp.views

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.db.williamchart.view.LineChartView
import com.example.weatherapp.Constants
import com.example.weatherapp.R
import com.example.weatherapp.adapter.AirPollutionAdapter
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
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.TimeZone
import kotlin.math.roundToInt


class TodayFragment : Fragment() {
    private lateinit var barChart: BarChart
    private lateinit var binding: FragmentTodayBinding
    val hourlyList = mutableListOf<HourlyWeather>()
    val airPollutionList = mutableListOf<AirPollution>()
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var weatherViewModel: WeatherViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationViewModel = ViewModelProvider(requireActivity())[LocationViewModel::class.java]
        weatherViewModel = ViewModelProvider(requireActivity())[WeatherViewModel::class.java]
barChart = binding.chart

        lifecycleScope.launch(Dispatchers.Main) {
            MobileAds.initialize(requireContext()) {
                val adRequest = AdRequest.Builder().build()
                binding.bannerAdView.loadAd(adRequest)
            }
        }
        locationViewModel.location.observe(viewLifecycleOwner) { (lat, lon) ->
            getLocationOpenMeteoWeatherDetails(lat, lon)
            getAirPollutionForecast(lat, lon)
        }
        /* val chart = view.findViewById<LineChartView>(R.id.lineChart)
         val data = listOf(
             "00:00" to 8f,
             "03:00" to 12f,
             "06:00" to 10f,
             "09:00" to 15f,
             "12:00" to 20f,
             "15:00" to 18f,
             "18:00" to 10f,
             "21:00" to 6f
         )

         chart.animation.duration = 1000
         chart.show(data)*/


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
                    airData?.list?.take(12)?.forEach { item ->
                        val dt = item.dt
                        val aqi = item.main.aqi
                        val pm25 = item.components.pm2_5
                        val readableTime = SimpleDateFormat("HH:mm").apply {
                            timeZone = TimeZone.getTimeZone("Europe/Istanbul")
                        }.format(Date(dt * 1000L))

                        Log.d("AIR", "Saat: $readableTime | AQI: $aqi | PM2.5: $pm25 µg/m³")
                        airPollutionList.add(
                            AirPollution(
                                time = readableTime.toString(),
                                aqi = aqi,
                                pm25 = pm25,
                                co = item.components.co,
                                no = item.components.no,
                                no2 = item.components.no2,
                                o3 = item.components.o3,
                                so2 = item.components.so2,
                                pm10 = item.components.pm10,
                                nh3 = item.components.nh3
                            )
                        )


                        /*val recyclerView = binding.rainChanceRecyclerView
                        recyclerView.layoutManager = LinearLayoutManager(
                            recyclerView.context,
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                        recyclerView.adapter = AirPollutionAdapter(airPollutionList)*/

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

    private fun airPopulationIndex(aqi: Int){
        when(aqi){
            1-> {
                binding.qualityText.text = getString(R.string.air_quality_index_one_text)
                binding.qualityDescription.text = getString(R.string.air_quality_index_one_text)
            }
            2-> {
                binding.qualityText.text = getString(R.string.air_quality_index_two_text)
                binding.qualityDescription.text = getString(R.string.air_quality_index_two_desc)
            }
            3-> {
                binding.qualityText.text = getString(R.string.air_quality_index_three_text)
                binding.qualityDescription.text = getString(R.string.air_quality_index_three_desc)
            }
            4-> {
                binding.qualityText.text = getString(R.string.air_quality_index_four_text)
                binding.qualityDescription.text = getString(R.string.air_quality_index_four_desc)
            }
            5-> {
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
            hourly = "temperature_2m,relative_humidity_2m,precipitation,weather_code,temperature_80m,temperature_120m,temperature_180m,rain,precipitation_probability",
            daily = "weather_code,wind_gusts_10m_mean,uv_index_max,relative_humidity_2m_mean,sunrise,sunset",
            timezone = "Europe/Moscow"

        )
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        val endOfDay = now.toLocalDate().atTime(23, 0)
        val maxRainExpected = 5.0
        call.enqueue(object : Callback<OpenMeteoResponse> {
            override fun onResponse(
                call: Call<OpenMeteoResponse>,
                response: Response<OpenMeteoResponse>
            ) {
                if (response.isSuccessful) {
                    val weatherData = response.body()
                    weatherData?.let {
                        val hourly = it.hourly
                        setUpMateoUI(it)
                        val size = hourly?.time?.size

                        for (i in 0 until size!!) {
                            val time = hourly.time[i]
                            val temperature = hourly?.temperature_2m?.get(i)
                            val precipitation = hourly?.precipitation_probability?.get(i)
                            val weatherCode = hourly?.weather_code?.get(i)
                            val rain = hourly?.rain?.get(i)
                            val dateTime = LocalDateTime.parse(time, formatter)
                            val calculatedPercent = ((rain!! / maxRainExpected) * 100)

                            Log.d(
                                "Weather",
                                "Saat: $time | Sıcaklık: $temperature°C | Yağış: $rain mm | Gündüz mü: ${calculatedPercent}"
                            )

                            if (dateTime.toLocalDate() == now.toLocalDate() &&
                                (dateTime.isAfter(now) || dateTime.hour == now.hour) &&
                                !dateTime.isAfter(endOfDay)
                            ) {
                                val splitTime = time.split("T").get(1)
                                hourlyList.add(
                                    HourlyWeather(
                                        time = splitTime,
                                        temperature = temperature!!,
                                        precipitation = precipitation!!,
                                        weatherCode = weatherCode!!
                                        //isDay = isDay
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
                        recyclerView.adapter = HourlyWeatherAdapter(hourlyList)
                    }

                }
            }

            override fun onFailure(call: Call<OpenMeteoResponse>, t: Throwable) {
                // Handle failure
            }
        })


    }

    private fun setUpMateoUI(it: OpenMeteoResponse) {

        val weatherCode = it.daily?.weather_code?.get(0)
        weatherCode?.let { code ->
            weatherViewModel.setWeatherCode(code)
        }
        binding.windSpeed.text = "${it.daily?.wind_gusts_10m_mean?.get(0).toString()} km/h"
        binding.uvIndex.text = it.daily?.uv_index_max?.get(0).toString()
        binding.humidty.text = it.daily?.relative_humidity_2m_mean?.get(0).toString()
        binding.tvSunsetTime.text = unixTime(it.daily?.sunset?.get(0)!!)
        binding.tvSunriseTime.text = unixTime(it.daily?.sunrise?.get(0)!!)

    }

    private fun unixTime(timex: String): String? {
        val parsed = LocalDateTime.parse(timex)
        val timeFormatted = parsed.format(DateTimeFormatter.ofPattern("HH:mm"))
        return timeFormatted
    }
}