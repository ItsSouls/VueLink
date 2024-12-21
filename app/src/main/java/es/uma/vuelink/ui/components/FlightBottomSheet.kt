package es.uma.vuelink.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.uma.vuelink.R
import es.uma.vuelink.data.FlightWithAirports

@Composable
fun FlightBottomSheet(flightWithAirports: FlightWithAirports?) {
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
                ),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(
                    R.string.airline_format,
                    it.flight.airlineName ?: stringResource(R.string.unknown)
                ),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(
                    R.string.flight_date_format,
                    it.flight.flightDate ?: stringResource(R.string.unknown)
                ),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(
                    R.string.departure_airport_format, it.departureAirport.name
                ),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(
                    R.string.arrival_airport_format, it.arrivalAirport.name
                ),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}