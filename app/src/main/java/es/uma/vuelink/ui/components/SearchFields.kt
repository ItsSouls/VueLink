package es.uma.vuelink.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.uma.vuelink.R

@Composable
fun SearchFields(
    searchFlightNumber: String,
    onFlightNumberChange: (String) -> Unit,
    searchDepartureAirport: String,
    onDepartureChange: (String) -> Unit,
    searchArrivalAirport: String,
    onArrivalChange: (String) -> Unit,
    selectedDate: String,
    onDateClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val listContentPadding = WindowInsets.safeDrawing.only(WindowInsetsSides.Top).asPaddingValues()

    Column(
        modifier = Modifier
            .padding(listContentPadding)
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchFlightNumber,
            onValueChange = onFlightNumberChange,
            label = { Text(stringResource(R.string.search_flight_number)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchDepartureAirport,
                onValueChange = onDepartureChange,
                label = { Text(stringResource(R.string.departure)) },
                leadingIcon = { Icon(Icons.Filled.FlightTakeoff, contentDescription = null) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = searchArrivalAirport,
                onValueChange = onArrivalChange,
                label = { Text(stringResource(R.string.arrival)) },
                leadingIcon = { Icon(Icons.Filled.FlightLand, contentDescription = null) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = selectedDate,
            onValueChange = {},
            label = { Text(stringResource(R.string.date)) },
            readOnly = true,
            trailingIcon = {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = stringResource(R.string.choose_date),
                    modifier = Modifier.clickable(onClick = onDateClick)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onSearchClick, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.search_flights))
        }
    }
}