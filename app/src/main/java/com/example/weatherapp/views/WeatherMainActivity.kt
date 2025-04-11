package com.example.weatherapp.views

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.weatherapp.PrefsHelper
import com.example.weatherapp.R
import com.example.weatherapp.databinding.ActivityWeatherMainBinding
import com.example.weatherapp.models.OpenMeteoResponse
import com.example.weatherapp.models.WeatherResponse
import com.example.weatherapp.network.OpenMeteoService
import com.example.weatherapp.network.WeatherService
import com.example.weatherapp.repository.WeatherRepository
import com.example.weatherapp.utils.WeatherCodeUtils
import com.example.weatherapp.viewmodel.LocationViewModel
import com.example.weatherapp.viewmodel.WeatherViewModel
import com.example.weatherapp.viewmodel.WeatherViewModelFactory
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

class WeatherMainActivity : AppCompatActivity() {

    private var navController: NavController? = null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mProgressDialog: Dialog? = null
    private lateinit var binding: ActivityWeatherMainBinding
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var weatherViewModel: WeatherViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        locationViewModel = ViewModelProvider(this)[LocationViewModel::class.java]
        val weatherApi = WeatherService.create()
        val meteoApi = OpenMeteoService.create()
        val repository = WeatherRepository(weatherApi, meteoApi)
        val factory = WeatherViewModelFactory(repository)

        weatherViewModel = ViewModelProvider(this, factory)[WeatherViewModel::class.java]


        weatherViewModel.meteoData.observe(this) { meteo ->
            meteo?.let {
                setUpMeteoUI(it)
            }
        }


        weatherViewModel.currentWeather.observe(this) { currentWeather ->
            currentWeather?.let {
                setUpCurrentWeatherUI(it)
            }

        }

        setUpCurrentUI()
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
        navController = navHostFragment?.findNavController()

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        binding.btnToday.setOnClickListener {
            updateTabSelection(R.id.btnToday)
            navController?.navigate(R.id.todayFragment)
        }


        binding.btnTomorrow.setOnClickListener {
            updateTabSelection(R.id.btnTomorrow)
            navController?.navigate(R.id.tomorrowFragment)

        }
        binding.btn1Week.setOnClickListener {
            updateTabSelection(R.id.btn1Week)
            navController?.navigate(R.id.oneWeekFragment)
        }

        binding.appBar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            if (Math.abs(verticalOffset) >= totalScrollRange) {
                binding.toolbar.setBackgroundResource(R.drawable.toolbar_gradient_color)

            } else {
                binding.toolbar.setBackgroundColor(Color.TRANSPARENT)
            }
        }

        if (PrefsHelper.isLocationGranted(this)) {
            requestLocationData()
        } else {
            Log.e("TAGX", "Konum izni onboarding sırasında verilmemiş.")
        }

    }

    private fun setUpCurrentUI() {

        binding.dateTime.text = getFormattedDateTime()

    }

    private fun setUpCurrentWeatherUI(weatherList: WeatherResponse) {
        val degree = WeatherCodeUtils.getUnit(application.resources.configuration.toString())
        binding.tvFeelsLike.text = "${getString(R.string.feels_like_txt)} ${weatherList.main.feels_like.roundToInt()} ${degree}"
        binding.tvName.text = " ${weatherList.name}, ${weatherList.sys.country}"
    }

    private fun setUpMeteoUI(meteo: OpenMeteoResponse?) {
        val degree = WeatherCodeUtils.getUnit(application.resources.configuration.toString())
        weatherViewModel.temperature.observe(this) { code ->
            binding.tvTemp.text = code + degree
        }
        weatherViewModel.isTargetOneWeek.observe(this) { code ->
            if (code) {
                weatherViewModel.oneWeekMaxTemperature.observe(this) { it ->
                    binding.tvDayTemp.text = it.average().roundToInt().toString() + degree
                }
                weatherViewModel.oneWeekMinTemperature.observe(this) {
                    binding.tvNightTemp.text = it.average().roundToInt().toString() + degree
                }
                binding.tvDayText.text = getString(R.string.daytime_weekly)
                binding.tvNightText.text = getString(R.string.nighttime_weekly)
            } else {
                weatherViewModel.temperature2mMax.observe(this) { it ->
                    binding.tvDayTemp.text = it + degree
                }
                weatherViewModel.temperature2mMin.observe(this) { it ->
                    binding.tvNightTemp.text = it + degree
                }
                binding.tvDayText.text = getString(R.string.day_txt)
                binding.tvNightText.text = getString(R.string.night_txt)
            }
        }
        weatherViewModel.weatherCode.observe(this) { code ->
            binding.ivMain.setImageResource(WeatherCodeUtils.getWeatherIconResId(code.toInt()))
            binding.tvMain.text =
                WeatherCodeUtils.getWeatherDescription(applicationContext, code.toInt())
        }
    }

    private fun updateTabSelection(selectedButtonId: Int) {
        val selectedColor = Color.parseColor("#E1B6FF")
        val defaultColor = Color.parseColor("#FFFFFF")

        binding.btnToday.backgroundTintList = ColorStateList.valueOf(defaultColor)
        binding.btnTomorrow.backgroundTintList = ColorStateList.valueOf(defaultColor)
        binding.btn1Week.backgroundTintList = ColorStateList.valueOf(defaultColor)

        when (selectedButtonId) {
            R.id.btnToday -> {
                binding.btnToday.backgroundTintList =
                    ColorStateList.valueOf(selectedColor)
            }

            R.id.btnTomorrow -> {
                binding.btnTomorrow.backgroundTintList =
                    ColorStateList.valueOf(selectedColor)
            }

            R.id.btn1Week -> {
                binding.btn1Week.backgroundTintList =
                    ColorStateList.valueOf(selectedColor)

            }
        }
    }

    private val mLocationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation!!
            val latitude = mLastLocation.latitude
            val longitude = mLastLocation.longitude
            Log.e("TAGX", "LATİTUDE: ${latitude} ,Longıtude: ${longitude}")
            if (shouldUpdateLocation(latitude, longitude)) {
                locationViewModel.setLocation(latitude, longitude)
                weatherViewModel.loadWeatherData(latitude, longitude)
            }
           // locationViewModel.setLocation(latitude, longitude)
           // weatherViewModel.loadWeatherData(latitude, longitude)
            locationViewModel.setLocation(latitude, longitude)
            mFusedLocationClient.removeLocationUpdates(this)
            runOnUiThread {
                binding.btnToday.performClick()
            }
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
    private fun shouldUpdateLocation(newLat: Double, newLon: Double): Boolean {
        val lastLat = locationViewModel.getLatitude() ?: return true
        val lastLon = locationViewModel.getLongitude() ?: return true

        val distance = FloatArray(1)
        Location.distanceBetween(lastLat, lastLon, newLat, newLon, distance)

        Log.d("TAGX", "Distance from last location: ${distance[0]}m")

        return distance[0] > 500 
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


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

 /*   override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                requestLocationData()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }*/

    private fun getFormattedDateTime(): String {
        val now = LocalDateTime.now()
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        val outputFormatter = DateTimeFormatter.ofPattern("MMMM dd, HH:mm", Locale.getDefault())
        val nowFormatted = now.format(inputFormatter)
        val displayText = LocalDateTime.parse(nowFormatted, inputFormatter).format(outputFormatter)
        return displayText
    }
}