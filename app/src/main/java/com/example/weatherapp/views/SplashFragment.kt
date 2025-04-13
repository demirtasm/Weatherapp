package com.example.weatherapp.views

import android.annotation.SuppressLint
import android.content.Intent
import android.health.connect.datatypes.ExerciseRoute
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.weatherapp.PrefsHelper
import com.example.weatherapp.R
import com.example.weatherapp.WeatherApplication
import com.example.weatherapp.databinding.FragmentSplashBinding
import com.example.weatherapp.viewmodel.WeatherViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices


class SplashFragment : Fragment() {

    private var binding: FragmentSplashBinding? = null
    private var isNavigated = false
    private var meteoLoaded = false
    private var currentWeatherLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding?.root?.setBackgroundResource(R.drawable.toolbar_gradient_color)

        if (PrefsHelper.isFirstTime(requireContext()) || !PrefsHelper.isLocationGranted(requireContext())) {
            findNavController().navigate(R.id.action_splashFragment_to_viewPagerFragment)
        } else {
            fetchLocationAndObserveData()
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocationAndObserveData() {
        val app = requireActivity().application as WeatherApplication
        val locationViewModel = app.locationViewModel
        val weatherViewModel = ViewModelProvider(this, app.weatherViewModelFactory)[WeatherViewModel::class.java]
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                fusedLocationClient.removeLocationUpdates(this)
                result.lastLocation?.let { location ->
                    locationViewModel.setLocation(location.latitude, location.longitude)
                    weatherViewModel.loadWeatherData(location.latitude, location.longitude)
                    observeWeatherData(weatherViewModel)
                }
            }
        }, Looper.getMainLooper())
    }

    private fun observeWeatherData(weatherViewModel: WeatherViewModel) {
        weatherViewModel.meteoData.observe(viewLifecycleOwner) {
            meteoLoaded = it != null
            tryNavigate()
        }
        weatherViewModel.currentWeather.observe(viewLifecycleOwner) {
            currentWeatherLoaded = it != null
            tryNavigate()
        }
    }

    private fun tryNavigate() {
        if (meteoLoaded && currentWeatherLoaded && !isNavigated) {
            isNavigated = true
            startActivity(Intent(requireContext(), WeatherMainActivity::class.java))
            requireActivity().finish()
        }
    }
}


