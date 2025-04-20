package com.madkit.weatherapp.utils

import android.content.Context
import android.content.SharedPreferences

object PrefsHelper {

    private const val PREF_NAME = "weather_prefs"
    private const val KEY_NOT_FIRST_TIME = "not_first_time"
    private const val KEY_LOCATION_GRANTED = "location_granted"
    private const val NOTIF_PERMISSION = "notification_permission"

    fun isFirstTime(context: Context): Boolean {
        val prefs = context.getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("isFirstTime", true)
    }

    fun setNotFirstTime(context: Context) {
        val prefs = context.getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("isFirstTime", false).apply()
    }
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    fun setLocationGranted(context: Context) {
        getPrefs(context).edit().putBoolean(KEY_LOCATION_GRANTED, true).apply()
    }

    fun isLocationGranted(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_LOCATION_GRANTED, false)
    }
    fun setNotificationPermission(context: Context, granted: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(NOTIF_PERMISSION, granted).apply()
    }

    fun isNotificationPermissionGranted(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(NOTIF_PERMISSION, false)
    }
}
