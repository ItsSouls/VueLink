package es.uma.vuelink.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Polyline
import es.uma.vuelink.R
import es.uma.vuelink.data.AirportCoordinates
import es.uma.vuelink.data.FlightWithAirports

@Composable
fun FlightMap(
    cameraPositionState: CameraPositionState,
    departureCoordinates: AirportCoordinates?,
    arrivalCoordinates: AirportCoordinates?,
    flightWithAirports: FlightWithAirports?
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
                coordinates = LatLng(arrival.latitude, arrival.longitude), title = stringResource(
                    R.string.arrival_airport_format,
                    flightWithAirports?.arrivalAirport?.name ?: stringResource(R.string.unknown)
                ), scheduledTime = flightWithAirports?.arrivalAirport?.scheduled ?: ""
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
}