package com.example.myweather.network

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherService @Inject constructor(
    private val retrofit: Retrofit
) : WeatherApi {

    private val api by lazy { retrofit.create(WeatherApi::class.java) }

    override fun getForecastWeather(
        serviceKey: String,
        date: String,
        time: String,
        nx: Int,
        ny: Int,
        dataType: String,
        numOfRows: Int,
        pageNo: Int
    ): Call<WeatherResponse> = api.getForecastWeather(
        serviceKey, date, time, nx, ny, dataType, numOfRows, pageNo
    )
}

interface WeatherApi {

    @GET("getVilageFcst")
    fun getForecastWeather(
        @Query("serviceKey", encoded = true) serviceKey: String,
        @Query("base_date") date: String,
        @Query("base_time") time: String,
        @Query("nx") nx: Int,
        @Query("ny") ny: Int,
        @Query("dataType") dataType: String,
        @Query("numOfRows") numOfRows: Int,
        @Query("pageNo") pageNo: Int
    ): Call<WeatherResponse>
}