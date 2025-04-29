package com.madkit.weatherapp.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.madkit.weatherapp.data.repository.WeatherRepositoryImpl
import com.madkit.weatherapp.domain.repository.WeatherRepository
import com.madkit.weatherapp.data.network.OpenMeteoService
import com.madkit.weatherapp.data.network.WeatherService
import com.madkit.weatherapp.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WeatherModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder().create()
    }

    @Provides
    @Singleton
    @Named("openMeteoRetrofit")
    fun provideOpenMeteoRetrofit(gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL_OPEN_METEO)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @Named("weatherRetrofit")
    fun provideWeatherRetrofit(gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL_OPEN_WEATHER)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherService(@Named("weatherRetrofit") retrofit: Retrofit): WeatherService {
        return retrofit.create(WeatherService::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenMeteoService(@Named("openMeteoRetrofit") retrofit: Retrofit): OpenMeteoService {
        return retrofit.create(OpenMeteoService::class.java)
    }
    @Provides
    @Singleton
    fun provideWeatherRepository(
        weatherService: WeatherService,
        openMeteoService: OpenMeteoService
    ): WeatherRepository {
        return WeatherRepositoryImpl(weatherService, openMeteoService)
    }
}