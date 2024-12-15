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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
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
import android.widget.Toast
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

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
                        val flightIata = flight.flight.iata?.contains(searchQuery, ignoreCase = true) == true
                        val airlineName = flight.airline.name?.contains(searchQuery, ignoreCase = true) == true
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
            Button(onClick = { launchSearch() }) {
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

        // LazyColumn con los vuelos
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
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
                            Text(text = "Vuelo: ${flight.flight_date}")
                            Text(text = "Estado: ${flight.flight_status}")
                            Text(text = "Salida: ${flight.departure.airport}")
                            Text(text = "Llegada: ${flight.arrival.airport}")
                            Text(text = "Aerolínea: ${flight.airline.name}")
                            Text(text = "Número de vuelo: ${flight.flight.iata ?: "No disponible"}")
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
                                        Toast.makeText(context, "Vuelo guardado exitosamente", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterVertically)  // Alinear el botón verticalmente
                        ) {
                            Text("Seleccionar")
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
            Text("Ver vuelos seleccionados")
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

suspend fun fetchFlightsFromApi(): FlightResponse {
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







