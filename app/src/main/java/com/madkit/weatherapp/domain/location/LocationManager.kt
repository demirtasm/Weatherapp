package com.madkit.weatherapp.domain.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.location.Location
import android.os.Looper
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority

class LocationManager(private val context: Context,
                      private val onResolutionRequired: (IntentSenderRequest) -> Unit
) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private val _locationLiveData = MutableLiveData<Location>()
    val locationLiveData: LiveData<Location> = _locationLiveData

    private val locationRequest = LocationRequest.create().apply {
        priority = Priority.PRIORITY_HIGH_ACCURACY
        fastestInterval = 5000
        interval = 1000
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            location?.let {
                _locationLiveData.postValue(it)
            }
            fusedLocationClient.removeLocationUpdates(this)
        }
    }

    @SuppressLint("MissingPermission")
    fun requestLocation() {
        val settingsClient = LocationServices.getSettingsClient(context)
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val task = settingsClient.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                    onResolutionRequired(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    sendEx.printStackTrace()
                }
            }
        }
    }
}