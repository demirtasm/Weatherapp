package com.madkit.weatherapp.adapter

import android.content.Context
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.madkit.weatherapp.R
import com.madkit.weatherapp.domain.model.OneWeek
import com.madkit.weatherapp.utils.WeatherCodeUtils
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
        val cardLayout = itemView.findViewById<CardView>(R.id.cardContent)
        val expandableLayout = itemView.findViewById<LinearLayout>(R.id.expandableLayout)
        val descriptionText: TextView = itemView.findViewById(R.id.textWeatherDesc)
        val toggleArrow = itemView.findViewById<ImageView>(R.id.expandToggle)
        val textApparentTemp = itemView.findViewById<TextView>(R.id.textApparentTemp)
        val textHumidity = itemView.findViewById<TextView>(R.id.textHumidity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OneWeekHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.one_week_item, parent, false)
        return OneWeekHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: OneWeekHolder, position: Int) {
        val item = items[position]
        val degree = WeatherCodeUtils.getUnit(context.resources.configuration.toString())
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
        holder.tempMaxText.text = item.tempMax.roundToInt().toString() + degree

        holder.tempMinText.text = item.tempMin.roundToInt().toString() + degree

        holder.textHumidity.text = "%"+item.humidity.toString()
        holder.textApparentTemp.text = item.apparentTemp.roundToInt().toString() + degree
        holder.cardLayout.setOnClickListener {
            val transitionParent = holder.expandableLayout
            TransitionManager.beginDelayedTransition(transitionParent, AutoTransition())
            val isVisible = holder.expandableLayout.visibility == View.VISIBLE
            holder.expandableLayout.visibility = if (isVisible) View.GONE else View.VISIBLE
            holder.toggleArrow.setImageResource(
                if (isVisible) R.drawable.ic_arrow_drop_down else R.drawable.ic_arrow_drop_up
            )
        }

        holder.imageWeather.setImageResource(WeatherCodeUtils.getWeatherIconResId(item.weatherCode.toInt(), true))
        holder.descriptionText.text =
            WeatherCodeUtils.getWeatherDescription(context, item.weatherCode.toInt())
    }
}