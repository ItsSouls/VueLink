package es.uma.vuelink.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val name: String,
    val main: WeatherMain,
    val weather: List<WeatherDetails>
) {
    fun toWeatherInfo(): WeatherInfo {
        val weatherDescription = weather.firstOrNull()?.description ?: "No disponible"
        return WeatherInfo(
            locationName = name,
            temperature = main.temp,
            description = weatherDescription
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
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class WeatherInfo(
    val locationName: String,
    val temperature: Double,
    val description: String
)