package com.madkit.weatherapp.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.madkit.weatherapp.adapter.OneWeekItemAdapter
import com.madkit.weatherapp.databinding.FragmentOneWeekBinding
import com.madkit.weatherapp.domain.model.OneWeek
import com.madkit.weatherapp.utils.DayType
import com.madkit.weatherapp.viewmodel.WeatherViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OneWeekFragment : Fragment() {

    private lateinit var binding: FragmentOneWeekBinding
    private val weatherViewModel: WeatherViewModel by viewModels({ requireActivity() })


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOneWeekBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        weatherViewModel.setTargetDayType(DayType.WEEKLY)

            weatherViewModel.weeklyUIState.observe(requireActivity()) { state ->
                val codes = state.weeklyWeatherCode
                val tempMax =state.weeklyMaxTemperature
                val tempMin =state.weeklyMinTemperature
                val humidity = state.weeklyRelativeHumidity
                val apparentTemperature = state.weeklyApparentTemperature
                val oneWeekItems = state.weeklyTimes.indices.map { i ->
                    OneWeek(time = state.weeklyTimes[i], weatherCode = codes[i], tempMax = tempMax[i], tempMin = tempMin[i], apparentTemperature[i], humidity[i])
                }
                val rv = binding.rvOneWeek
                rv.layoutManager = LinearLayoutManager(rv.context, LinearLayoutManager.VERTICAL, false)
                rv.adapter = OneWeekItemAdapter(requireContext(),oneWeekItems)
            }




    }
}