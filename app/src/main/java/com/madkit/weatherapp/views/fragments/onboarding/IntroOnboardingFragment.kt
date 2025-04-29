package com.madkit.weatherapp.views.fragments.onboarding

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.madkit.weatherapp.R
import com.madkit.weatherapp.databinding.FragmentIntroOnboardingBinding
import com.madkit.weatherapp.viewmodel.LocationViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IntroOnboardingFragment : Fragment() {
    private var binding: FragmentIntroOnboardingBinding? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentIntroOnboardingBinding.inflate(inflater, container, false)

        requireActivity().window.apply {
            decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
            statusBarColor = Color.TRANSPARENT
        }


        val viewPager = activity?.findViewById<ViewPager2>(R.id.onboardingViewPager)
        binding?.btnGetStarted?.setOnClickListener{
            viewPager?.currentItem = 1
        }
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}