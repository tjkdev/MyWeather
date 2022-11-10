package com.example.myweather.network

import com.google.gson.annotations.SerializedName

data class WeatherRequest(
    @SerializedName("base_date")
    val baseDate: String,
    @SerializedName("base_time")
    val baseTime: String,
    @SerializedName("nx")
    val x: Int,
    @SerializedName("ny")
    val y: Int,
    @SerializedName("dataType")
    val dataType: String = "JSON",
    @SerializedName("numOfRows")
    val numOfRows: Int = 250,
    @SerializedName("pageNo")
    val pageNo: Int = 1
) : BaseRequest()

open class BaseRequest(
    @SerializedName("serviceKey")
    val serviceKey: String = "TywO0gvW6f%2FWjQjrrc5H7PfHSaIEERt3jLQxsV%2BI3r%2F6hMisYBzWDyWEEfWjV7WtZoO5wbKv%2BlipbdwW7rPbaw%3D%3D"
)