package es.uma.vuelink.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.MarkerState
import es.uma.vuelink.BuildConfig
import es.uma.vuelink.R
import es.uma.vuelink.model.WeatherInfo
import es.uma.vuelink.model.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WeatherMarkerInfo(
    coordinates: LatLng, title: String, scheduledTime: String
) {
    val coroutineScope = rememberCoroutineScope()
    var weather by remember { mutableStateOf<WeatherInfo?>(null) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            weather =
                fetchWeather(LatLng(coordinates.latitude, coordinates.longitude), scheduledTime)
        }
    }

    MarkerInfoWindowContent(state = remember { MarkerState(position = coordinates) }) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(8.dp)
        ) {
            Column {
                Text(
                    text = title, style = MaterialTheme.typography.titleMedium
                )
                weather?.let {
                    Text(
                        text = stringResource(
                            R.string.forecast_format,
                            it.tempMin,
                            it.tempMax,
                            getWeatherEmoji(it.weatherId)
                        ), style = MaterialTheme.typography.bodyMedium
                    )
                } ?: Text(text = stringResource(R.string.loading_forecast))
            }
        }
    }
}

fun convertToUnixTimestamp(scheduled: String): Long {
    val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    val dateTime = Instant.from(formatter.parse(scheduled))
    return dateTime.epochSecond
}

suspend fun fetchWeather(latLng: LatLng, scheduled: String): WeatherInfo =
    withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val timestamp = convertToUnixTimestamp(scheduled)
        val languageCode = Locale.getDefault().language
        val url =
            "https://api.openweathermap.org/data/2.5/weather?lat=${latLng.latitude}&lon=${latLng.longitude}&dt=${timestamp}&units=metric&lang=$languageCode&appid=${BuildConfig.OPENWEATHER_API_KEY}"

        val request = Request.Builder().url(url).build()

        try {
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                println("Código de estado: ${response.code}")
                throw Exception("Error en la llamada a la API: ${response.message}")
            }

            val responseBody = response.body?.string()

            if (responseBody.isNullOrEmpty()) {
                throw Exception("Respuesta vacía de la API")
            }

            println("Respuesta de la API: $responseBody")

            Gson().fromJson(responseBody, WeatherResponse::class.java).toWeatherInfo()
        } catch (e: Exception) {
            println("Error en la llamada a la API: ${e.localizedMessage}")
            e.printStackTrace()
            throw e
        }
    }

fun getWeatherEmoji(weatherId: Int): String {
    return when (weatherId) {
        in 200..202 -> "⛈️"
        in 210..221 -> "🌩️"
        in 230..232 -> "⛈️"
        in 300..321 -> "🌧️"
        in 500..504 -> "🌦️"
        511 -> "❄️"
        in 520..531 -> "🌧️"
        in 600..622 -> "❄️"
        in 701..781 -> when (weatherId) {
            701 -> "🌫️"
            711 -> "💨"
            721 -> "🌫️"
            731, 751, 761 -> "🌪️"
            741 -> "🌫️"
            762 -> "🌋"
            771 -> "💨"
            781 -> "🌪️"
            else -> "🌫️"
        }

        800 -> "☀️"
        801 -> "🌤️"
        802 -> "⛅"
        803 -> "🌥️"
        804 -> "☁️"
        else -> "❓"
    }
}