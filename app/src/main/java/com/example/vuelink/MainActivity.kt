package com.example.vuelink

import Flight
import FlightResponse
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vuelink.data.AppDatabase
import com.example.vuelink.data.FlightDao
import com.example.vuelink.data.FlightEntity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

private const val apiKey = "1ca4c88141825a87f533f8b64d9723af"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Base de datos
        val flightDao = AppDatabase.getInstance(this).flightDao()

        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "search") {
                composable("search") {
                    FlightSearchScreen(navController, flightDao)
                }
                composable("selected") {
                    SelectedFlightsScreen(navController, flightDao)
                }
            }
        }
    }
}

@Composable
fun FlightSearchScreen(navController: NavHostController, flightDao: FlightDao) {
    var searchQuery by remember { mutableStateOf("") }
    var flights by remember { mutableStateOf<List<Flight>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Función que ejecuta la búsqueda
    fun launchSearch() {
        if (searchQuery.isNotBlank()) {
            loading = true
            errorMessage = null

            // Ejecutar la llamada a la API
            scope.launch {
                try {
                    val flightResponse = withContext(Dispatchers.IO) { fetchFlightsFromApi() }

                    flights = flightResponse.data.filter { flight ->
                        val flightIata =
                            flight.flight.iata?.contains(searchQuery, ignoreCase = true) == true
                        val airlineName =
                            flight.airline.name.contains(searchQuery, ignoreCase = true)
                        flightIata || airlineName
                    }
                } catch (e: Exception) {
                    errorMessage = "Error: ${e.localizedMessage}"
                } finally {
                    loading = false
                }
            }
        } else {
            flights = emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.search_flights),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(stringResource(R.string.flight_number_or_airline)) },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { launchSearch() }) {
                Text(stringResource(R.string.search))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (loading) {
            Text(text = stringResource(R.string.loading_flights))
        }

        errorMessage?.let {
            Text(text = it, color = androidx.compose.ui.graphics.Color.Red)
        }

        // LazyColumn con los vuelos
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(flights) { flight ->
                // Envolver cada item de vuelo en un Card para darle un borde y sombra
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)  // Espacio entre los elementos
                        .padding(horizontal = 16.dp),  // Espacio a los lados
                    shape = MaterialTheme.shapes.medium,  // Bordes redondeados
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = stringResource(R.string.flight, flight.flight_date))
                            Text(text = stringResource(R.string.status, flight.flight_status))
                            Text(text = stringResource(R.string.departure, flight.departure.airport))
                            Text(text = stringResource(R.string.arrival, flight.arrival.airport))
                            Text(text = stringResource(R.string.airline, flight.airline.name))
                            Text(
                                text = stringResource(
                                    R.string.flight_number,
                                    flight.flight.iata ?: stringResource(R.string.not_available)
                                )
                            )
                        }

                        // Usar Modifier.align en el botón para centrarlo verticalmente
                        Button(
                            onClick = {
                                val flightEntity = FlightEntity(
                                    flightDate = flight.flight_date,
                                    flightStatus = flight.flight_status,
                                    departureAirport = flight.departure.airport,
                                    arrivalAirport = flight.arrival.airport,
                                    airlineName = flight.airline.name,
                                    flightNumber = flight.flight.iata
                                )
                                scope.launch(Dispatchers.IO) {
                                    flightDao.insertFlight(flightEntity)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.flight_saved_successfully),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterVertically)  // Alinear el botón verticalmente
                        ) {
                            Text(stringResource(R.string.select))
                        }
                    }
                }
            }
        }

        // Spacer para separar el botón de la lista
        Spacer(modifier = Modifier.height(16.dp))

        // Botón de "Ver vuelos seleccionados"
        Button(
            onClick = { navController.navigate("selected") },
            modifier = Modifier.align(Alignment.CenterHorizontally) // Coloca el botón en la parte inferior
        ) {
            Text(stringResource(R.string.view_selected_flights))
        }
    }
}

@Composable
fun SelectedFlightsScreen(navController: NavHostController, flightDao: FlightDao) {
    val selectedFlights = remember { mutableStateListOf<FlightEntity>() }

    LaunchedEffect(Unit) {
        val flights = flightDao.getAllFlights()
        selectedFlights.clear()
        selectedFlights.addAll(flights)
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(R.string.selected_flights),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (selectedFlights.isEmpty()) {
            Text(text = stringResource(R.string.no_selected_flights))
        } else {
            selectedFlights.forEach { flight ->
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(text = stringResource(R.string.flight, flight.flightDate))
                    Text(text = stringResource(R.string.status, flight.flightStatus))
                    Text(text = stringResource(R.string.departure, flight.departureAirport))
                    Text(text = stringResource(R.string.arrival, flight.arrivalAirport))
                    Text(text = stringResource(R.string.airline, flight.airlineName))
                    Text(
                        text = stringResource(
                            R.string.flight_number,
                            flight.flightNumber ?: stringResource(R.string.not_available)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("search") }) {
            Text(stringResource(R.string.return_to_search))
        }
    }
}

fun fetchFlightsFromApi(): FlightResponse {
    val client = OkHttpClient()
    val url = "https://api.aviationstack.com/v1/flights?access_key=$apiKey"
    val request = Request.Builder().url(url).build()

    // Intentar realizar la llamada a la API y capturar cualquier excepción
    try {
        // Realizar la llamada a la API en un hilo de fondo
        val response = client.newCall(request).execute()

        // Verifica si la respuesta fue exitosa
        if (!response.isSuccessful) {
            println("Código de estado: ${response.code}")
            throw Exception("Error en la llamada a la API: ${response.message}")
        }

        // Leer el cuerpo de la respuesta solo una vez
        val responseBody = response.body?.string()

        // Verifica si el cuerpo es nulo o vacío
        if (responseBody.isNullOrEmpty()) {
            throw Exception("Respuesta vacía de la API")
        }

        // Imprime la respuesta de la API para depuración
        println("Respuesta de la API: $responseBody")

        // Deserializar la respuesta
        return Gson().fromJson(responseBody, FlightResponse::class.java)
    } catch (e: Exception) {
        // Imprimir más detalles si algo sale mal
        println("Error en la llamada a la API: ${e.localizedMessage}")
        e.printStackTrace()  // Para obtener más detalles sobre el error
        throw e
    }
}







