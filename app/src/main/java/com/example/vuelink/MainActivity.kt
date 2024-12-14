package com.example.vuelink

import Flight
import FlightResponse
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
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
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

private val apiKey = "44851a0ee844dcfc3cbbee44794d01b1"

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightSearchScreen(navController: NavHostController, flightDao: FlightDao) {
    var searchQuery by remember { mutableStateOf("") }
    var flights by remember { mutableStateOf<List<Flight>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Usar `LaunchedEffect` para manejar la búsqueda
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            loading = true
            errorMessage = null

            // Hacer la solicitud HTTP y filtrar resultados
            try {
                val response = fetchFlightsFromApi()
                if (response.isSuccessful) {
                    val flightResponse = Gson().fromJson(
                        response.body?.string(),
                        FlightResponse::class.java
                    )
                    flights = flightResponse.data.filter {
                        it.flight.number?.contains(searchQuery, ignoreCase = true) == true ||
                                it.airline.name?.contains(searchQuery, ignoreCase = true) == true
                    }
                } else {
                    errorMessage = "Error: ${response.code}"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.localizedMessage}"
            } finally {
                loading = false
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Buscar vuelos",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Número de vuelo o aerolínea") },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { /* No es necesario hacer nada aquí */ }) {
                Text("Buscar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (loading) {
            Text(text = "Cargando vuelos...")
        }

        errorMessage?.let {
            Text(text = it, color = androidx.compose.ui.graphics.Color.Red)
        }

        flights.forEach { flight ->
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(text = "Vuelo: ${flight.flight_date}")
                Text(text = "Estado: ${flight.flight_status}")
                Text(text = "Salida: ${flight.departure.airport}")
                Text(text = "Llegada: ${flight.arrival.airport}")
                Text(text = "Aerolínea: ${flight.airline.name ?: "No disponible"}")
                Text(text = "Número de vuelo: ${flight.flight.number ?: "No disponible"}")

                Spacer(modifier = Modifier.height(4.dp))

                // Ahora, el código del botón
                Button(onClick = {
                    val flightEntity = FlightEntity(
                        flightDate = flight.flight_date,
                        flightStatus = flight.flight_status,
                        departureAirport = flight.departure.airport ?: "No disponible",
                        arrivalAirport = flight.arrival.airport ?: "No disponible",
                        airlineName = flight.airline.name,
                        flightNumber = flight.flight.number
                    )
                    // Aquí invocamos la función que maneja la inserción de forma segura
                    insertFlightToDatabase(flightEntity, flightDao)
                }) {
                    Text("Seleccionar")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate("selected") }) {
            Text("Ver vuelos seleccionados")
        }
    }
}

@Composable
fun insertFlightToDatabase(flightEntity: FlightEntity, flightDao: FlightDao) {
    // Aquí utilizamos LaunchedEffect para invocar la corutina de forma segura
    LaunchedEffect(flightEntity) {
        flightDao.insertFlight(flightEntity) // Inserta el vuelo en la base de datos
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
            text = "Vuelos seleccionados",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (selectedFlights.isEmpty()) {
            Text(text = "No hay vuelos seleccionados.")
        } else {
            selectedFlights.forEach { flight ->
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(text = "Vuelo: ${flight.flightDate}")
                    Text(text = "Estado: ${flight.flightStatus}")
                    Text(text = "Salida: ${flight.departureAirport}")
                    Text(text = "Llegada: ${flight.arrivalAirport}")
                    Text(text = "Aerolínea: ${flight.airlineName ?: "No disponible"}")
                    Text(text = "Número de vuelo: ${flight.flightNumber ?: "No disponible"}")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("search") }) {
            Text("Volver a buscar")
        }
    }
}

// Función para hacer la solicitud HTTP a AviationStack
suspend fun fetchFlightsFromApi(): Response {
    val client = OkHttpClient()
    val url = "https://api.aviationstack.com/v1/flights?access_key=$apiKey"
    val request = Request.Builder().url(url).build()
    return client.newCall(request).execute()
}

