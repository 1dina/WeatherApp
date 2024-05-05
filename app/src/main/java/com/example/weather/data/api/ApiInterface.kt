package com.example.weather.data.api

import com.example.weather.data.api.current.CurrentWeather
import com.example.weather.data.api.forecast.ForecastWeather
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface  {
    @GET ("weather?")
    fun getWeather(
        @Query ("q") city : String,
        @Query ("units") units : String,
        @Query ("appid") apiKey :String
    ) : Call<CurrentWeather>

    @GET ("forecast?")
    fun getForecast(
        @Query("q") city: String,
        @Query("appid") apiKey: String
    ) : Call <ForecastWeather>

}