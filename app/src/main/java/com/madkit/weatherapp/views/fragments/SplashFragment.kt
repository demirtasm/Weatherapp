package com.madkit.weatherapp.views.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
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
import com.google.android.gms.common.api.ResolvableApiException
import com.madkit.weatherapp.BuildConfig
import com.madkit.weatherapp.utils.PrefsHelper
import com.madkit.weatherapp.R
import com.madkit.weatherapp.databinding.FragmentSplashBinding
import com.madkit.weatherapp.viewmodel.WeatherViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.SettingsClient
import com.madkit.weatherapp.utils.Constants.LOCATION_PERMISSION_REQUEST_CODE
import com.madkit.weatherapp.viewmodel.LocationViewModel
import com.madkit.weatherapp.views.activities.WeatherMainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : Fragment() {

    private var binding: FragmentSplashBinding? = null
    private var isNavigated = false
    private var meteoLoaded = false
    private var currentWeatherLoaded = false
    private lateinit var resolutionLauncher: ActivityResultLauncher<IntentSenderRequest>
    private val locationViewModel: LocationViewModel by activityViewModels()
    private val weatherViewModel: WeatherViewModel by viewModels()

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
                fetchLocationAndObserveData()
            } else {
                findNavController().navigate(R.id.action_splashFragment_to_viewPagerFragment)
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding?.root?.setBackgroundResource(R.drawable.toolbar_gradient_color)
        val versionName = BuildConfig.VERSION_NAME
        binding?.versionText?.text = "v$versionName"
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        if (PrefsHelper.isFirstTime(requireContext())) {
            findNavController().navigate(R.id.action_splashFragment_to_viewPagerFragment)
        } else if (!hasLocationPermission()) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            fetchLocationAndObserveData()
        }
    }
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocationAndObserveData() {
        observeWeatherData(weatherViewModel)
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    locationViewModel.setLocation(location.latitude, location.longitude)
                    weatherViewModel.loadWeatherData(location.latitude, location.longitude)
                    observeWeatherData(weatherViewModel)
                } else {
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        object : LocationCallback() {
                            override fun onLocationResult(result: LocationResult) {
                                fusedLocationClient.removeLocationUpdates(this)
                                result.lastLocation?.let { location ->
                                    locationViewModel.setLocation(location.latitude, location.longitude)
                                    weatherViewModel.loadWeatherData(location.latitude, location.longitude)
                                    observeWeatherData(weatherViewModel)
                                }
                            }
                        },
                        Looper.getMainLooper()
                    )
                }
            }
        }
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                    resolutionLauncher.launch(intentSenderRequest)
                } catch (e: Exception) {
                    e.printStackTrace()
                    openAppSettings()
                }
            } else {
                openAppSettings()
            }
        }

    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireContext().packageName, null)
        }
        startActivity(intent)
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


