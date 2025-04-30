# 🌦️ Weatherly - Android Weather App

Weatherly is a lightweight and efficient Android weather app crafted with Kotlin and powered by a clean MVVM architecture. It delivers accurate real-time weather updates, detailed hourly and daily climate insights, and environmental air quality metrics. Leveraging the strengths of both OpenWeather and Open-Meteo APIs, Weatherly transforms raw meteorological data into meaningful information—tailored for a smooth and responsive mobile experience.

## 🚀 Technologies

- **Language**: Kotlin
- **Architecture**: MVVM
- **Dependency Injection**: Hilt (Dagger)
- **Networking**: Retrofit
- **Image Loading**: Picasso
- **JSON Parsing**: Gson
- **UI Management**: ViewBinding
- **Navigation**: AndroidX Navigation Component

## 📦 APIs Used
OpenWeatherMap – for real-time weather & air pollution

Open-Meteo – for hourly & daily weather forecasts

## 🧠 Architecture Overview
📦 data
┣ 📂model              → API response data models
┣ 📂network            → Retrofit service interfaces
┗ 📂repository         → Repository implementation layer

📦 domain
┗ 📂repository         → Abstract repository interfaces

📦 di                   → Hilt module for dependency injection

📸 Screenshots
### 🟣 Splash Screen
![Splash](images/splash.jpeg)

### 🏠 Home Screen
![Home](images/home.jpeg)

### ⏱ Hourly Forecast
![Hourly Forecast](images/hourlyforecast.jpeg)

### 🌫 Air Quality Index
![AQI](images/aqi.jpeg)

### 🌅 Sunrise & Sunset
![Sunrise Sunset](images/sunrisesunset.jpeg)

### 🌬 Wind Speed
![Wind Speed](images/windspeed.jpeg)

### 📅 7-Day Forecast - Page 1
![Week 1](images/oneweek1.jpeg)

![Week 2](images/oneweek2.jpeg)