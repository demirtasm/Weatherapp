package com.example.weatherapp.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.adapter.OneWeekItemAdapter
import com.example.weatherapp.databinding.FragmentOneWeekBinding
import com.example.weatherapp.models.OneWeek
import com.example.weatherapp.network.OpenMeteoService
import com.example.weatherapp.network.WeatherService
import com.example.weatherapp.repository.WeatherRepository
import com.example.weatherapp.viewmodel.WeatherViewModel
import com.example.weatherapp.viewmodel.WeatherViewModelFactory


class OneWeekFragment : Fragment() {

    private lateinit var binding: FragmentOneWeekBinding
    private lateinit var weatherViewModel: WeatherViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOneWeekBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val weatherApi = WeatherService.create()
        val meteoApi = OpenMeteoService.create()
        val repository = WeatherRepository(weatherApi, meteoApi)
        val factory = WeatherViewModelFactory(repository)

        weatherViewModel = ViewModelProvider(requireActivity(), factory)[WeatherViewModel::class.java]
        weatherViewModel.setTargetOneWeek(true)



        weatherViewModel.oneWeekTimes.observe(viewLifecycleOwner) { times ->
           // val times = weatherViewModel.oneWeekTimes
            val codes =weatherViewModel.oneWeekWeatherCode.value.orEmpty()
            val tempMax =weatherViewModel.oneWeekMaxTemperature.value.orEmpty()
            val tempMin =weatherViewModel.oneWeekMinTemperature.value.orEmpty()
            val apparentTemperature = weatherViewModel.oneWeekApparentTemperature.value.orEmpty()
            val humidity = weatherViewModel.oneWeekRelativeHumidity.value.orEmpty()

            val oneWeekItems = times.indices.map { i ->
                OneWeek(time = times[i], weatherCode = codes[i], tempMax = tempMax[i], tempMin = tempMin[i], apparentTemperature[i], humidity[i])
            }
            val rv = binding.rvOneWeek
            rv.layoutManager = LinearLayoutManager(rv.context, LinearLayoutManager.VERTICAL, false)
            rv.adapter = OneWeekItemAdapter(requireContext(),oneWeekItems)
        }

    }
}