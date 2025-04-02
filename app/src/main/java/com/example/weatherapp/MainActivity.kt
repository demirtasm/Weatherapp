package com.example.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.adapter.HourlyWeatherAdapter
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.models.HourlyWeather
import com.example.weatherapp.models.OpenMeteoResponse
import com.example.weatherapp.models.WeatherResponse
import com.example.weatherapp.network.WeatherService
import com.example.weatherapp.utils.WeatherCodeUtils
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.CoroutineScope
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
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mProgressDialog: Dialog? = null
    private lateinit var binding: ActivityMainBinding
    val hourlyList = mutableListOf<HourlyWeather>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            MobileAds.initialize(this@MainActivity) {
                val adRequest = AdRequest.Builder().build()
                binding.bannerAdView.loadAd(adRequest)
            }
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        if (!isLocationEnabled()) {
            Toast.makeText(this, "Your location provider is turned off", Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            Dexter.withActivity(this).withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ).withListener(
                object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {
                            requestLocationData()
                        }
                        if (report.isAnyPermissionPermanentlyDenied) {
                            Toast.makeText(
                                this@MainActivity,
                                "Your have denied location permission",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?
                    ) {
                        showRationalDialogForPermission()
                    }

                }).onSameThread().check()
        }

    }

    private fun getLocationWeatherDetails(latitude: Double, longitude: Double) {
        if (Constants.isNetworkAvailable(this)) {
            val retrofit: Retrofit =
                Retrofit.Builder().baseUrl(Constants.BASE_URL_OPEN_WEATHER).addConverterFactory(
                    GsonConverterFactory.create()
                ).build()

            val service: WeatherService =
                retrofit.create<WeatherService>(WeatherService::class.java)
            val listCall: Call<WeatherResponse> =
                service.getWeather(latitude, longitude, Constants.METRIC_UNIT, Constants.APP_ID)
            showProgressDialog()
            listCall.enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if (response.isSuccessful) {
                        hideProgressDialog()

                        val weatherList: WeatherResponse = response.body()!!
                        setupUI(weatherList)
                        Log.e("TAGX", weatherList.toString())
                    } else {
                        val rc = response.code()
                        when (rc) {
                            400 -> Log.e("Error", "Bad connection")
                            404 -> Log.e("Error", "Not Found")
                            else -> Log.e("Error", "Generic error")
                        }
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    Log.e("Error", t.message.toString())
                    hideProgressDialog()

                }

            })
        } else {
            Toast.makeText(
                this@MainActivity,
                "No internet",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
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
            daily = "weather_code",
            timezone = "Europe/Moscow"

        )
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        val endOfDay = now.toLocalDate().atTime(23, 0)
        val maxRainExpected = 5.0
        call.enqueue(object : Callback<OpenMeteoResponse> {
            override fun onResponse(call: Call<OpenMeteoResponse>, response: Response<OpenMeteoResponse>) {
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

                            Log.d("Weather", "Saat: $time | Sıcaklık: $temperature°C | Yağış: $rain mm | Gündüz mü: ${calculatedPercent}")

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
        binding.dateTime.text = getFormattedDateTime()

        binding.ivMain.setImageResource( WeatherCodeUtils.getWeatherIconResId(it.daily?.weather_code?.get(0)?.toInt()!!))

    }

    private val mLocationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation!!
            val latitude = mLastLocation.latitude
            val longitude = mLastLocation.longitude
            Log.e("TAGX", "LATİTUDE: ${latitude} ,Longıtude: ${longitude}")
            getLocationWeatherDetails(latitude, longitude)
            getLocationOpenMeteoWeatherDetails(latitude, longitude)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }

    private fun showRationalDialogForPermission() {
        AlertDialog.Builder(this)
            .setMessage("It looks like you have turned off permissions required for this feature")
            .setPositiveButton("Go to settings") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("It Looks like you have turned off permissions required for this feature. It can be enabled under Application Settings")
            .setPositiveButton(
                "GO TO SETTINGS"
            ) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog,
                                           _ ->
                dialog.dismiss()
            }.show()
    }

    private fun showProgressDialog() {
        mProgressDialog = Dialog(this)
        mProgressDialog!!.setContentView(R.layout.dialog_custom_progress)
        mProgressDialog!!.show()
    }

    private fun hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog!!.dismiss()
        }
    }

    private fun setupUI(weatherList: WeatherResponse) {
        for (i in weatherList.weather.indices) {
            //Log.i("Weather Name", weatherList.weather.toString())
            binding.tvMain.text = weatherList.weather[i].main
           //binding.tvMainDescription.text = weatherList.weather[i].description
            binding.tvTemp.text = weatherList.main.temp.roundToInt().toString() + getUnit(application.resources.configuration.toString())
           // binding.tvSunriseTime.text = unixTime(weatherList.sys.sunrise)
           // binding.tvSunsetTime.text = unixTime(weatherList.sys.sunset)
            //binding.tvHumidity.text = weatherList.main.humidity.toString() + "%"
            //binding.tvMin.text = weatherList.main.temp_min.toString()
             //binding.tvMax.text =weatherList.main.temp_max.toString()
            // binding.tvSpeed.text =weatherList.wind.speed.toString()
           // binding.tvClouds.text = weatherList.clouds.all.toString() + "%"
            binding.tvName.text =" ${weatherList.name}, ${weatherList.sys.country}"
            // binding.tvCountry.text = weatherList.sys.country
            binding.tvFeelsLike.text = "Feels like ${weatherList.main.feels_like.toString()}"
            //binding.tvSunsetTime.text =unixTime(weatherList.sys.sunset)

        }
    }

    private fun getUnit(value: String): String? {
        var value = "°"
        if ("US" == value || "LR" == value || "MM" == value) {
            value = "°F"
        }
        return value
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                requestLocationData()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun getFormattedDateTime(): String {
        val now = LocalDateTime.now()
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        val outputFormatter = DateTimeFormatter.ofPattern("MMMM dd, HH:mm", Locale.ENGLISH)
        val nowFormatted = now.format(inputFormatter)
        val displayText = LocalDateTime.parse(nowFormatted, inputFormatter).format(outputFormatter)
        return displayText
    }

    private fun unixTime(timex: Long): String? {
        val date = Date(timex * 1000L)
        @SuppressLint("SimpleDateFormat") val sdf =
            SimpleDateFormat("HH:mm")
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }
}