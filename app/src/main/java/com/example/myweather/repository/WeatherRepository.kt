package com.example.myweather.repository

import android.util.Log
import com.example.myweather.network.*
import java.lang.Exception
import javax.inject.Inject

interface WeatherRepository {
    suspend fun getCurrentLocationWeather(request: WeatherRequest): Result<Exception, List<WeatherInfo>?>
}

class WeatherRepositoryImpl @Inject constructor(
    private val weatherService: WeatherService
) : WeatherRepository {

    override suspend fun getCurrentLocationWeather(request: WeatherRequest): Result<Exception, List<WeatherInfo>?> {
        return try {
            val response = weatherService.getForecastWeather(
                request.serviceKey, request.baseDate, request.baseTime, request.x, request.y, request.dataType, request.numOfRows, request.pageNo
            ).execute()

            if (response.isSuccessful) {
                val body = response.body() as WeatherResponse
                if (body.response.header.resultCode == ResponseType.NO_ERR) {
                    Result.Success(body.response.body?.weatherInfo?.weatherInfos)
                } else {
                    Result.Fail(Error(Throwable("${body.response.header.resultCode}//${body.response.header.resultMsg}")))
                }
            } else {
                Result.Fail(Error(Throwable("retrofit response failure")))
            }
        } catch (exception: Throwable) {
            Log.e("exception", exception.stackTraceToString())
            Result.Fail(Error(exception))
        }
    }
}