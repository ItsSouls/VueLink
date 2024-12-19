package es.uma.vuelink.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AirplaneTicket
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import es.uma.vuelink.R
import es.uma.vuelink.data.FlightDao
import es.uma.vuelink.data.FlightEntity
import es.uma.vuelink.model.Flight
import es.uma.vuelink.ui.components.fetchFlightsFromApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

@Composable
fun FlightSearchScreen(navController: NavHostController, flightDao: FlightDao) {
    var searchFlightNumber by remember { mutableStateOf("") }
    var searchDepartureAirport by remember { mutableStateOf("") }
    var searchArrivalAirport by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var flights by remember { mutableStateOf<List<Flight>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val calendar = Calendar.getInstance()
    val datePicker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedDate = "$year-${month + 1}-$dayOfMonth"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    fun launchSearch() {
        loading = true
        errorMessage = null
        scope.launch {
            try {

                val flightResponse = withContext(Dispatchers.IO) { fetchFlightsFromApi() }
                flights = flightResponse.data.filter { flight ->
                    val matchesFlightNumber = searchFlightNumber.isBlank() ||
                            flight.flight.iata?.lowercase()?.contains(searchFlightNumber.lowercase()) == true
                    val matchesDepartureAirport = searchDepartureAirport.isBlank() ||
                            flight.departure.airport.lowercase() == searchDepartureAirport.lowercase()
                    val matchesArrivalAirport = searchArrivalAirport.isBlank() ||
                            flight.arrival.airport.lowercase() == searchArrivalAirport.lowercase()
                    val matchesDate = selectedDate.isBlank() || flight.flightDate == selectedDate
                    matchesFlightNumber && matchesDepartureAirport && matchesArrivalAirport && matchesDate
                }

            } catch (e: Exception) {
                errorMessage = "Error: ${e.localizedMessage}"
            } finally {
                loading = false
            }
        }
    }

    Scaffold(
        topBar = {
            Column(Modifier.padding(16.dp)) {
                // Search by Flight Number
                OutlinedTextField(
                    value = searchFlightNumber,
                    onValueChange = { searchFlightNumber = it },
                    label = { Text(stringResource(R.string.search_flight_number)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Row for Departure and Arrival Airports
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Search by Departure Airport
                    OutlinedTextField(
                        value = searchDepartureAirport,
                        onValueChange = { searchDepartureAirport = it },
                        label = { Text("Salida") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp) // Bordes redondeados
                    )

                    // Search by Arrival Airport
                    OutlinedTextField(
                        value = searchArrivalAirport,
                        onValueChange = { searchArrivalAirport = it },
                        label = { Text("Llegada") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp) // Bordes redondeados
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row for Date Selection
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedDate,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.select_date)) },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = stringResource(R.string.choose_date),
                                modifier = Modifier.clickable { datePicker.show() }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp) // Bordes redondeados
                    )
                }



                Spacer(modifier = Modifier.height(16.dp))

                // Search Button
                Button(
                    onClick = { launchSearch() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.search_flights))
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("saved") }) {
                Icon(
                    Icons.AutoMirrored.Filled.AirplaneTicket,
                    contentDescription = stringResource(R.string.view_saved_flights)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                } ?: LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(flights) { flight ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = stringResource(R.string.flight, flight.flightDate),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = stringResource(R.string.departure, flight.departure.airport),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = stringResource(R.string.arrival, flight.arrival.airport),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Button(
                                    onClick = {
                                        val flightEntity = FlightEntity(
                                            flightDate = flight.flightDate,
                                            flightStatus = flight.flightStatus,
                                            departureAirport = flight.departure.airport,
                                            arrivalAirport = flight.arrival.airport,
                                            departureIATA = flight.departure.iata,
                                            arrivalIATA = flight.arrival.iata,
                                            airlineName = flight.airline.name,
                                            flightNumber = flight.flight.iata
                                        )

                                        scope.launch(Dispatchers.IO) {
                                            val existingFlight = flightDao.getFlightDetails(
                                                flight.departure.iata, flight.arrival.iata
                                            )

                                            if (existingFlight == null) {
                                                flightDao.insertFlight(flightEntity)
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        context,
                                                        context.getString(R.string.flight_saved_successfully),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            } else {
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        context,
                                                        context.getString(R.string.flight_already_saved),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.padding(start = 16.dp)
                                ) {
                                    Text(stringResource(R.string.save))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
