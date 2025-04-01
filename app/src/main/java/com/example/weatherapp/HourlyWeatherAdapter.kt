package com.example.weatherapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.models.HourlyWeather
import kotlin.math.roundToInt

class HourlyWeatherAdapter(private val items: List<HourlyWeather>) :
    RecyclerView.Adapter<HourlyWeatherAdapter.HourlyViewHolder>() {
    class HourlyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeText: TextView = itemView.findViewById(R.id.textTime)
        val tempText: TextView = itemView.findViewById(R.id.textTemp)
        val rainText: TextView = itemView.findViewById(R.id.textRain)
        val weatherCodeImage: ImageView = itemView.findViewById(R.id.iv_weather_code)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hourly_weather, parent, false)
        return HourlyViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {
        val item = items[position]
        holder.timeText.text = item.time
        holder.rainText.text = "%${item.precipitation.toString()}"
        holder.tempText.text = "${item.temperature.roundToInt()}°C"
        holder.weatherCodeImage.setImageResource(
            when (item.weatherCode) {
                0 -> R.drawable.ic_clear_day
                1 -> R.drawable.ic_mainly_clear_day
                2 -> R.drawable.ic_cloudy_day
                3 -> R.drawable.ic_overcast_day
                45, 48 -> R.drawable.ic_haze_day
                51 -> R.drawable.ic_rain_day
                53,55 -> R.drawable.ic_dense_rainy_day
                56 -> R.drawable.ic_clear_day
                57 -> R.drawable.ic_clear_day
                61 -> R.drawable.ic_clear_day
                63 -> R.drawable.ic_clear_day
                65->R.drawable.ic_clear_day
                66,67->R.drawable.ic_freezing_rain_day
                71->R.drawable.ic_clear_day
                73->R.drawable.ic_clear_day
                75->R.drawable.ic_clear_day
                77->R.drawable.ic_clear_day
                80 -> R.drawable.ic_clear_day
                81 -> R.drawable.ic_clear_day
                82 -> R.drawable.ic_clear_day
                85 -> R.drawable.ic_clear_day
                86 -> R.drawable.ic_clear_day
                else -> R.drawable.ic_clear_day
            }
        )

    }
}