package com.madkit.weatherapp.views.fragments.onboarding

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Color
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.madkit.weatherapp.R
import com.madkit.weatherapp.viewmodel.WeatherViewModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.madkit.weatherapp.LocationManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocationOnboardingFragment : Fragment() {
    private var binding: FragmentLocationOnboardingBinding? = null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val locationViewModel: LocationViewModel by viewModels({ requireActivity() })
    private val weatherViewModel: WeatherViewModel by viewModels({ requireActivity() })
    private lateinit var locationManager: LocationManager
    private lateinit var resolutionLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLocationOnboardingBinding.inflate(inflater, container, false)
        return binding?.root
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resolutionLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                locationManager.requestLocation()
            }
        }

        locationManager = LocationManager(requireContext()) { intentSenderRequest ->
            resolutionLauncher.launch(intentSenderRequest)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.apply {
            decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
            statusBarColor = Color.TRANSPARENT
        }

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
                            startLocationFlow()

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


   private fun startLocationFlow() {
       locationManager.locationLiveData.observe(viewLifecycleOwner) { location ->
           location?.let {
               locationViewModel.setLocation(it.latitude, it.longitude)
               weatherViewModel.loadWeatherData(it.latitude, it.longitude)

               PrefsHelper.setNotFirstTime(requireContext())
               PrefsHelper.setLocationGranted(requireContext())

               binding?.progressRing?.visibility = View.INVISIBLE

               val viewPager = activity?.findViewById<ViewPager2>(R.id.onboardingViewPager)
               viewPager?.currentItem = 3
           }
       }
       locationManager.requestLocation()
   }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}