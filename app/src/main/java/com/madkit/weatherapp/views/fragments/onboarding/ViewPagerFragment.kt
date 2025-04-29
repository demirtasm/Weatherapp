package com.madkit.weatherapp.views.fragments.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.madkit.weatherapp.adapter.ViewPagerAdapter
import com.madkit.weatherapp.databinding.FragmentViewPagerBinding


class ViewPagerFragment : Fragment() {
    private var binding: FragmentViewPagerBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentViewPagerBinding.inflate(inflater, container, false)
        val fragmentList = arrayListOf<Fragment>(
IntroOnboardingFragment(),LocationOnboardingFragment(), NotificationOnboardingFragment()
        )
        val adapter = ViewPagerAdapter(fragmentList, childFragmentManager, lifecycle)
        binding?.onboardingViewPager?.adapter = adapter
        binding?.onboardingViewPager?.isUserInputEnabled = false
        return binding?.root
    }

}