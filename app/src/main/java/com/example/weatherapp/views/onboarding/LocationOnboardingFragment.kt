package com.example.weatherapp.views.onboarding

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.weatherapp.PrefsHelper
import com.example.weatherapp.databinding.FragmentLocationOnboardingBinding
import com.example.weatherapp.viewmodel.LocationViewModel
import com.example.weatherapp.views.WeatherMainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationRequest
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.example.weatherapp.R
import com.example.weatherapp.network.OpenMeteoService
import com.example.weatherapp.network.WeatherService
import com.example.weatherapp.repository.WeatherRepository
import com.example.weatherapp.viewmodel.WeatherViewModel
import com.example.weatherapp.viewmodel.WeatherViewModelFactory
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener


class LocationOnboardingFragment : Fragment() {
    private var binding: FragmentLocationOnboardingBinding? = null
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var weatherViewModel: WeatherViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLocationOnboardingBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationViewModel = ViewModelProvider(requireActivity())[LocationViewModel::class.java]
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val weatherApi = WeatherService.create()
        val meteoApi = OpenMeteoService.create()
        val repository = WeatherRepository(weatherApi, meteoApi)
        val factory = WeatherViewModelFactory(repository)
        weatherViewModel = ViewModelProvider(this, factory)[WeatherViewModel::class.java]

        binding?.btnNext?.setOnClickListener {
            Dexter.withContext(requireContext())
                .withPermissions(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if (report.areAllPermissionsGranted()) {
                            requestLocationData()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Location permission is required to proceed.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        token?.continuePermissionRequest()
                    }
                }).check()
        }

    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 0
        }

        mFusedLocationClient.requestLocationUpdates(
            locationRequest,
            mLocationCallback,
            Looper.getMainLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation!!
            val latitude = mLastLocation.latitude
            val longitude = mLastLocation.longitude
            Log.e("TAGX", "LATİTUDE: ${latitude} ,Longıtude: ${longitude}")
            locationViewModel.setLocation(latitude, longitude)
            weatherViewModel.loadWeatherData(latitude, longitude)
            locationViewModel.setLocation(latitude, longitude)
            PrefsHelper.setNotFirstTime(requireContext())

            val viewPager = activity?.findViewById<ViewPager2>(R.id.onboardingViewPager)

            viewPager?.currentItem = 2

            mFusedLocationClient.removeLocationUpdates(this)
            PrefsHelper.setNotFirstTime(requireContext())
            PrefsHelper.setLocationGranted(requireContext())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}