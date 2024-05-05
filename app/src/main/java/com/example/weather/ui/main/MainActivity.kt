package com.example.weather.ui.main

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.adapter.RecyclerAdapter
import com.example.weather.data.SharedPreferenceManager
import com.example.weather.data.api.forecast.ForecastData
import com.example.weather.data.notify.AlarmReceiver
import com.example.weather.data.notify.NotificationAlarmScheduler
import com.example.weather.data.notify.ReminderItem
import com.example.weather.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var viewModel: WeatherViewModel
    lateinit var tempdeg: String
    lateinit var maxTemp: String
    lateinit var minTemp: String
    lateinit var feelsLike: String
    lateinit var sharedPreferenceManager: SharedPreferenceManager
    lateinit var bottomSheetDialog: BottomSheetDialog
    lateinit var view: View
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: RecyclerAdapter
    lateinit var toCloseDialog: TextView
    var dialogIsClosed: Boolean = true
    private val notificationAlarmScheduler by lazy {
        NotificationAlarmScheduler(this)
    }

    var forecastArray = ArrayList<ForecastData>()
    var cityName: String = "london"


    companion object {
        const val KEY_TO_GET_CITY = "key_to_get_city"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferenceManager = SharedPreferenceManager(applicationContext)
        viewModel = ViewModelProviders.of(this).get(WeatherViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        requestForPermission()
        scheduleDailyNotification()
        checkStoredData()
        refreshData()
        binding.fiveDays.setOnClickListener {
            openDialog()
        }

        viewModel.cityName.observe(this) { value ->
            println("the city you tryin to observe : $value")
            refreshData()
            hideKeyboard(this, binding.enterCity)
        }
        viewModel.networkFail.observe(this) { value ->
            if (value == true)
                Toast.makeText(
                    this,
                    "Please make sure you are connected to internet!!",
                    Toast.LENGTH_LONG
                ).show()
        }
        viewModel.wrongCity.observe(this) { value ->
            if (value == true)
                Toast.makeText(this, "Please enter a valid city name !!", Toast.LENGTH_LONG).show()
        }

        binding.enterCity.setOnEditorActionListener(
            OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event == null || !event.isShiftPressed) {
                        cityName = binding.enterCity.text.toString()
                        viewModel.cityName.value = cityName
                        println("the city you entered is ${viewModel.cityName.value}")
                        return@OnEditorActionListener true // consume.
                    }
                }
                false // pass on to other listeners.
            }
        )
        binding.swipeRefresh.setOnRefreshListener {
            refreshData()

        }
    }

    fun hideKeyboard(context: Context, textView: TextView) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(textView.windowToken, 0) // Hide keyboard
    }

    fun checkStoredData() {
        val lastWeather = sharedPreferenceManager.getWeather()
        val lastForecastWeather = sharedPreferenceManager.getForecast()
        if (lastWeather != null && lastForecastWeather != null) {

            viewModel.currentWeather.value = lastWeather
            viewModel.sunriseTime.value =
                viewModel.convertUtcToLocal(lastWeather.sys.sunrise.toLong(), lastWeather.timezone)
            viewModel.sunsetTime.value =
                viewModel.convertUtcToLocal(lastWeather.sys.sunset.toLong(), lastWeather.timezone)
            viewModel.cityName.value = lastWeather.name
            viewModel.forecastWeather.value = lastForecastWeather
            cityName = lastWeather.name

        } else {
            viewModel.cityName.value = cityName
        }

    }

    @SuppressLint("SetTextI18n")
    private fun refreshData() {
        binding.swipeRefresh.isRefreshing = true
        // Load data
        viewModel.loadDataFromWorker(this)
        // Observe data changes to update the UI and hide the refresh indicator
        viewModel.weatherAndTimes.observe(this) { weatherDetails ->
            weatherDetails?.let {

                val currentWeather = it.currentWeather
                val sunrise = it.sunriseTime
                val sunset = it.sunsetTime
                val forecastWeather = it.forecastWeather
                if (currentWeather != null && sunrise != null && sunset != null && forecastWeather != null)
                // Update the UI
                {
                    forecastArray = forecastWeather.list as ArrayList<ForecastData>
                    sharedPreferenceManager.saveLastWeather(currentWeather, forecastWeather)
                    val adapter = RecyclerAdapter(forecastArray)
                    this@MainActivity.adapter = adapter
                    //   bottomSheetBinding.rvForecast.adapter=adapter
                    //   bottomSheetBinding.textView.text = "Five days forecast in ${forecastWeather.city.name} "
                    tempdeg = currentWeather.main.temp.toInt().toString()
                    tempdeg += getString(R.string.celsius)
                    val iconId = currentWeather.weather[0].icon
                    val imgUrl = "https://openweathermap.org/img/wn/$iconId.png"
                    Picasso.get().load(imgUrl).into(binding.weatherIcon)
                    binding.apply {
                        temp.text = tempdeg
                        location.text = currentWeather.name
                        weatherDisc.text = currentWeather.weather[0].description
                        minTemp = currentWeather.main.temp_min.toInt().toString()
                        maxTemp = currentWeather.main.temp_max.toInt().toString()
                        feelsLike = currentWeather.main.feels_like.toInt().toString()
                        maxAndMin.text = getString(R.string.feels_like, maxTemp, minTemp, feelsLike)
                        wind.text = "Wind \n ${currentWeather.wind.speed} km/h"
                        pressure.text = "Pressure \n ${currentWeather.main.pressure} hPa"
                        humidity.text = "Humidity \n ${currentWeather.main.humidity} %"
                        airQuality.text = "Air quality \n fair"
                        sunRise.text = "Sunrise \n $sunrise"
                        sunSet.text = "Sunrise \n $sunset"
                        lastUpdate.text = "Last update : " + SimpleDateFormat(
                            "hh:mm a",
                            Locale.ENGLISH
                        ).format(System.currentTimeMillis())
                        Handler(Looper.getMainLooper()).postDelayed({
                            swipeRefresh.isRefreshing =
                                false // Hide the refresh icon after 500ms delay
                        }, 500)
                    }
                }
            }
        }
    }


    private val pushPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
    }

    private fun openDialog() {
        if (dialogIsClosed) {
            view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_layout, null)
            bottomSheetDialog = BottomSheetDialog(this)
            recyclerView = view.findViewById(R.id.rv_forecast)
            recyclerView.adapter = adapter
            recyclerView.layoutManager =
                GridLayoutManager(this, 1, RecyclerView.HORIZONTAL, false)
            bottomSheetDialog.window?.apply {
                requestFeature(Window.FEATURE_NO_TITLE)
                setWindowAnimations(R.style.DialogAnimation)
            }
            bottomSheetDialog.setContentView(view)
            toCloseDialog = view.findViewById(R.id.fiveDays)
            toCloseDialog.setOnClickListener {
                bottomSheetDialog.dismiss()
            }
            bottomSheetDialog.setOnDismissListener { dialogIsClosed = true }
            bottomSheetDialog.show()
            dialogIsClosed = false
        }
    }

    private fun requestForPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pushPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun scheduleDailyNotification() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set the alarm to trigger at 12 PM every day
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.MINUTE, 1)

            // If the current time is past 12 PM, schedule for the next day
            if (before(Calendar.getInstance())) {
                add(Calendar.MINUTE, 1)
            }
        }
        val reminderItem = ReminderItem(time = calendar.timeInMillis, id = 1)
        notificationAlarmScheduler.schedule(reminderItem)

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent,
        )
    }


}




