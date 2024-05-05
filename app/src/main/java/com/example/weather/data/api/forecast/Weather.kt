package com.example.weather.data.api.forecast

data class Weather(
    val description: String,
    val icon: String,
    val id: Int,
    val main: String
)