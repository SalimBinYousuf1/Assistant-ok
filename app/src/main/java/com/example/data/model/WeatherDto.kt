package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    val results: List<GeoLocationDto>?
)

@JsonClass(generateAdapter = true)
data class GeoLocationDto(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String?,
    @Json(name = "admin1") val adminRegion: String?
)

@JsonClass(generateAdapter = true)
data class WeatherApiResponse(
    val latitude: Double?,
    val longitude: Double?,
    @Json(name = "current_weather") val currentWeather: CurrentWeatherDto?
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherDto(
    val temperature: Double,
    val windspeed: Double,
    val winddirection: Double?,
    val weathercode: Int,
    val time: String?
)

data class FormattedWeatherData(
    val locationName: String,
    val temperatureCelsius: Double,
    val conditionDescription: String,
    val windSpeedKmh: Double
) {
    val temperatureFahrenheit: Double
        get() = (temperatureCelsius * 9.0 / 5.0) + 32.0
}
