package com.madkit.weatherapp.views.fragments.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.madkit.weatherapp.utils.PrefsHelper
import com.madkit.weatherapp.R
import com.madkit.weatherapp.WeatherWorker
import com.madkit.weatherapp.databinding.FragmentNotificationOnboardingBinding
import java.util.Calendar
import java.util.concurrent.TimeUnit


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
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            } else {
                PrefsHelper.setNotificationPermission(requireContext(), true)
                navigateToMain()
            }
        }
        requireActivity().window.apply {
            decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
            statusBarColor = Color.TRANSPARENT
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            PrefsHelper.setNotificationPermission(requireContext(), granted)
            PrefsHelper.setNotFirstTime(requireContext())
            if (granted) {
                PrefsHelper.setNotificationPermission(requireContext(), granted)
                PrefsHelper.setNotFirstTime(requireContext())
                scheduleWeatherWorker(9, 0)
            }
            navigateToMain()
        }

    }
    private fun navigateToMain() {
        findNavController().navigate(R.id.action_viewPagerFragment_to_splashFragment)

    }

    private fun scheduleWeatherWorker(hour: Int, minute: Int) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DATE, 1)
        }

        val delay = target.timeInMillis - now.timeInMillis

        val request = PeriodicWorkRequestBuilder<WeatherWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            "daily_weather",
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }


}