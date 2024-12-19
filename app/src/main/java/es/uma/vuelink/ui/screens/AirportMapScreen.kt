package es.uma.vuelink.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import es.uma.vuelink.BuildConfig
import es.uma.vuelink.R
import es.uma.vuelink.data.AirportCoordinates
import es.uma.vuelink.data.FlightEntity
import es.uma.vuelink.model.WeatherInfo
import es.uma.vuelink.model.WeatherResponse
import es.uma.vuelink.ui.theme.VueLinkTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirportMapScreen(
    navController: NavHostController,
    departureAirport: String?,
    arrivalAirport: String?,
    airportCoordinatesList: List<AirportCoordinates>,
    flightDetails: FlightEntity?
) {
    VueLinkTheme {
        val defaultLocation = LatLng(40.416775, -3.703790)
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(defaultLocation, 5f)
        }

        val departureCoordinates = airportCoordinatesList.find { it.iata == departureAirport }
        val arrivalCoordinates = airportCoordinatesList.find { it.iata == arrivalAirport }

        val midpoint = if (departureCoordinates != null && arrivalCoordinates != null) {
            val midLat = (departureCoordinates.latitude + arrivalCoordinates.latitude) / 2
            val midLng = (departureCoordinates.longitude + arrivalCoordinates.longitude) / 2
            LatLng(midLat, midLng)
        } else {
            defaultLocation
        }

        val distance = if (departureCoordinates != null && arrivalCoordinates != null) {
            calculateDistance(
                departureCoordinates.latitude,
                departureCoordinates.longitude,
                arrivalCoordinates.latitude,
                arrivalCoordinates.longitude
            )
        } else {
            0.0
        }

        val zoomLevel = calculateZoomLevel(distance)
        val scaffoldState = rememberBottomSheetScaffoldState()
        val coroutineScope = rememberCoroutineScope()
        var weather by remember { mutableStateOf<WeatherInfo?>(null) }
        var showDialog by remember { mutableStateOf(false) }

        LaunchedEffect(midpoint, zoomLevel) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(midpoint, zoomLevel)
        }

        BottomSheetScaffold(scaffoldState = scaffoldState,
            sheetPeekHeight = 128.dp,
            sheetContent = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    flightDetails?.let { flight ->
                        Text(
                            text = stringResource(R.string.flight_details),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = stringResource(
                                R.string.flight_number_format,
                                flight.flightNumber ?: stringResource(R.string.unknown)
                            ), style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(
                                R.string.flight_date_format,
                                flight.flightDate ?: stringResource(R.string.unknown)
                            ), style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(
                                R.string.departure_airport_format, flight.departureAirport
                            ), style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(
                                R.string.arrival_airport_format, flight.arrivalAirport
                            ), style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(
                                R.string.airline_name_format,
                                flight.airlineName ?: stringResource(R.string.unknown)
                            ), style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState
                ) {
                    departureCoordinates?.let { departure ->
                        Marker(state = MarkerState(position = LatLng(departure.latitude, departure.longitude)),
                            title = departure.iata,
                            snippet = departure.iata,
                            onClick = {
                                coroutineScope.launch {
                                    weather = fetchWeather(LatLng(departure.latitude, departure.longitude))
                                    showDialog = true
                                }
                                false
                            })
                    }

                    arrivalCoordinates?.let { arrival ->
                        Marker(state = MarkerState(position = LatLng(arrival.latitude, arrival.longitude)),
                            title = arrival.iata,
                            snippet = arrival.iata,
                            onClick = {
                                coroutineScope.launch {
                                    weather = fetchWeather(LatLng(arrival.latitude, arrival.longitude))
                                    showDialog = true
                                }
                                false
                            })
                    }

                    if (departureCoordinates != null && arrivalCoordinates != null) {
                        Polyline(
                            points = listOf(
                                LatLng(
                                    departureCoordinates.latitude, departureCoordinates.longitude
                                ), LatLng(arrivalCoordinates.latitude, arrivalCoordinates.longitude)
                            ), color = Color.Blue, width = 5f
                        )
                    }

                    if (showDialog && weather != null) {
                        AlertDialog(onDismissRequest = { showDialog = false },
                            title = { Text(stringResource(R.string.weather)) },
                            text = {
                                Column {
                                    Text(
                                        stringResource(
                                            R.string.location_format,
                                            weather?.locationName ?: stringResource(R.string.not_available)
                                        ))
                                    Text(
                                        stringResource(
                                            R.string.temperature_format,
                                            weather?.temperature ?: stringResource(R.string.not_available)
                                        ))
                                    Text(
                                        stringResource(
                                            R.string.description_format,
                                            weather?.description ?: stringResource(R.string.not_available)
                                        ))
                                }
                            },
                            confirmButton = {
                                Button(onClick = { showDialog = false }) {
                                    Text(stringResource(R.string.close))
                                }
                            })
                    }
                }
                TopAppBar(title = {}, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ), navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(color = MaterialTheme.colorScheme.background)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.go_back)
                        )
                    }
                })

            }
        }
    }
}

fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371.0

    val latDistance = Math.toRadians(lat2 - lat1)
    val lonDistance = Math.toRadians(lon2 - lon1)

    val a =
        sin(latDistance / 2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(
            lonDistance / 2
        ).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadius * c
}

fun calculateZoomLevel(distance: Double): Float {
    return when {
        distance < 100 -> 9f
        distance < 400 -> 8f
        distance < 1000 -> 7f
        distance < 1500 -> 6f
        distance < 2000 -> 5f
        distance < 5000 -> 4f
        distance < 10000 -> 3f
        else -> 1f
    }
}

suspend fun fetchWeather(latLng: LatLng): WeatherInfo = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val languageCode = Locale.getDefault().language
    val url =
        "https://api.openweathermap.org/data/2.5/weather?lat=${latLng.latitude}&lon=${latLng.longitude}&units=metric&lang=$languageCode&appid=${BuildConfig.OPENWEATHER_API_KEY}"

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