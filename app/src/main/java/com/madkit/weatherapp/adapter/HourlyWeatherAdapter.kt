package com.madkit.weatherapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.madkit.weatherapp.R
import com.madkit.weatherapp.models.HourlyWeather
import com.madkit.weatherapp.utils.WeatherCodeUtils
import kotlin.math.roundToInt

class HourlyWeatherAdapter(private val items: List<HourlyWeather>,  private val currentHour: String) :
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
            WeatherCodeUtils.getWeatherIconResId(item.weatherCode)
        )
        if (item.time.startsWith(currentHour)) {
            holder.itemView.setBackgroundResource(R.drawable.bg_selected_hourly_item)
        } else {
            holder.itemView.setBackgroundResource(0)
        }
    }
}