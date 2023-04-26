package com.example.myweather

import android.Manifest
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.myweather.network.WeatherCategory
import com.example.myweather.network.WeatherInfo
import com.example.myweather.ui.theme.MyWeatherTheme
import com.example.myweather.util.Address
import com.example.myweather.util.LocationUtil
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel.VM by viewModels()
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            requestLocation()
        }
    }

    @OptIn(ExperimentalPagerApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pages = listOf(getString(R.string.title_current), getString(R.string.title_forecast))
        val weatherInfo = viewModel.outputs.currentLocationWeather()
        val weatherForecastInfo = viewModel.outputs.currentLocationForecastWeather()
        setContent {
            MyWeatherTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        val pagerState = rememberPagerState()
                        val coroutineScope = rememberCoroutineScope()

                        androidx.compose.material.TabRow(
                            selectedTabIndex = pagerState.currentPage,
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(
                                    modifier = Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                                    color = LightGray
                                )
                            }
                        ) {
                            pages.forEachIndexed { index, title ->
                                Tab(
                                    text = { Text(text = title, color = White) },
                                    selected = pagerState.currentPage == index,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.scrollToPage(index)
                                        }
                                    }
                                )
                            }
                        }

                        HorizontalPager(
                            count = pages.size,
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (currentPage == 0) {
                                CurrentWeatherView(weatherInfo.observeAsState().value ?: emptyList())
                            } else {
                                ForecastWeatherView(weatherForecastInfo.observeAsState().value ?: emptyList())
                            }
                            val failure = viewModel.outputs.networkFailure().observeAsState().value
                            ShowError(error = failure)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkLocationPermission()
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                requestLocation()
            }
        } else {
            requestLocation()
        }
    }

    private fun requestLocation() {
        LocationUtil(this@MainActivity, ::convertLocation).getLocation()
    }

    private fun convertLocation(address: Address) {
        viewModel.inputs.getWeather(address, getRequestDateTime())
    }

    private fun getRequestDateTime(): Pair<String, String> {
        val format = SimpleDateFormat("yyyyMMdd HH mm", Locale.KOREA)
        val date = Date(System.currentTimeMillis())
        val dateTimeStringArray = format.format(date).split(" ")

        return Pair(dateTimeStringArray[0], getValidTime(dateTimeStringArray[1]))
    }

    private fun getValidTime(timeString: String): String {
        return when (timeString) {
            "02", "03", "04" -> "0200"
            "05", "06", "07" -> "0500"
            "08", "09", "10" -> "0800"
            "11", "12", "13" -> "1100"
            "14", "15", "16" -> "1400"
            "17", "18", "19" -> "1700"
            "20", "21", "22" -> "2000"
            "23", "24", "01" -> "2300"
            else -> "0200"
        }
    }
}

@Composable
fun ShowError(error: Exception?) {
    error?.let {
        Toast.makeText(LocalContext.current, it.message, Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun CurrentWeatherView(weatherInfos: List<WeatherInfo>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(weatherInfos) { _, item ->
            when (item.category) {
                WeatherCategory.ADDRESS -> WeatherAddressAndTime(weatherInfo = item)
                WeatherCategory.RAIN_PERCENTAGE -> WeatherRainPercentage(weatherInfo = item)
                WeatherCategory.RAIN_TYPE -> WeatherSky(weatherInfo = item)
                WeatherCategory.RAIN_PER_HOUR -> WeatherRainPerHour(weatherInfo = item)
                WeatherCategory.HUMIDITY -> WeatherHumidity(weatherInfo = item)
                WeatherCategory.SNOW_PER_HOUR -> WeatherSnowPerHour(weatherInfo = item)
                WeatherCategory.SKY -> WeatherSky(weatherInfo = item)
                WeatherCategory.TEMPERATURE_PER_HOUR -> WeatherTemperaturePerHour(weatherInfo = item)
                WeatherCategory.TEMPERATURE_LOW -> WeatherTemperatureLow(weatherInfo = item)
                WeatherCategory.TEMPERATURE_HIGH -> WeatherTemperatureHigh(weatherInfo = item)
                WeatherCategory.WAVE -> WeatherWave(weatherInfo = item)
                WeatherCategory.WIND_DIRECTION -> WeatherWindDirection(weatherInfo = item)
                else -> {}
            }
        }
    }
}

@Composable
fun ForecastWeatherView(weatherInfos: List<WeatherInfo>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(weatherInfos) { _, item ->
            when (item.category) {
                WeatherCategory.ADDRESS -> WeatherAddressAndTime(weatherInfo = item)
                WeatherCategory.TIME -> WeatherTime(weatherInfo = item)
                WeatherCategory.RAIN_PERCENTAGE -> WeatherRainPercentage(weatherInfo = item)
                WeatherCategory.RAIN_TYPE -> WeatherSky(weatherInfo = item)
                WeatherCategory.RAIN_PER_HOUR -> WeatherRainPerHour(weatherInfo = item)
                WeatherCategory.HUMIDITY -> WeatherHumidity(weatherInfo = item)
                WeatherCategory.SNOW_PER_HOUR -> WeatherSnowPerHour(weatherInfo = item)
                WeatherCategory.SKY -> WeatherSky(weatherInfo = item)
                WeatherCategory.TEMPERATURE_PER_HOUR -> WeatherTemperaturePerHour(weatherInfo = item)
                WeatherCategory.TEMPERATURE_LOW -> WeatherTemperatureLow(weatherInfo = item)
                WeatherCategory.TEMPERATURE_HIGH -> WeatherTemperatureHigh(weatherInfo = item)
                WeatherCategory.WAVE -> WeatherWave(weatherInfo = item)
                WeatherCategory.WIND_DIRECTION -> WeatherWindDirection(weatherInfo = item)
                else -> {}
            }
        }
    }
}

@Composable
fun WeatherAddressAndTime(weatherInfo: WeatherInfo) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = weatherInfo.fcstValue, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 15.dp))
        Text(text = "${weatherInfo.date} ${weatherInfo.time}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(vertical = 5.dp))
    }
}

@Composable
fun WeatherTime(weatherInfo: WeatherInfo) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "${weatherInfo.date} ${weatherInfo.time}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(vertical = 5.dp))
    }
}

@Composable
fun WeatherRainPercentage(weatherInfo: WeatherInfo) {
    Text(text = weatherInfo.category.title, style = MaterialTheme.typography.labelMedium)
    Text(text = stringResource(R.string.unit_percent, weatherInfo.fcstValue), style = MaterialTheme.typography.labelSmall)
}

@Composable
fun WeatherRainPerHour(weatherInfo: WeatherInfo) {
    if (weatherInfo.fcstValue != stringResource(R.string.rain_no)) {
        Text(text = weatherInfo.category.title, style = MaterialTheme.typography.labelMedium)
        Text(text = stringResource(R.string.unit_mm, weatherInfo.fcstValue), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun WeatherHumidity(weatherInfo: WeatherInfo) {
    Text(text = weatherInfo.category.title, style = MaterialTheme.typography.labelMedium)
    Text(text = stringResource(id = R.string.unit_percent, weatherInfo.fcstValue), style = MaterialTheme.typography.labelSmall)
}

@Composable
fun WeatherSnowPerHour(weatherInfo: WeatherInfo) {
    if (weatherInfo.fcstValue != stringResource(R.string.snow_no)) {
        Text(text = weatherInfo.category.title, style = MaterialTheme.typography.labelMedium)
        Text(text = stringResource(R.string.unit_cm, weatherInfo.fcstValue), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun WeatherSky(weatherInfo: WeatherInfo) {
    if (weatherInfo.category == WeatherCategory.SKY) {
        val skyValue = when {
            weatherInfo.fcstValue.toInt() < 6 -> stringResource(R.string.sky_clear)
            weatherInfo.fcstValue.toInt() in 6..8 -> stringResource(R.string.sky_little_cloudy)
            weatherInfo.fcstValue.toInt() > 8 -> stringResource(R.string.sky_cloudy)
            else -> stringResource(R.string.empty)
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = skyValue, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 15.dp))
        }
    } else {
        val skyValueInt = weatherInfo.fcstValue.toInt()
        if (skyValueInt > 0) {
            val skyValue = when (skyValueInt) {
                1 -> stringResource(R.string.weather_rain)
                2 -> stringResource(R.string.weather_rain_snow)
                3 -> stringResource(R.string.weather_snow)
                4 -> stringResource(R.string.weather_shower)
                else -> stringResource(R.string.empty)
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = skyValue, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 15.dp))
            }
        }
    }
}

@Composable
fun WeatherTemperaturePerHour(weatherInfo: WeatherInfo) {
    Text(text = weatherInfo.category.title, style = MaterialTheme.typography.labelMedium)
    Text(text = stringResource(R.string.unit_temperature, weatherInfo.fcstValue), style = MaterialTheme.typography.labelSmall)
}

@Composable
fun WeatherTemperatureLow(weatherInfo: WeatherInfo) {
    Text(text = weatherInfo.category.title, style = MaterialTheme.typography.labelMedium)
    Text(text = stringResource(R.string.unit_temperature, weatherInfo.fcstValue), style = MaterialTheme.typography.labelSmall)
}

@Composable
fun WeatherTemperatureHigh(weatherInfo: WeatherInfo) {
    Text(text = weatherInfo.category.title, style = MaterialTheme.typography.labelMedium)
    Text(text = stringResource(R.string.unit_temperature, weatherInfo.fcstValue), style = MaterialTheme.typography.labelSmall)
}

@Composable
fun WeatherWave(weatherInfo: WeatherInfo) {
    if (weatherInfo.fcstValue != "0") {
        Text(text = weatherInfo.category.title, style = MaterialTheme.typography.labelMedium)
        Text(text = stringResource(R.string.unit_m, weatherInfo.fcstValue), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun WeatherWindDirection(weatherInfo: WeatherInfo) {
    val direction = when (weatherInfo.fcstValue.toInt()) {
        in 0..20 -> stringResource(R.string.north)
        in 21..69 -> stringResource(R.string.northeast)
        in 70..110 -> stringResource(R.string.east)
        in 111..159 -> stringResource(R.string.southeast)
        in 160..200 -> stringResource(R.string.south)
        in 201..249 -> stringResource(R.string.southwest)
        in 250..290 -> stringResource(R.string.west)
        in 291..339 -> stringResource(R.string.northwest)
        in 340..360 -> stringResource(R.string.north)
        else -> stringResource(R.string.empty)
    }
    if (direction.isNotBlank()) {
        Text(text = weatherInfo.category.title, style = MaterialTheme.typography.labelMedium)
        Text(text = direction, style = MaterialTheme.typography.labelSmall)
    }
}