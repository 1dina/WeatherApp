package com.example.weather.data.api.forecast

data class ForecastWeather(
    val city: City,
    val cnt: Int,
    val cod: String,
    val list: List<ForecastData>,
    val message: Int
)