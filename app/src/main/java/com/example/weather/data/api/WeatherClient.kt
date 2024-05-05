package com.example.weather.data.api

import com.example.weather.data.api.current.CurrentWeather
import com.example.weather.data.api.forecast.ForecastWeather
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class WeatherClient {
    companion object {
        private const val Base_URL = "https://api.openweathermap.org/data/2.5/"
        private const val KEY_API = "bdba5d391b4fc6184d85f1d18fc92b2f"
        private var INSTANCE: WeatherClient? = null
        fun getInstance(): WeatherClient? {
            if (INSTANCE == null) {
                INSTANCE = WeatherClient()
            }
            return INSTANCE
        }
    }

    val retrofit: Retrofit = Retrofit.Builder().baseUrl(Base_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val apiInterface = retrofit.create(ApiInterface::class.java)

    fun getWeather(newCity: String): Call<CurrentWeather> {
        return apiInterface.getWeather(newCity, "metric", KEY_API)
    }

    fun getForecast(newCity: String): Call<ForecastWeather> {
        return apiInterface.getForecast(newCity, KEY_API)
    }


}