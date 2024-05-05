package com.example.weather.adapter

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.data.api.forecast.ForecastData
import com.squareup.picasso.Picasso
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RecyclerAdapter(val arrayList: ArrayList<ForecastData>) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
    val myArray = arrayList


    // Assuming you have a reference to your bottom sheet


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var temp = itemView.findViewById<TextView>(R.id.card_Temp)
        var state = itemView.findViewById<TextView>(R.id.card_forecastWeather)
        var weatherIcon = itemView.findViewById<ImageView>(R.id.card_icon)
        var intervalTime = itemView.findViewById<TextView>(R.id.intervalTime)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.rv_item_layout, parent, false)
        )

    }


    override fun getItemCount(): Int {
        return myArray.size
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentForecast = myArray[position]
        holder.state.text = currentForecast.weather[0].description
        var temp = currentForecast.main.temp
        temp -= 273.15
        holder.temp.text = temp.toInt().toString() + "°"
        val iconId = currentForecast.weather[0].icon
        val imgUrl = "https://openweathermap.org/img/wn/$iconId.png"
        Picasso.get().load(imgUrl).into(holder.weatherIcon)
        holder.intervalTime.text = displayTime(currentForecast.dt_txt)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun displayTime(time: String): String {
        val input = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val output = DateTimeFormatter.ofPattern("MM-dd HH:mm")
        val dateTime = LocalDateTime.parse(time, input)
        return output.format(dateTime)

    }

}