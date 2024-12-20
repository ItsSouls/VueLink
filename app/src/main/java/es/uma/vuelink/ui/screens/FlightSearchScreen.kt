package es.uma.vuelink.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AirplaneTicket
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.gson.Gson
import es.uma.vuelink.BuildConfig
import es.uma.vuelink.R
import es.uma.vuelink.data.AirportDao
import es.uma.vuelink.data.AirportEntity
import es.uma.vuelink.data.FlightDao
import es.uma.vuelink.data.FlightEntity
import es.uma.vuelink.model.Flight
import es.uma.vuelink.model.FlightResponse
import es.uma.vuelink.ui.components.CalendarDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

@Composable
fun FlightSearchScreen(navController: NavHostController, flightDao: FlightDao, airportDao: AirportDao) {
    var searchFlightNumber by remember { mutableStateOf("") }
    var searchDepartureAirport by remember { mutableStateOf("") }
    var searchArrivalAirport by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var flights by remember { mutableStateOf<List<Flight>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val openDialog = remember { mutableStateOf(false) }

    if (openDialog.value) {
        CalendarDialog(
            onDismiss = { openDialog.value = false },
            onDateSelected = {
                selectedDate = it
                openDialog.value = false
            }
        )
    }

    fun launchSearch() {
        loading = true
        errorMessage = null
        scope.launch {
            try {
                val flightResponse = withContext(Dispatchers.IO) { fetchFlights() }
                flights = flightResponse.data.filter { flight ->
                    val matchesFlightNumber =
                        searchFlightNumber.isBlank() || flight.flight.iata?.lowercase()
                            ?.contains(searchFlightNumber.lowercase()) == true || flight.airline.name?.lowercase()?.contains(searchFlightNumber.lowercase()) == true
                    val matchesDepartureAirport =
                        searchDepartureAirport.isBlank() || flight.departure.name.lowercase()
                            .contains(searchDepartureAirport.lowercase())
                    val matchesArrivalAirport =
                        searchArrivalAirport.isBlank() || flight.departure.name.lowercase()
                            .contains(searchArrivalAirport.lowercase())
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

    LaunchedEffect(Unit) {
        launchSearch()
    }

    val listContentPadding = WindowInsets.safeDrawing.only(WindowInsetsSides.Top).asPaddingValues()

    Scaffold(topBar = {
        Column(
            Modifier
                .padding(listContentPadding)
                .padding(16.dp)
        ) {
            // Search by Flight Number
            OutlinedTextField(value = searchFlightNumber,
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
                OutlinedTextField(value = searchDepartureAirport,
                    onValueChange = { searchDepartureAirport = it },
                    label = { Text(stringResource(R.string.departure)) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.FlightTakeoff, contentDescription = null
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                // Search by Arrival Airport
                OutlinedTextField(value = searchArrivalAirport,
                    onValueChange = { searchArrivalAirport = it },
                    label = { Text(stringResource(R.string.arrival)) },
                    leadingIcon = { Icon(Icons.Filled.FlightLand, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row for Date Selection
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(value = selectedDate,
                    onValueChange = {},
                    label = { Text(stringResource(R.string.date)) },
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Default.CalendarToday,
                            contentDescription = stringResource(R.string.choose_date),
                            modifier = Modifier.clickable {
                                openDialog.value = !openDialog.value
                            })
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp) // Bordes redondeados
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Button
            Button(
                onClick = { launchSearch() }, modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.search_flights))
            }
        }
    }, floatingActionButton = {
        FloatingActionButton(onClick = { navController.navigate("saved") }) {
            Icon(
                Icons.AutoMirrored.Filled.AirplaneTicket,
                contentDescription = stringResource(R.string.view_saved_flights)
            )
        }
    }) { innerPadding ->
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
                        color = MaterialTheme.colorScheme.error,
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
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 6.dp
                            ),
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
                                        text = stringResource(
                                            R.string.flight_number_format,
                                            flight.flight.iata ?: R.string.unknown
                                        ), style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = stringResource(
                                            R.string.departure_format, flight.departure.name
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = stringResource(
                                            R.string.arrival_format, flight.arrival.name
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Button(
                                    onClick = {
                                        scope.launch(Dispatchers.IO) {
                                            try {
                                                val departureAirportId = airportDao.getAirportByIATA(flight.departure.iata)?.id
                                                    ?: airportDao.insertAirport(
                                                        AirportEntity(
                                                            id = 0,
                                                            name = flight.departure.name,
                                                            timezone = flight.departure.timezone,
                                                            iata = flight.departure.iata,
                                                            icao = flight.departure.icao,
                                                            terminal = flight.departure.terminal,
                                                            gate = flight.departure.gate,
                                                            delay = flight.departure.delay,
                                                            scheduled = flight.departure.scheduled,
                                                            estimated = flight.departure.estimated,
                                                            actual = flight.departure.actual,
                                                            estimatedRunway = flight.departure.estimatedRunway,
                                                            actualRunway = flight.departure.actualRunway
                                                        )
                                                    ).toInt()

                                                val arrivalAirportId = airportDao.getAirportByIATA(flight.arrival.iata)?.id
                                                    ?: airportDao.insertAirport(
                                                        AirportEntity(
                                                            id = 0,
                                                            name = flight.arrival.name,
                                                            timezone = flight.arrival.timezone,
                                                            iata = flight.arrival.iata,
                                                            icao = flight.arrival.icao,
                                                            terminal = flight.arrival.terminal,
                                                            gate = flight.arrival.gate,
                                                            delay = flight.arrival.delay,
                                                            scheduled = flight.arrival.scheduled,
                                                            estimated = flight.arrival.estimated,
                                                            actual = flight.arrival.actual,
                                                            estimatedRunway = flight.arrival.estimatedRunway,
                                                            actualRunway = flight.arrival.actualRunway
                                                        )
                                                    ).toInt()

                                                val flightEntity = FlightEntity(
                                                    flightDate = flight.flightDate,
                                                    flightStatus = flight.flightStatus,
                                                    departureAirportId = departureAirportId,
                                                    arrivalAirportId = arrivalAirportId,
                                                    airlineName = flight.airline.name,
                                                    flightNumber = flight.flight.iata
                                                )

                                                val existingFlight = flightDao.getFlightWithAirportsByIata(
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
                                            } catch (e: Exception) {
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        context,
                                                        context.getString(R.string.error_saving_flight),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    }, modifier = Modifier.padding(start = 16.dp)
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

fun fetchFlights(): FlightResponse {
    val client = OkHttpClient()
    val url =
        "https://api.aviationstack.com/v1/flights?access_key=${BuildConfig.AVIATIONSTACK_API_KEY}"
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

        return Gson().fromJson(responseBody, FlightResponse::class.java)
    } catch (e: Exception) {
        println("Error en la llamada a la API: ${e.localizedMessage}")
        e.printStackTrace()
        throw e
    }
}