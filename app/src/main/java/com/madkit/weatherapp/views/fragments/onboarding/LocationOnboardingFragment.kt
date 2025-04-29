package com.madkit.weatherapp.views.fragments.onboarding

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.madkit.weatherapp.utils.PrefsHelper
import com.madkit.weatherapp.databinding.FragmentLocationOnboardingBinding
import com.madkit.weatherapp.viewmodel.LocationViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationRequest
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.madkit.weatherapp.R
import com.madkit.weatherapp.viewmodel.WeatherViewModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocationOnboardingFragment : Fragment() {
    private var binding: FragmentLocationOnboardingBinding? = null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val locationViewModel: LocationViewModel by viewModels({ requireActivity() })
    private val weatherViewModel: WeatherViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLocationOnboardingBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        binding?.btnNext?.setOnClickListener {
            Dexter.withContext(requireContext())
                .withPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if (report.areAllPermissionsGranted()) {
                            binding?.progressRing?.visibility = View.VISIBLE
                            requestLocationData()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.required_permission_toast),
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

            locationViewModel.setLocation(latitude, longitude)
            weatherViewModel.loadWeatherData(latitude, longitude)

            PrefsHelper.setNotFirstTime(requireContext())
            PrefsHelper.setLocationGranted(requireContext())

            binding?.progressRing?.visibility = View.INVISIBLE

            val viewPager = activity?.findViewById<ViewPager2>(R.id.onboardingViewPager)
            viewPager?.currentItem = 3

            mFusedLocationClient.removeLocationUpdates(this)

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}