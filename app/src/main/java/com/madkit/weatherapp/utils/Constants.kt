package com.madkit.weatherapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.madkit.weatherapp.BuildConfig

object Constants {
    const val APP_ID: String = BuildConfig.APP_ID
    const val BASE_URL_OPEN_WEATHER: String = "https://api.openweathermap.org/data/"
    const val BASE_URL_OPEN_METEO: String = "https://api.open-meteo.com/v1/"
    const val METRIC_UNIT: String = "metric"
    const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    const val LOCATION_SETTINGS_REQUEST_CODE = 1002

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {


                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnectedOrConnecting
        }
    }
}