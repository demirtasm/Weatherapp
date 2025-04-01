package com.example.weatherapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object Constants {
//https://api.open-meteo.com/v1/forecast?latitude=40.9751592&longitude=27.4933325&hourly=temperature_2m,relative_humidity_2m,precipitation,weather_code
    const val APP_ID: String  =  BuildConfig.APP_ID
    const val BASE_URL_OPEN_WEATHER:String = "https://api.openweathermap.org/data/"
    const val BASE_URL_OPEN_METEO:String = "https://api.open-meteo.com/v1/"
    const val METRIC_UNIT:String = "metric"

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