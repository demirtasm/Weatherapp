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
<img src="images/splash.jpeg" alt="Splash" width="300"/>

### 🏠 Home Screen
<img src="images/home.jpeg" alt="Home" width="300"/>

### ⏱ Hourly Forecast
<img src="images/hourlyforecast.jpeg" alt="Hourly Forecast" width="300"/>

### 🌫 Air Quality Index
<img src="images/aqi.jpeg" alt="AQI" width="300"/>

### 🌅 Sunrise & Sunset
<img src="images/sunrisesunset.jpeg" alt="Sunrise Sunset" width="300"/>

### 🌬 Wind Speed
<img src="images/windspeed.jpeg" alt="Wind Speed" width="300"/>

### 📅 7-Day Forecast - Page 1
<img src="images/oneweek1.jpeg" alt="Week 1" width="300"/> <br/> <img src="images/oneweek2.jpeg" alt="Week 2" width="300"/>