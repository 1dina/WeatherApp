package com.example.weather.ui.main


import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.weather.data.api.current.CurrentWeather
import com.example.weather.data.api.forecast.ForecastWeather
import com.example.weather.data.services.DownloadInfoWorker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class WeatherViewModel : ViewModel() {
    var currentWeather = MutableLiveData<CurrentWeather>()
    var forecastWeather = MutableLiveData<ForecastWeather>()
    var cityName = MutableLiveData<String>("")
    var networkFail = MutableLiveData(false)
    var wrongCity = MutableLiveData(false)
    var sunriseTime = MutableLiveData<String>()
    var sunsetTime = MutableLiveData<String>()


    val weatherAndTimes = MediatorLiveData<WeatherDetails>().apply {
        addSource(currentWeather) { weather ->
            value =
                WeatherDetails(weather, sunriseTime.value, sunsetTime.value, forecastWeather.value)
        }
        addSource(sunriseTime) { sunrise ->
            value = WeatherDetails(
                currentWeather.value,
                sunrise,
                sunsetTime.value,
                forecastWeather.value
            )
        }
        addSource(sunsetTime) { sunset ->
            value = WeatherDetails(
                currentWeather.value,
                sunriseTime.value,
                sunset,
                forecastWeather.value
            )

        }
        addSource(forecastWeather) { forecastWeather ->
            value = WeatherDetails(
                currentWeather.value,
                sunriseTime.value,
                sunsetTime.value,
                forecastWeather
            )

        }
    }

    data class WeatherDetails(
        val currentWeather: CurrentWeather?,
        val sunriseTime: String?,
        val sunsetTime: String?,
        val forecastWeather: ForecastWeather?
    )


    fun loadDataFromWorker(lifecycleOwner: LifecycleOwner) {
        val workManager = WorkManager.getInstance()
        val data: Data =
            Data.Builder().putString(MainActivity.KEY_TO_GET_CITY, cityName.value).build()
        val oneTimeWorkRequest =
            OneTimeWorkRequest.Builder(DownloadInfoWorker::class.java).setInputData(data).build()
        workManager.enqueue(oneTimeWorkRequest)
        workManager.getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
            .observe(lifecycleOwner, Observer {
                println("Work state: ${it.state}")
                if (it.state.isFinished) {
                    val outputData = it.outputData
                    val message = outputData.getString(DownloadInfoWorker.KEY_CURRENT_WEATHER)
                    val message2 = outputData.getString(DownloadInfoWorker.KEY_FORECAST_WEATHER)
                    println("first message : $message and second $message2")
                    if (message == "success" && message2 == "success") {
                        currentWeather.value = DownloadInfoWorker.currentWeather
                        forecastWeather.value = DownloadInfoWorker.forecastWeather
                        // Increment by 1 day to get tomorrow's date
                        val sunriseUtc =
                            currentWeather.value!!.sys.sunrise.toLong() // UTC timestamp for sunrise
                        val timezoneOffset = currentWeather.value!!.timezone
                        sunriseTime.value = convertUtcToLocal(sunriseUtc, timezoneOffset)
                        val sunsetUtc =
                            currentWeather.value!!.sys.sunset.toLong() // UTC timestamp for sunrise
                        sunsetTime.value = convertUtcToLocal(sunsetUtc, timezoneOffset)
                        println("the name of city is ${currentWeather.value!!.name}")

                    } else {
                        val error = it.outputData.getString("error")
                        val errorMessage = it.outputData.getString("errorMessage")
                        println("Work failed yay: $error - $errorMessage")
                        if (error == "HTTP error") {
                            wrongCity.value = true
                        } else {
                            networkFail.value = true
                        }

                    }
                } else {
                    print("not Working !!")
                }

            })


    }

    fun convertUtcToLocal(utcTimeInSeconds: Long, timezoneOffsetInSeconds: Int): String {
        // Convert UTC time from seconds to milliseconds
        val utcTimeInMillis = utcTimeInSeconds * 1000

        // Determine the local time zone from the offset
        val offsetInHours = timezoneOffsetInSeconds / 3600
        val localTimeZone = TimeZone.getTimeZone("GMT+$offsetInHours:00")

        // Create a SimpleDateFormat with desired pattern
        val sdf = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        sdf.timeZone = localTimeZone // Set the desired time zone

        // Format the UTC time as local time
        return sdf.format(Date(utcTimeInMillis))
    }
}