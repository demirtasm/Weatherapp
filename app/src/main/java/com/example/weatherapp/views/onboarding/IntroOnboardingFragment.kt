package com.example.weatherapp.views.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentIntroOnboardingBinding
import com.example.weatherapp.viewmodel.LocationViewModel
import com.google.android.gms.location.FusedLocationProviderClient


class IntroOnboardingFragment : Fragment() {
    private var binding: FragmentIntroOnboardingBinding? = null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var locationViewModel: LocationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentIntroOnboardingBinding.inflate(inflater, container, false)
        val viewPager = activity?.findViewById<ViewPager2>(R.id.onboardingViewPager)
        binding?.btnGetStarted?.setOnClickListener{
            viewPager?.currentItem = 1
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        locationViewModel = ViewModelProvider(this)[LocationViewModel::class.java]

    }

}