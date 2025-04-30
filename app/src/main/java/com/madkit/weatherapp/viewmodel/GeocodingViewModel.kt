package com.madkit.weatherapp.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madkit.weatherapp.domain.repository.GeocodingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GeocodingViewModel @Inject constructor(
    private val geocodingRepository: GeocodingRepository
) : ViewModel() {
    private val _location = MutableLiveData<Location>()
    val location: LiveData<Location> = _location

    private val _shortLocationName = MutableLiveData<String>()
    val shortLocationName: LiveData<String> = _shortLocationName

    fun setLocation(lat: Double, lon: Double) {
        _location.value = Location("").apply {
            latitude = lat
            longitude = lon
        }
        fetchLocationName(lat, lon)
    }

    fun fetchLocationName(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val response = geocodingRepository.getGeocoding(lat, lon)
                val town = response?.address?.town ?: "Unknown Town"
                val state = response?.address?.state ?: "Unknown State"
                _shortLocationName.postValue("$town / $state")
            } catch (e: Exception) {
                _shortLocationName.postValue("Location unavailable")
            }
        }
    }
}