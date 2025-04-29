package com.madkit.weatherapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocationViewModel  @Inject constructor(): ViewModel() {
    private val _location = MutableLiveData<Pair<Double, Double>>()
    val location: LiveData<Pair<Double, Double>> = _location

    fun setLocation(lat: Double, lon: Double) {
        _location.value = Pair(lat, lon)
    }

    fun getLatitude(): Double? {
        return _location.value?.first
    }

    fun getLongitude(): Double? {
        return _location.value?.second
    }

}