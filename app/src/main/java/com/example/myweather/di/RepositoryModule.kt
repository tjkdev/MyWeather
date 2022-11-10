package com.example.myweather.di

import com.example.myweather.repository.WeatherRepository
import com.example.myweather.repository.WeatherRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
class RepositoryModule {

    @Provides
    fun provideWeatherRepository(repositoryImpl: WeatherRepositoryImpl): WeatherRepository = repositoryImpl
}