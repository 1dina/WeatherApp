package com.example.weather.data

import android.content.Context
import android.content.SharedPreferences
import com.example.weather.data.api.current.CurrentWeather
import com.example.weather.data.api.forecast.ForecastWeather
import com.google.gson.Gson

class SharedPreferenceManager(context: Context) {

    private val PREFS_NAME = "sharedpref"
    val sharedPref: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveLastWeather(lastWeather: CurrentWeather,lastForecast : ForecastWeather) {
        sharedPref.edit().putString("weather", Gson().toJson(lastWeather))
            .putString("forecast", Gson().toJson(lastForecast)).apply()
    }

    internal fun getWeather(): CurrentWeather? {
        val data = sharedPref.getString("weather", null)
        if (data == null) {
            return null
        }
        return Gson().fromJson(data, CurrentWeather::class.java)
    }
    internal fun getForecast(): ForecastWeather? {
        val data = sharedPref.getString("forecast", null)
        if (data == null) {
            return null
        }
        return Gson().fromJson(data, ForecastWeather::class.java)
    }

}