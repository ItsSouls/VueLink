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
import androidx.compose.material3.BottomSheetScaffold
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import es.uma.vuelink.R
import es.uma.vuelink.data.AirportCoordinates
import es.uma.vuelink.data.FlightWithAirports
import es.uma.vuelink.ui.components.WeatherMarkerInfo
import es.uma.vuelink.ui.theme.VueLinkTheme
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirportMapScreen(
    navController: NavHostController,
    flightWithAirports: FlightWithAirports?,
    airportCoordinatesList: List<AirportCoordinates>
) {
    VueLinkTheme {
        val defaultLocation = LatLng(40.416775, -3.703790)
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(defaultLocation, 5f)
        }

        val departureCoordinates = airportCoordinatesList.find {
            it.iata == (flightWithAirports?.departureAirport?.iata ?: "")
        }
        val arrivalCoordinates = airportCoordinatesList.find {
            it.iata == (flightWithAirports?.arrivalAirport?.iata ?: "")
        }

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
                    flightWithAirports?.let {
                        Text(
                            text = stringResource(
                                R.string.flight_number_format,
                                it.flight.flightNumber ?: stringResource(R.string.unknown)
                            ), style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(
                                R.string.airline_format,
                                it.flight.airlineName ?: stringResource(R.string.unknown)
                            ), style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(
                                R.string.flight_date_format,
                                it.flight.flightDate ?: stringResource(R.string.unknown)
                            ), style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(
                                R.string.departure_airport_format, it.departureAirport.name
                            ), style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(
                                R.string.arrival_airport_format, it.arrivalAirport.name
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
                        WeatherMarkerInfo(
                            coordinates = LatLng(departure.latitude, departure.longitude),
                            title = stringResource(
                                R.string.departure_airport_format,
                                flightWithAirports?.departureAirport?.name ?: stringResource(R.string.unknown)
                            ),
                            scheduledTime = flightWithAirports?.departureAirport?.scheduled ?: ""
                        )
                    }

                    arrivalCoordinates?.let { arrival ->
                        WeatherMarkerInfo(
                            coordinates = LatLng(arrival.latitude, arrival.longitude),
                            title = stringResource(
                                R.string.arrival_airport_format,
                                flightWithAirports?.arrivalAirport?.name ?: stringResource(R.string.unknown)
                            ),
                            scheduledTime = flightWithAirports?.arrivalAirport?.scheduled ?: ""
                        )
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