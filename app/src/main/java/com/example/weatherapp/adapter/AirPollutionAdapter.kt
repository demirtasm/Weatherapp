package com.example.weatherapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.models.AirPollution


class AirPollutionAdapter(private val items: List<AirPollution>): RecyclerView.Adapter<AirPollutionAdapter.AirPollutionHolder>() {

    class AirPollutionHolder(items:View): RecyclerView.ViewHolder(items){
        val timeText: TextView = itemView.findViewById(R.id.textTime)
        val percentText: TextView = itemView.findViewById(R.id.textPercent)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AirPollutionHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.air_pollution_item, parent, false)
        return AirPollutionHolder(view)

    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: AirPollutionHolder, position: Int) {
        val item = items[position]
        holder.timeText.text = item.time
        holder.percentText.text = "${item.aqi}%"
        holder.progressBar.progress = item.aqi
    }
}