package com.example.weatherapp.views

import android.annotation.SuppressLint
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


class TodayFragment : Fragment() {
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

                        val recyclerView = binding.rainChanceRecyclerView
                        recyclerView.layoutManager = LinearLayoutManager(
                            recyclerView.context,
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                        recyclerView.adapter = AirPollutionAdapter(airPollutionList)
                    }
                }
            }

            override fun onFailure(call: Call<AirPollutionResponse>, t: Throwable) {
                Log.e("AIR", "Hava kirliliği verisi alınamadı: ${t.message}")
            }
        })
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