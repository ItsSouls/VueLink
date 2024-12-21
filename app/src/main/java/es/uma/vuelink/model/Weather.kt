package es.uma.vuelink.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val name: String, val main: WeatherMain, val weather: List<WeatherDetails>
) {
    fun toWeatherInfo(): WeatherInfo {
        return WeatherInfo(
            locationName = name,
            temperature = main.temp,
            tempMin = main.tempMin,
            tempMax = main.tempMax,
            weatherId = weather.firstOrNull()?.id ?: 900
        )
    }
}

data class WeatherMain(
    val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double,
    val pressure: Int,
    val humidity: Int
)

data class WeatherDetails(
    val id: Int, val main: String, val description: String, val icon: String
)

data class WeatherInfo(
    val locationName: String,
    val temperature: Double,
    val tempMin: Double,
    val tempMax: Double,
    val weatherId: Int
)