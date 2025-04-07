package com.example.weatherapp.views

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.adapter.OneWeekItemAdapter
import com.example.weatherapp.databinding.FragmentOneWeekBinding
import com.example.weatherapp.models.OneWeek
import com.example.weatherapp.viewmodel.WeatherViewModel


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
        weatherViewModel = ViewModelProvider(requireActivity())[WeatherViewModel::class.java]

        Log.d("taggx", "" + weatherViewModel.weatherCode.value)
        val times = weatherViewModel.oneWeekTimes
        val codes =weatherViewModel.oneWeekWeatherCode.value.orEmpty()
        val tempMax =weatherViewModel.oneWeekMaxTemperature.value.orEmpty()
        val tempMin =weatherViewModel.oneWeekMinTemperature.value.orEmpty()

        weatherViewModel.oneWeekTimes.observe(viewLifecycleOwner) { times ->
            val oneWeekItems = times.indices.map { i ->
                OneWeek(time = times[i], weatherCode = codes[i], tempMax = tempMax[i], tempMin = tempMin[i])
            }
            val rv = binding.rvOneWeek
            rv.layoutManager = LinearLayoutManager(rv.context, LinearLayoutManager.VERTICAL, false)
            rv.adapter = OneWeekItemAdapter(requireContext(),oneWeekItems)
        }

    }
}