package com.madkit.weatherapp.views.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.madkit.weatherapp.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

}