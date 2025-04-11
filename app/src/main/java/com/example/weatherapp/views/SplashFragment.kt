package com.example.weatherapp.views

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.weatherapp.PrefsHelper
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentSplashBinding


class SplashFragment : Fragment() {

    private var binding: FragmentSplashBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.root?.setBackgroundResource(R.drawable.toolbar_gradient_color)
        Handler(Looper.getMainLooper()).postDelayed({
            if (PrefsHelper.isFirstTime(requireContext())) {
                findNavController().navigate(R.id.action_splashFragment_to_viewPagerFragment)
            } else if (!PrefsHelper.isLocationGranted(requireContext())) {
                findNavController().navigate(R.id.action_splashFragment_to_viewPagerFragment)
            } else {
                //binding?.lottieSplash?.postDelayed({
                    val intent = Intent(requireContext(), WeatherMainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
               // }, 1000)

            }
        }, 2000)
    }
}

