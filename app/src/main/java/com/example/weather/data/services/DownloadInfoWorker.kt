package com.example.weather.data.services

import android.content.Context
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.example.weather.data.api.WeatherClient
import com.example.weather.data.api.current.CurrentWeather
import com.example.weather.data.api.forecast.ForecastWeather
import com.example.weather.ui.main.MainActivity
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors

class DownloadInfoWorker(context: Context, parameters: WorkerParameters) :
    ListenableWorker(context, parameters) {
    var firstResponse: Boolean = false
    var secondResponse: Boolean = false


    companion object {
        const val KEY_CURRENT_WEATHER = "KEY_CURRENT_WEATHER"
        const val KEY_FORECAST_WEATHER = "KEY_FORECAST_WEATHER"
        lateinit var currentWeather: CurrentWeather
        lateinit var forecastWeather: ForecastWeather
        var isItWrongName: Boolean = false
        lateinit var nullResult: ListenableFuture<Result>

    }

    override fun startWork(): ListenableFuture<Result> {
        return CallbackToFutureAdapter.getFuture { completer ->
            val futures = mutableListOf<ListenableFuture<Result>>()
            val cityName = inputData.getString(MainActivity.KEY_TO_GET_CITY)
            isItWrongName = false

            if (cityName == null) {
                completer.set(
                    Result.failure(
                        Data.Builder().putString("error", "City name is null").build()
                    )
                )
                return@getFuture nullResult
            }

            val forecastFuture = CallbackToFutureAdapter.getFuture { forecastCompleter ->
                WeatherClient.getInstance()?.getForecast(cityName)
                    ?.enqueue(object : Callback<ForecastWeather> {
                        override fun onResponse(
                            call: Call<ForecastWeather>,
                            response: Response<ForecastWeather>
                        ) {
                            if (response.isSuccessful && response.body() != null) {
                                firstResponse = true
                                forecastWeather = response.body()!!
                                isItWrongName = false
                                val forecastData = Data.Builder()
                                    .putString(KEY_FORECAST_WEATHER, "success")
                                    .build()
                                forecastCompleter.set(Result.success(forecastData))
                            } else {
                                val errorData = Data.Builder()
                                    .putString("error", "HTTP error")
                                    .putString("errorMessage", response.message())
                                    .build()
                                isItWrongName = true
                                forecastCompleter.set(Result.failure(errorData))
                            }
                        }

                        override fun onFailure(call: Call<ForecastWeather>, t: Throwable) {
                            val errorData = Data.Builder()
                                .putString("error", "Network failure")
                                .putString("errorMessage", t.message)
                                .build()
                            forecastCompleter.set(Result.failure(errorData))
                        }
                    })
            }

            futures.add(forecastFuture)

            val currentWeatherFuture =
                CallbackToFutureAdapter.getFuture<Result> { currentCompleter ->
                    WeatherClient.getInstance()?.getWeather(cityName)
                        ?.enqueue(object : Callback<CurrentWeather> {
                            override fun onResponse(
                                call: Call<CurrentWeather>,
                                response: Response<CurrentWeather>
                            ) {
                                if (response.isSuccessful && response.body() != null) {
                                    secondResponse = true
                                    currentWeather = response.body()!!
                                    val currentWeatherData = Data.Builder()
                                        .putString(KEY_CURRENT_WEATHER, "success")
                                        .build()
                                    currentCompleter.set(Result.success(currentWeatherData))
                                } else {
                                    val errorData = Data.Builder()
                                        .putString("error", "HTTP error")
                                        .putString("errorMessage", response.message())
                                        .build()
                                    currentCompleter.set(Result.failure(errorData))
                                }
                            }

                            override fun onFailure(call: Call<CurrentWeather>, t: Throwable) {
                                val errorData = Data.Builder()
                                    .putString("error", "Network failure")
                                    .putString("errorMessage", t.message)
                                    .build()
                                currentCompleter.set(Result.failure(errorData))
                            }
                        })
                }

            futures.add(currentWeatherFuture)

            // Combine all futures to ensure completion of both calls
            Futures.whenAllComplete(futures).call({
                if (futures.all { it.isDone } && firstResponse && secondResponse) {
                    completer.set(
                        Result.success(
                            Data.Builder()
                                .putString(KEY_FORECAST_WEATHER, "success")
                                .putString(KEY_CURRENT_WEATHER, "success")
                                .build()
                        )
                    )
                } else {
                    if (isItWrongName)
                        completer.set(
                            Result.failure(
                                Data.Builder().putString("error", "HTTP error")
                                    .build()
                            )
                        ) else completer.set(
                        Result.failure(
                            Data.Builder().putString("error", "One or more network calls failed")
                                .build()
                        )
                    )
                }
            }, Executors.newSingleThreadExecutor())
        }
    }
}
