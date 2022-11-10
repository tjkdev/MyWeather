package com.example.myweather

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myweather.network.WeatherCategory
import com.example.myweather.network.WeatherInfo
import com.example.myweather.network.WeatherRequest
import com.example.myweather.usecase.GetCurrentLocationForecastUseCase
import com.example.myweather.util.Address
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

interface MainViewModel {

    interface Inputs {
        fun getWeather(address: Address, dateTimePair: Pair<String, String>)
    }

    interface Outputs {
        fun currentLocationWeather(): LiveData<List<WeatherInfo>?>
        fun currentLocationForecastWeather(): LiveData<List<WeatherInfo>?>
        fun networkFailure(): LiveData<Exception>
    }

    @HiltViewModel
    class VM @Inject constructor(
        private val getCurrentLocationForecastUseCase: GetCurrentLocationForecastUseCase
    ) : Inputs, Outputs, ViewModel() {

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val currentLocationWeather = MutableLiveData<List<WeatherInfo>?>()
        private val currentLocationForecastWeather = MutableLiveData<List<WeatherInfo>?>()
        private val networkFailure = MutableLiveData<Exception>()

        override fun getWeather(address: Address, dateTimePair: Pair<String, String>) {
            CoroutineScope(Dispatchers.IO).launch {
                getCurrentLocationForecastUseCase.run(WeatherRequest(dateTimePair.first, dateTimePair.second, address.x, address.y)) {
                    it.handle(
                        { exception ->
                            networkFailure.postValue(exception)
                        }, { info ->
                            if (!info.isNullOrEmpty()) {
                                if (info.any { it.time.toInt() == dateTimePair.second.toInt() }) {
                                    currentLocationWeather.postValue(
                                        makeCurrentWeatherList(
                                            info.filter {
                                                it.date == dateTimePair.first && it.time.toInt() == dateTimePair.second.toInt()
                                            },
                                            address
                                        )
                                    )
                                    currentLocationForecastWeather.postValue(
                                        makeForecastWeatherList(
                                            info.filter {
                                                (it.date == dateTimePair.first && it.time.toInt() > dateTimePair.second.toInt()) ||
                                                it.date != dateTimePair.first
                                            },
                                            address
                                        )
                                    )
                                } else {
                                    currentLocationWeather.postValue(
                                        makeCurrentWeatherList(
                                            info.filter {
                                                it.date == dateTimePair.first && it.time.toInt() == dateTimePair.second.toInt() + 100
                                            },
                                            address
                                        )
                                    )
                                    currentLocationForecastWeather.postValue(
                                        makeForecastWeatherList(
                                            info.filter {
                                                (it.date == dateTimePair.first && it.time.toInt() > dateTimePair.second.toInt() + 100) ||
                                                    it.date != dateTimePair.first
                                            },
                                            address
                                        )
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }

        private fun makeCurrentWeatherList(filteredList: List<WeatherInfo>, address: Address): List<WeatherInfo> {
            return filteredList.toMutableList().apply {
                val skyIndex = indexOfFirst { it.category == WeatherCategory.SKY }
                if (skyIndex > -1) {
                    val skyItem = get(skyIndex)
                    removeAt(skyIndex)
                    add(0, skyItem)
                }
                add(0, WeatherInfo(convertDate(get(0).date), convertTime(get(0).time), WeatherCategory.ADDRESS, address.address))
            }
        }

        private fun makeForecastWeatherList(filteredList: List<WeatherInfo>, address: Address): List<WeatherInfo> {
            filteredList.toMutableList().apply {
                val skyIndex = indexOfFirst { it.category == WeatherCategory.SKY }
                if (skyIndex > -1) {
                    val skyItem = get(skyIndex)
                    removeAt(skyIndex)
                    add(0, skyItem)
                }
                add(0, WeatherInfo(convertDate(get(0).date), convertTime(get(0).time), WeatherCategory.ADDRESS, address.address))
            }

            var pastTime = -1
            val forecastWeatherList = mutableListOf<WeatherInfo>()
            filteredList.sortedWith(
                compareBy<WeatherInfo> { it.date.toInt() }.thenBy { it.time.toInt() }.thenBy { it.category.ordinal }
            ).forEachIndexed { index, weatherInfo ->
                if (pastTime < 0) {
                    if (index > 0) {
                        pastTime = weatherInfo.time.toInt()
                    } else {
                        forecastWeatherList.add(0, WeatherInfo(convertDate(weatherInfo.date), convertTime(weatherInfo.time), WeatherCategory.ADDRESS, address.address))
                    }
                    forecastWeatherList.add(weatherInfo)
                } else if (pastTime == weatherInfo.time.toInt()) {
                    forecastWeatherList.add(weatherInfo)
                } else {
                    forecastWeatherList.add(WeatherInfo(convertDate(weatherInfo.date), convertTime(weatherInfo.time), WeatherCategory.TIME, ""))
                    forecastWeatherList.add(weatherInfo)
                    pastTime = weatherInfo.time.toInt()
                }
            }

            return forecastWeatherList
        }

        private fun convertDate(date: String): String {
            val dateStringChunkedArray = date.chunked(2)
            return "${dateStringChunkedArray[0]}${dateStringChunkedArray[1]}년 ${dateStringChunkedArray[2]}월 ${dateStringChunkedArray[3]}일"
        }

        private fun convertTime(time: String): String {
            val timeStringChunkedArray = time.chunked(2)
            return "${timeStringChunkedArray[0]}시 기준"
        }

        override fun currentLocationWeather(): LiveData<List<WeatherInfo>?> = currentLocationWeather
        override fun currentLocationForecastWeather(): LiveData<List<WeatherInfo>?> = currentLocationForecastWeather
        override fun networkFailure(): LiveData<Exception> = networkFailure
    }
}