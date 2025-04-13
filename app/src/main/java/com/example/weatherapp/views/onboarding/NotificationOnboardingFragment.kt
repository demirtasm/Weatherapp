package com.example.weatherapp.views.onboarding

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.weatherapp.PrefsHelper
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentNotificationOnboardingBinding
import com.example.weatherapp.views.WeatherMainActivity


class NotificationOnboardingFragment : Fragment() {
    private var binding: FragmentNotificationOnboardingBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationOnboardingBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding?.btnNext?.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            } else {
                PrefsHelper.setNotificationPermission(requireContext(), true)
                navigateToMain()
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            PrefsHelper.setNotificationPermission(requireContext(), granted)
            PrefsHelper.setNotFirstTime(requireContext())
            PrefsHelper.setNotificationPermission(requireContext(), granted)
            navigateToMain()
        }
    }
    private fun navigateToMain() {
        findNavController().navigate(R.id.action_viewPagerFragment_to_splashFragment)

    }
}