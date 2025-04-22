package com.madkit.weatherapp.views

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.madkit.weatherapp.R
import com.madkit.weatherapp.WeatherApplication
import com.madkit.weatherapp.databinding.ActivityWeatherMainBinding
import com.madkit.weatherapp.models.OpenMeteoResponse
import com.madkit.weatherapp.models.WeatherResponse
import com.madkit.weatherapp.utils.WeatherCodeUtils
import com.madkit.weatherapp.viewmodel.LocationViewModel
import com.madkit.weatherapp.viewmodel.WeatherViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.analytics.FirebaseAnalytics
import com.madkit.weatherapp.utils.DayType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

class WeatherMainActivity : AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var mostFrequentCode: String? = null
    private var navController: NavController? = null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mProgressDialog: Dialog? = null
    private lateinit var binding: ActivityWeatherMainBinding
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var weatherViewModel: WeatherViewModel
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        val app = application as WeatherApplication
        weatherViewModel =
            ViewModelProvider(this, app.weatherViewModelFactory)[WeatherViewModel::class.java]
        locationViewModel = app.locationViewModel


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

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
        navController = navHostFragment?.findNavController()

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        lifecycleScope.launch(Dispatchers.Main) {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                this@WeatherMainActivity,
                "ca-app-pub-3438221392612643/1988120079",
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        mInterstitialAd = ad
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        mInterstitialAd = null
                    }
                })
        }
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
            /* if (mInterstitialAd != null) {
                 mInterstitialAd?.show(this)
                 mInterstitialAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                     override fun onAdDismissedFullScreenContent() {
                         mInterstitialAd = null
                         navController?.navigate(R.id.oneWeekFragment)
                         val adRequest = AdRequest.Builder().build()
                         InterstitialAd.load(this@WeatherMainActivity,"ca-app-pub-3438221392612643/1988120079", adRequest, object : InterstitialAdLoadCallback() {
                             override fun onAdLoaded(ad: InterstitialAd) {
                                 mInterstitialAd = ad
                             }
                         })
                     }

                     override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                         mInterstitialAd = null
                         navController?.navigate(R.id.oneWeekFragment)
                     }
                 }
             }*/
        }

        binding.appBar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            if (Math.abs(verticalOffset) >= totalScrollRange) {
                binding.toolbar.setBackgroundResource(R.drawable.toolbar_gradient_color)

            } else {
                binding.toolbar.setBackgroundColor(Color.TRANSPARENT)
            }
        }

    }

    private fun setUpCurrentWeatherUI(weatherList: WeatherResponse) {
        val degree = WeatherCodeUtils.getUnit(application.resources.configuration.toString())


        binding.tvName.text = " ${weatherList.name}, ${weatherList.sys.country}"
    }

    private fun setUpMeteoUI(meteo: OpenMeteoResponse?) {
        val degree = WeatherCodeUtils.getUnit(application.resources.configuration.toString())
        var oneWeekMaxTemperature: String? = null
        var oneWeekMinTemperature: String? = null
        weatherViewModel.targetDayType.observe(this) { type ->
            weatherViewModel.formattedDailyDate.removeObservers(this)
            //weatherViewModel.temperature.removeObservers(this)
            when (type) {
                DayType.TOMORROW -> {

                    binding.tvDayText.text = getString(R.string.day_txt)
                    binding.tvNightText.text = getString(R.string.night_txt)
                    weatherViewModel.hourlyUIState.observe(this) {state->
                        binding.tvTemp.text = state.hourlyTemperature + degree
                    }
                    weatherViewModel.dailyUIState.observe(this) { state ->
                        binding.tvMain.text = WeatherCodeUtils.getWeatherDescription(
                            applicationContext,
                            state.weatherCode.toInt()
                        )
                        binding.ivMain.setImageResource(
                            WeatherCodeUtils.getWeatherIconResId(
                                state.weatherCode.toInt(),
                                true
                            )
                        )
                        binding.tvDayTemp.text = state.temperature2mMax + degree
                        binding.tvNightTemp.text = state.temperature2mMin + degree

                        binding.tvFeelsLike.text =
                            "${getString(R.string.feels_like_txt)} ${state.apparentTemperature} ${degree}"
                    }
                    weatherViewModel.formattedDailyDate.observe(this) { it ->
                        binding.dateTime.text = it
                    }
                   /* weatherViewModel.temperature.observe(this) { code ->
                        binding.tvTemp.text = code + degree
                    }*/
                }

                DayType.TODAY -> {

                    binding.tvDayText.text = getString(R.string.day_txt)
                    binding.tvNightText.text = getString(R.string.night_txt)
                    weatherViewModel.hourlyUIState.observe(this) {state->
                        binding.tvTemp.text = state.hourlyTemperature + degree
                    }
                    weatherViewModel.dailyUIState.observe(this) { state ->
                        binding.tvMain.text = WeatherCodeUtils.getWeatherDescription(
                            applicationContext,
                            state.weatherCode.toInt()
                        )
                        binding.ivMain.setImageResource(
                            WeatherCodeUtils.getWeatherIconResId(
                                state.weatherCode.toInt(),
                                true
                            )
                        )
                        binding.tvDayTemp.text = state.temperature2mMax + degree
                        binding.tvNightTemp.text = state.temperature2mMin + degree

                        binding.tvFeelsLike.text =
                            "${getString(R.string.feels_like_txt)} ${state.apparentTemperature} ${degree}"
                    }
                    weatherViewModel.formattedDailyDate.observe(this) { it ->
                        binding.dateTime.text = it
                    }
                    /*weatherViewModel.temperature.observe(this) { code ->
                        binding.tvTemp.text = code + degree
                    }*/
                }

                DayType.WEEKLY -> {
                    binding.tvDayText.text = getString(R.string.daytime_weekly)
                    binding.tvNightText.text = getString(R.string.nighttime_weekly)

                    weatherViewModel.weeklyUIState.observe(this) { state ->
                        val summary =
                            generateWeeklySummary(applicationContext, state.weeklyWeatherCode)
                        binding.tvMain.text = summary
                        binding.ivMain.setImageResource(
                            WeatherCodeUtils.getWeatherIconResId(
                                mostFrequentCode?.toInt()!!, true
                            )
                        )
                        binding.tvDayTemp.text =
                            state.weeklyMaxTemperature.average().roundToInt().toString() + degree
                        oneWeekMaxTemperature =
                            state.weeklyMaxTemperature.maxOrNull()?.roundToInt().toString()
                        binding.tvNightTemp.text =
                            state.weeklyMinTemperature.average().roundToInt().toString() + degree
                        oneWeekMinTemperature =
                            state.weeklyMinTemperature.minOrNull()?.roundToInt().toString()
                        binding.dateTime.text = getDateRangeString(state.weeklyTimes)
                        binding.tvTemp.text =
                            oneWeekMaxTemperature + "-" + oneWeekMinTemperature + degree
                    }

                    binding.tvFeelsLike.text = ""
                }
            }

        }

        /* weatherViewModel.isTargetOneWeek.observe(this) { oneWeek ->

             weatherViewModel.formattedDailyDate.removeObservers(this)
             weatherViewModel.temperature.removeObservers(this)
             if (oneWeek) {

             } else {

             }
         }*/
    }

    fun getDateRangeString(dates: List<String>): String {
        if (dates.isEmpty()) return ""

        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val outputDayFormatter = DateTimeFormatter.ofPattern("d")
        val outputMonthFormatter = DateTimeFormatter.ofPattern("MMMM", Locale("tr"))

        val startDate = LocalDate.parse(dates.first(), inputFormatter)
        val endDate = LocalDate.parse(dates.last(), inputFormatter)

        val startDay = outputDayFormatter.format(startDate)
        val endDay = outputDayFormatter.format(endDate)
        val month = outputMonthFormatter.format(endDate)

        return "$startDay-$endDay ${month.replaceFirstChar { it.uppercase() }}"
    }

    fun generateWeeklySummary(context: Context, codes: List<String>): String {
        val codeCounts = codes.groupingBy { it }.eachCount()
        val sortedByFrequency = codeCounts.toList().sortedByDescending { it.second }

        mostFrequentCode = sortedByFrequency.getOrNull(0)?.first
        val secondFrequentCode = sortedByFrequency.getOrNull(1)?.first

        val mainDesc = mostFrequentCode?.toIntOrNull()?.let {
            WeatherCodeUtils.getWeatherDescription(context, it)
        }

        val secondDesc = if (sortedByFrequency.size > 1) {
            sortedByFrequency[1].first.toIntOrNull()?.let {
                WeatherCodeUtils.getWeatherDescription(context, it)
            }
        } else null

        return if (!secondDesc.isNullOrEmpty()) {
            "${getString(R.string.oneWeek_mostly)} $mainDesc, ${getString(R.string.oneWeek_occasionally)} $secondDesc."
        } else {
            " $mainDesc ${getString(R.string.oneWeek_forecasted_week)}"
        }
    }


    private fun updateTabSelection(selectedButtonId: Int) {
        val selectedColor = ContextCompat.getColor(this, R.color.active_button_color)
        val defaultColor = ContextCompat.getColor(this, R.color.default_button_color)

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

    private val mLocationCallback by lazy {
        object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult) {
                val mLastLocation: Location = locationResult.lastLocation!!
                val latitude = mLastLocation.latitude
                val longitude = mLastLocation.longitude
                firebaseAnalytics.logEvent("loadWeatherDataOnboarding", null)
                if (shouldUpdateLocation(latitude, longitude)) {
                    locationViewModel.setLocation(latitude, longitude)
                    weatherViewModel.loadWeatherData(latitude, longitude)
                }
                locationViewModel.setLocation(latitude, longitude)
                mFusedLocationClient.removeLocationUpdates(this)
                runOnUiThread {
                    binding.btnToday.performClick()
                }
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


}