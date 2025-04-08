package com.example.weatherapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.models.OneWeek
import com.example.weatherapp.utils.WeatherCodeUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class OneWeekItemAdapter(private val context: Context, private val items: List<OneWeek>) :
    RecyclerView.Adapter<OneWeekItemAdapter.OneWeekHolder>() {

    class OneWeekHolder(items: View) : RecyclerView.ViewHolder(items) {
        val timeText: TextView = itemView.findViewById(R.id.textDay)
        val tempMaxText: TextView = itemView.findViewById(R.id.textTempMax)
        val tempMinText: TextView = itemView.findViewById(R.id.textTempMin)
        val imageWeather: ImageView = itemView.findViewById(R.id.imageWeather)

        val descriptionText: TextView = itemView.findViewById(R.id.textWeatherDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OneWeekHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.one_week_item, parent, false)
        return OneWeekHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: OneWeekHolder, position: Int) {
       val item = items[position]

        val inputDate = LocalDate.parse(item.time)
        val locale = holder.itemView.context.resources.configuration.locales[0]
        val today = LocalDate.now()

        val formattedDate = if (inputDate == today) {
            context.getString(R.string.today)
        } else {
            val outputFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d", locale)
            inputDate.format(outputFormatter)
        }

        holder.timeText.text = formattedDate
        holder.tempMaxText.text = "${item.tempMax.roundToInt()} ${WeatherCodeUtils.getUnit(
            context.resources.configuration.toString()
        )}"
        holder.tempMinText.text = "${item.tempMin.roundToInt()} ${WeatherCodeUtils.getUnit(
            context.resources.configuration.toString()
        )}"
        holder.imageWeather.setImageResource(WeatherCodeUtils.getWeatherIconResId(item.weatherCode.toInt()))
        holder.descriptionText.text = WeatherCodeUtils.getWeatherDescription(context, item.weatherCode.toInt())
    }
}