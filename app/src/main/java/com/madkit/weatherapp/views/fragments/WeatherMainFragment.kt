package com.madkit.weatherapp.views.fragments

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.madkit.weatherapp.R
import com.madkit.weatherapp.data.model.OpenMeteoResponse
import com.madkit.weatherapp.data.model.WeatherResponse
import com.madkit.weatherapp.databinding.FragmentWeatherMainBinding
import com.madkit.weatherapp.utils.DayType
import com.madkit.weatherapp.utils.WeatherCodeUtils
import com.madkit.weatherapp.viewmodel.GeocodingViewModel
import com.madkit.weatherapp.viewmodel.LocationViewModel
import com.madkit.weatherapp.viewmodel.WeatherViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.text.compareTo

@AndroidEntryPoint
class WeatherMainFragment : Fragment() {
    private lateinit var binding: FragmentWeatherMainBinding
    private val weatherViewModel: WeatherViewModel by activityViewModels()
    private val locationViewModel: LocationViewModel by activityViewModels()
    private val geocodingViewModel: GeocodingViewModel by activityViewModels()
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var mostFrequentCode: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWeatherMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
        observeWeatherData()
        setupUI()
        binding.btnToday.performClick()
    }
    private fun setupUI() {
        val nestedNavController = childFragmentManager.findFragmentById(R.id.fragmentContainerView)?.findNavController()

        binding.btnToday.setOnClickListener {
            weatherViewModel.setTargetDayType(DayType.TODAY)
            updateTabSelection(R.id.btnToday)
            nestedNavController?.navigate(R.id.todayFragment)
        }

        binding.btnTomorrow.setOnClickListener {
            weatherViewModel.setTargetDayType(DayType.TOMORROW)
            updateTabSelection(R.id.btnTomorrow)
            nestedNavController?.navigate(R.id.tomorrowFragment)
        }

        binding.btn1Week.setOnClickListener {
            weatherViewModel.setTargetDayType(DayType.WEEKLY)
            updateTabSelection(R.id.btn1Week)
            nestedNavController?.navigate(R.id.oneWeekFragment)
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
    private fun updateTabSelection(selectedButtonId: Int) {
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.active_button_color)
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.default_button_color)

        binding.btnToday.backgroundTintList = ColorStateList.valueOf(defaultColor)
        binding.btnTomorrow.backgroundTintList = ColorStateList.valueOf(defaultColor)
        binding.btn1Week.backgroundTintList = ColorStateList.valueOf(defaultColor)

        when (selectedButtonId) {
            R.id.btnToday -> {
                binding.btnToday.backgroundTintList = ColorStateList.valueOf(selectedColor)
            }

            R.id.btnTomorrow -> {
                binding.btnTomorrow.backgroundTintList = ColorStateList.valueOf(selectedColor)
            }

            R.id.btn1Week -> {
                binding.btn1Week.backgroundTintList = ColorStateList.valueOf(selectedColor)

            }
        }
    }

    private fun observeWeatherData() {
        weatherViewModel.meteoData.observe(viewLifecycleOwner) { meteo ->
            meteo?.let {
                setUpMeteoUI(it)
            }
        }

        weatherViewModel.currentWeather.observe(viewLifecycleOwner) { currentWeather ->
            currentWeather?.let {
                setUpCurrentWeatherUI(it)
            }
        }
    }
    private fun setUpCurrentWeatherUI(weatherList: WeatherResponse) {
        geocodingViewModel.shortLocationName.observe(viewLifecycleOwner) { locationName ->
            binding?.tvName?.text = locationName
        }
       // binding.tvName.text = " ${weatherList.name}, ${weatherList.sys.country}"
    }
    private fun setUpMeteoUI(meteo: OpenMeteoResponse?) {
        val degree = WeatherCodeUtils.getUnit(requireContext().resources.configuration.toString())
        var oneWeekMaxTemperature: String? = null
        var oneWeekMinTemperature: String? = null
        weatherViewModel.targetDayType.observe(viewLifecycleOwner) { type ->
            weatherViewModel.formattedDailyDate.removeObservers(this)
            when (type) {
                DayType.TOMORROW -> {

                    binding.tvDayText.text = getString(R.string.day_txt)
                    binding.tvNightText.text = getString(R.string.night_txt)
                    weatherViewModel.hourlyUIState.observe(viewLifecycleOwner) { state ->
                        binding.tvTemp.text = state.hourlyTemperature + degree
                    }
                    weatherViewModel.dailyUIState.observe(viewLifecycleOwner) { state ->
                        binding.tvMain.text = WeatherCodeUtils.getWeatherDescription(
                            requireContext(), state.weatherCode.toInt()
                        )
                        binding.ivMain.setImageResource(
                            WeatherCodeUtils.getWeatherIconResId(
                                state.weatherCode.toInt(), true
                            )
                        )
                        binding.tvDayTemp.text = state.temperature2mMax + degree
                        binding.tvNightTemp.text = state.temperature2mMin + degree

                        binding.tvFeelsLike.text =
                            "${getString(R.string.feels_like_txt)} ${state.apparentTemperature} ${degree}"
                    }
                    weatherViewModel.formattedDailyDate.observe(viewLifecycleOwner) { it ->
                        binding.dateTime.text = it
                    }
                }

                DayType.TODAY -> {

                    binding.tvDayText.text = getString(R.string.day_txt)
                    binding.tvNightText.text = getString(R.string.night_txt)
                    weatherViewModel.hourlyUIState.observe(viewLifecycleOwner) { state ->
                        binding.tvTemp.text = state.hourlyTemperature + degree
                    }
                    weatherViewModel.dailyUIState.observe(viewLifecycleOwner) { state ->
                        binding.tvMain.text = WeatherCodeUtils.getWeatherDescription(
                            requireContext(), state.weatherCode.toInt()
                        )
                        val currentHour = LocalTime.now().hour

                        val hourlyIsDayList =
                            weatherViewModel.hourlyUIState.value?.hourlyIsDaY ?: listOf()

                        val matchedIndex =
                            weatherViewModel.hourlyUIState.value?.hourlyAllTemperature?.indices?.minByOrNull { index ->
                                Math.abs(index - currentHour)
                            } ?: 0

                        val isDayNow = hourlyIsDayList.getOrNull(matchedIndex) == 1


                        binding.ivMain.setImageResource(
                            WeatherCodeUtils.getWeatherIconResId(
                                state.weatherCode.toInt(), isDayNow
                            )
                        )

                        binding.tvDayTemp.text = state.temperature2mMax + degree
                        binding.tvNightTemp.text = state.temperature2mMin + degree

                        binding.tvFeelsLike.text =
                            "${getString(R.string.feels_like_txt)} ${state.apparentTemperature} ${degree}"
                    }
                    weatherViewModel.formattedDailyDate.observe(viewLifecycleOwner) { it ->
                        binding.dateTime.text = it
                    }
                }

                DayType.WEEKLY -> {
                    binding.tvDayText.text = getString(R.string.daytime_weekly)
                    binding.tvNightText.text = getString(R.string.nighttime_weekly)

                    weatherViewModel.weeklyUIState.observe(viewLifecycleOwner) { state ->
                        val summary =
                            generateWeeklySummary(requireContext(), state.weeklyWeatherCode)
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


}