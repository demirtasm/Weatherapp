package com.madkit.weatherapp.views.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.madkit.weatherapp.BuildConfig
import com.madkit.weatherapp.utils.PrefsHelper
import com.madkit.weatherapp.R
import com.madkit.weatherapp.databinding.FragmentSplashBinding
import com.madkit.weatherapp.viewmodel.WeatherViewModel
import com.madkit.weatherapp.domain.location.LocationManager
import com.madkit.weatherapp.utils.Constants.LOCATION_PERMISSION_REQUEST_CODE
import com.madkit.weatherapp.viewmodel.GeocodingViewModel
import com.madkit.weatherapp.viewmodel.LocationViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : Fragment() {

    private var binding: FragmentSplashBinding? = null
    private var isNavigated = false
    private var meteoLoaded = false
    private var currentWeatherLoaded = false
    private lateinit var resolutionLauncher: ActivityResultLauncher<IntentSenderRequest>
    private val locationViewModel: LocationViewModel by activityViewModels()
    private val geocodingViewModel: GeocodingViewModel by activityViewModels()
    private val weatherViewModel: WeatherViewModel by activityViewModels()
    private lateinit var locationManager: LocationManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resolutionLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                locationManager.requestLocation()
            } else {
                findNavController().navigate(R.id.action_splashFragment_to_viewPagerFragment)
            }
        }
        locationManager = LocationManager(requireContext()) { intentSenderRequest ->
            resolutionLauncher.launch(intentSenderRequest)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding?.root?.setBackgroundResource(R.drawable.toolbar_gradient_color)
        val versionName = BuildConfig.VERSION_NAME
        binding?.versionText?.text = "v$versionName"
        requireActivity().window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

        requireActivity().window.statusBarColor = Color.TRANSPARENT

        if (PrefsHelper.isFirstTime(requireContext())) {
            findNavController().navigate(R.id.action_splashFragment_to_viewPagerFragment)
        } else if (!hasLocationPermission()) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            startLocationFlow()
        }
    }
    private fun startLocationFlow() {
        observeWeatherData(weatherViewModel)
        locationManager.locationLiveData.observe(viewLifecycleOwner) { location ->
            location?.let {
                locationViewModel.setLocation(it.latitude, it.longitude)
                weatherViewModel.loadWeatherData(it.latitude, it.longitude)
                geocodingViewModel.setLocation(it.latitude, it.longitude)
            }
        }
        locationManager.requestLocation()
    }
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
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
            findNavController().navigate(R.id.action_splashFragment_to_weatherMainFragment)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.status_bar_color)

    }

}


