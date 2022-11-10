package com.example.myweather.usecase

import com.example.myweather.network.Result
import com.example.myweather.network.WeatherCategory
import com.example.myweather.network.WeatherInfo
import com.example.myweather.network.WeatherRequest
import com.example.myweather.repository.WeatherRepository
import kotlinx.coroutines.*
import javax.inject.Inject

class GetCurrentLocationForecastUseCase @Inject constructor(
    private val repo: WeatherRepository
) {
    @OptIn(DelicateCoroutinesApi::class)
    fun run(request: WeatherRequest, onResult: (Result<Exception, List<WeatherInfo>?>) -> Unit) {
        val job = GlobalScope.async(Dispatchers.IO) {
            repo.getCurrentLocationWeather(request).handle(
                {
                    Result.Fail(it)
                },
                {
                    it?.let {
                        Result.Success(it.filter { it.category != WeatherCategory.ERR })
                    } ?: run {
                        Result.Fail(Exception("no data"))
                    }
                }
            ) as Result<Exception, List<WeatherInfo>?>
        }
        GlobalScope.launch(Dispatchers.Main) {
            onResult(job.await())
        }
    }
}