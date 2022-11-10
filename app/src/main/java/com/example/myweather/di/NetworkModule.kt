package com.example.myweather.di

import com.example.myweather.network.WeatherCategory
import com.example.myweather.network.ResponseType
import com.example.myweather.network.ResponseTypeAdapter
import com.example.myweather.network.WeatherCategoryAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    private val url = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/"

    @Provides
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create(makeGson()))
            .build()
    }

    private fun makeGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(WeatherCategory::class.java, WeatherCategoryAdapter())
            .registerTypeAdapter(ResponseType::class.java, ResponseTypeAdapter())
            .create()
    }
}