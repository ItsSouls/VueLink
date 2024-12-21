package es.uma.vuelink.ui.screens

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AirplaneTicket
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import es.uma.vuelink.ui.components.FlightsList
import es.uma.vuelink.ui.components.SearchFields
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

@Composable
fun FlightSearchScreen(
    navController: NavHostController, flightDao: FlightDao, airportDao: AirportDao
) {
    var searchFlightNumber by rememberSaveable { mutableStateOf("") }
    var searchDepartureAirport by rememberSaveable { mutableStateOf("") }
    var searchArrivalAirport by rememberSaveable { mutableStateOf("") }
    var selectedDate by rememberSaveable { mutableStateOf("") }
    var flights by rememberSaveable { mutableStateOf<List<Flight>>(emptyList()) }
    var loading by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val openDialog = remember { mutableStateOf(false) }

    if (openDialog.value) {
        CalendarDialog(onDismiss = { openDialog.value = false }, onDateSelected = {
            selectedDate = it
            openDialog.value = false
        })
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
                            ?.contains(searchFlightNumber.lowercase()) == true || flight.airline.name?.lowercase()
                            ?.contains(searchFlightNumber.lowercase()) == true
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
        if (flights.isEmpty() && !loading) {
            launchSearch()
        }
    }

    Scaffold(topBar = {
        SearchFields(searchFlightNumber = searchFlightNumber,
            onFlightNumberChange = { searchFlightNumber = it },
            searchDepartureAirport = searchDepartureAirport,
            onDepartureChange = { searchDepartureAirport = it },
            searchArrivalAirport = searchArrivalAirport,
            onArrivalChange = { searchArrivalAirport = it },
            selectedDate = selectedDate,
            onDateClick = { openDialog.value = true },
            onSearchClick = { launchSearch() })
    }, floatingActionButton = {
        FloatingActionButton(onClick = { navController.navigate("saved") }) {
            Icon(
                Icons.AutoMirrored.Filled.AirplaneTicket,
                contentDescription = stringResource(R.string.view_saved_flights)
            )
        }
    }) { innerPadding ->
        FlightsList(innerPadding = innerPadding,
            flights = flights,
            loading = loading,
            errorMessage = errorMessage,
            onSaveClick = { flight ->
                scope.launch(Dispatchers.IO) {
                    try {
                        val departureAirportId =
                            airportDao.getAirportByIATA(flight.departure.iata)?.id
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
            })
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