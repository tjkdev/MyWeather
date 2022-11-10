package com.example.myweather.network

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("response")
    val response: XmlResponse
)

data class XmlResponse(
    @SerializedName("header")
    val header: XmlHeader,
    @SerializedName("body")
    val body: XmlBody?
)

data class XmlHeader(
    @SerializedName("resultCode")
    val resultCode: ResponseType,
    @SerializedName("resultMsg")
    val resultMsg: String
)

data class XmlBody(
    @SerializedName("items")
    val weatherInfo: WeatherInfoResponse
)

data class WeatherInfoResponse(
    @SerializedName("item")
    val weatherInfos: List<WeatherInfo>
)

data class WeatherInfo(
    @SerializedName("fcstDate")
    val date: String,
    @SerializedName("fcstTime")
    val time: String,
    @SerializedName("category")
    val category: WeatherCategory,
    @SerializedName("fcstValue")
    val fcstValue: String
)

enum class ResponseType(val category: String) {
    NO_ERR("00"),
    APPLICATION_ERR("01"),
    DB_ERR("02"),
    NO_DATA("03"),
    HTTP_ERR("04"),
    SERVICE_TIME_OUT("05"),
    INVALID_REQUEST_PARAM("10"),
    NO_MANDATORY_REQUEST_PARAM("11"),
    NO_OPEN_API_SERVICE("12"),
    SERVICE_ACCESS_DENIED("20"),
    TEMPORARILY_DISABLE_THE_SERVICE_KEY("21"),
    LIMITED_NUMBER_OF_SERVICE_REQUESTS_EXCEEDS("22"),
    SERVICE_KEY_IS_NOT_REGISTERED("30"),
    DEADLINE_HAS_EXPIRED("31"),
    UNREGISTERED_IP("32"),
    UNSIGNED_CALL("33"),
    UNKNOWN("99");

    companion object {
        fun getValueOf(value: String): ResponseType {
            if (value.isEmpty()) return UNKNOWN
            for (type in values()) {
                if (type.category == value) return type
            }
            return UNKNOWN
        }
    }
}

enum class WeatherCategory(val category: String, val title: String) {
    ADDRESS("ADDRESS", ""),
    TIME("TIME", ""),
    SKY("SKY", ""),
    TEMPERATURE_PER_HOUR("TMP", "기온"),
    TEMPERATURE_LOW("TMN", "최고기온"),
    TEMPERATURE_HIGH("TMX", "최저기온"),
    RAIN_PERCENTAGE("POP", "강수확률"),
    RAIN_TYPE("PTY", ""),
    RAIN_PER_HOUR("PCP", "시간 당 강수량"),
    HUMIDITY("REH", "습도"),
    SNOW_PER_HOUR("SNO", "시간 당 적설량"),
    WAVE("WAV", "파고"),
    WIND_DIRECTION("VEC", "풍향"),
    ERR("", "");

    companion object {
        fun getValueOf(value: String): WeatherCategory {
            if (value.isEmpty()) return ERR
            for (type in values()) {
                if (type.category == value) return type
            }
            return ERR
        }
    }
}