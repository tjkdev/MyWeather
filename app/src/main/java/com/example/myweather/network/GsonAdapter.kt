package com.example.myweather.network

import com.google.gson.*
import java.lang.reflect.Type

class WeatherCategoryAdapter : JsonSerializer<WeatherCategory>, JsonDeserializer<WeatherCategory> {

    override fun serialize(
        src: WeatherCategory,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return context.serialize(src.category)
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): WeatherCategory {
        return WeatherCategory.getValueOf(json.asString)
    }
}

class ResponseTypeAdapter : JsonSerializer<ResponseType>, JsonDeserializer<ResponseType> {

    override fun serialize(
        src: ResponseType,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return context.serialize(src.category)
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ResponseType {
        return ResponseType.getValueOf(json.asString)
    }
}