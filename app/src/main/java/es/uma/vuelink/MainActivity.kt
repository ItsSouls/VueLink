package es.uma.vuelink

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Airlines
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import es.uma.vuelink.data.AirportCoordinates
import es.uma.vuelink.data.AppDatabase
import es.uma.vuelink.data.FlightDao
import es.uma.vuelink.data.FlightEntity
import es.uma.vuelink.data.loadAirportCoordinates
import es.uma.vuelink.model.Flight
import es.uma.vuelink.model.FlightResponse
import es.uma.vuelink.ui.theme.VueLinkTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        var flightDao = AppDatabase.getInstance(this).flightDao()

        setContent {
            VueLinkTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "search") {
                    composable("search") {
                        FlightSearchScreen(navController, flightDao)
                    }
                    composable("selected") {
                        SelectedFlightsScreen(navController, flightDao)
                    }
                    composable("map/{departureAirport}/{arrivalAirport}") { backStackEntry ->
                        val departureAirport =
                            backStackEntry.arguments?.getString("departureAirport")
                        val arrivalAirport = backStackEntry.arguments?.getString("arrivalAirport")


                        flightDao = AppDatabase.getInstance(LocalContext.current).flightDao()

                        val airportCoordinatesList = loadAirportCoordinates(LocalContext.current)

                        val flightDetails = remember { mutableStateOf<FlightEntity?>(null) }

                        LaunchedEffect(departureAirport, arrivalAirport) {
                            if (!departureAirport.isNullOrEmpty() && !arrivalAirport.isNullOrEmpty()) {
                                flightDetails.value =
                                    flightDao.getFlightDetails(departureAirport, arrivalAirport)
                            }
                        }

                        AirportMapScreen(
                            navController,
                            departureAirport,
                            arrivalAirport,
                            airportCoordinatesList,
                            flightDetails.value
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightSearchScreen(navController: NavHostController, flightDao: FlightDao) {
    VueLinkTheme {
        var searchQuery by remember { mutableStateOf("") }
        var flights by remember { mutableStateOf<List<Flight>>(emptyList()) }
        var loading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val textFieldState = rememberTextFieldState()
        var expanded by rememberSaveable { mutableStateOf(false) }

        fun launchSearch() {
            if (searchQuery.isNotBlank()) {
                loading = true
                errorMessage = null

                scope.launch {
                    try {
                        val flightResponse = withContext(Dispatchers.IO) { fetchFlightsFromApi() }

                        flights = flightResponse.data.filter { flight ->
                            val flightIata =
                                flight.flight.iata?.contains(searchQuery, ignoreCase = true) == true
                            val departureIATA =
                                flight.departure.iata.contains(searchQuery, ignoreCase = true)
                            val arrivalIATA =
                                flight.arrival.iata.contains(searchQuery, ignoreCase = true)
                            val airlineName = flight.airline.name?.contains(
                                searchQuery, ignoreCase = true
                            ) == true
                            flightIata || airlineName || departureIATA || arrivalIATA
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

        Scaffold(topBar = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                SearchBar(
                    modifier = Modifier,
                    inputField = {
                        SearchBarDefaults.InputField(
                            onSearch = {
                                expanded = false
                                launchSearch()
                            },
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            placeholder = { Text(stringResource(R.string.search_flights)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search, contentDescription = null
                                )
                            },
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                        )
                    },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        var resultText = "3U4828"
                        ListItem(headlineContent = { Text(resultText) },
                            supportingContent = { Text(stringResource(R.string.search_flight_number)) },
                            leadingContent = {
                                Icon(
                                    Icons.Filled.Flight, contentDescription = null
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier
                                .clickable {
                                    textFieldState.setTextAndPlaceCursorAtEnd(resultText)
                                    expanded = false
                                }
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp))
                        resultText = "Air Travel"
                        ListItem(headlineContent = { Text(resultText) },
                            supportingContent = { Text(stringResource(R.string.search_airline)) },
                            leadingContent = {
                                Icon(
                                    Icons.Filled.Airlines, contentDescription = null
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier
                                .clickable {
                                    textFieldState.setTextAndPlaceCursorAtEnd(resultText)
                                    expanded = false
                                }
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp))
                    }
                }
            }
        }, bottomBar = {
            BottomAppBar {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Button(
                        onClick = { navController.navigate("selected") },
                    ) {
                        Text(stringResource(R.string.view_saved_flights))
                    }
                }
            }
        }) { innerPadding ->

            if (loading) {
                Text(
                    text = stringResource(R.string.loading_flights), Modifier.padding(innerPadding)
                )
            }

            errorMessage?.let {
                Text(text = it, color = Color.Red, modifier = Modifier.padding(innerPadding))
            }

            LazyColumn(
                contentPadding = innerPadding,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.semantics { traversalIndex = 1f },
            ) {
                items(flights) { flight ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .padding(horizontal = 16.dp),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = stringResource(R.string.flight, flight.flightDate))
                                Text(text = stringResource(R.string.status, flight.flightStatus))
                                Text(
                                    text = stringResource(
                                        R.string.departure, flight.departure.airport
                                    )
                                )
                                Text(
                                    text = stringResource(
                                        R.string.arrival, flight.arrival.airport
                                    )
                                )
                                Text(
                                    text = stringResource(
                                        R.string.airline,
                                        flight.airline.name
                                            ?: stringResource(R.string.not_available)
                                    )
                                )
                                Text(
                                    text = stringResource(
                                        R.string.flight_number,
                                        flight.flight.iata ?: stringResource(R.string.not_available)
                                    )
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
                                }, modifier = Modifier.align(Alignment.CenterVertically)
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

@Composable
fun SelectedFlightsScreen(navController: NavHostController, flightDao: FlightDao) {
    VueLinkTheme {
        val selectedFlights = remember { mutableStateListOf<FlightEntity>() }
        val scope = rememberCoroutineScope() // Corutina scope
        var showDeleteDialog by remember { mutableStateOf(false) } // Estado para el diálogo
        var flightToDelete by remember { mutableStateOf<FlightEntity?>(null) } // Vuelo seleccionado para borrar

        LaunchedEffect(Unit) {
            val flights = flightDao.getAllFlights()
            selectedFlights.clear()
            selectedFlights.addAll(flights)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.saved_flights),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (selectedFlights.isEmpty()) {
                Text(text = stringResource(R.string.no_saved_flights))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f) // Permitir que la lista ocupe una parte proporcional del espacio
                        .fillMaxWidth()
                ) {
                    items(selectedFlights) { flight ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .padding(horizontal = 16.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(
                                        R.string.flight,
                                        flight.flightDate ?: stringResource(R.string.not_available)
                                    ),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(text = stringResource(R.string.status, flight.flightStatus))
                                Text(
                                    text = stringResource(
                                        R.string.departure,
                                        flight.departureAirport
                                    )
                                )
                                Text(
                                    text = stringResource(R.string.arrival, flight.arrivalAirport)
                                )
                                Text(
                                    text = stringResource(
                                        R.string.airline,
                                        flight.airlineName ?: stringResource(R.string.not_available)
                                    )
                                )
                                Text(
                                    text = stringResource(
                                        R.string.flight_number,
                                        flight.flightNumber
                                            ?: stringResource(R.string.not_available)
                                    )
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    // Botón para ver el mapa
                                    Button(
                                        onClick = {
                                            val departureIATA = flight.departureIATA
                                            val arrivalIATA = flight.arrivalIATA
                                            navController.navigate("map/$departureIATA/$arrivalIATA")
                                        }
                                    ) {
                                        Text(stringResource(R.string.show_map))
                                    }

                                    Spacer(modifier = Modifier.width(8.dp)) // Espaciado entre los botones

                                    // Botón para mostrar el diálogo de confirmación
                                    Button(
                                        onClick = {
                                            // Asignamos el vuelo a eliminar y mostramos el diálogo
                                            flightToDelete = flight
                                            showDeleteDialog = true
                                        }
                                    ) {
                                        Text(stringResource(R.string.remove_flight))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón centrado horizontalmente, consistente con el otro botón
            Button(
                onClick = { navController.navigate("search") },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(stringResource(R.string.return_to_search))
            }
        }

        // Diálogo de confirmación para borrar el vuelo
        if (showDeleteDialog && flightToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    // Cerrar el diálogo si el usuario hace clic fuera de él
                    showDeleteDialog = false
                },
                title = {
                    Text(stringResource(R.string.confirm_delete))
                },
                text = {
                    Text(stringResource(R.string.are_you_sure_delete, flightToDelete?.flightNumber ?: stringResource(R.string.not_available)))
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // Eliminar el vuelo de la base de datos y la lista
                            scope.launch {
                                flightDao.deleteFlight(flightToDelete!!)
                                selectedFlights.remove(flightToDelete) // Eliminar de la lista
                            }
                            showDeleteDialog = false // Cerrar el diálogo
                        }
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            // Solo cerrar el diálogo sin hacer nada
                            showDeleteDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}


fun fetchFlightsFromApi(): FlightResponse {
    val client = OkHttpClient()
    val url =
        "https://api.aviationstack.com/v1/flights?access_key=${BuildConfig.AVIATIONSTACK_API_KEY}"
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

@Composable
fun AirportMapScreen(
    navController: NavHostController,  // Asegúrate de pasar el navController aquí
    departureAirport: String?,
    arrivalAirport: String?,
    airportCoordinatesList: List<AirportCoordinates>,
    flightDetails: FlightEntity?
) {
    VueLinkTheme {
        val defaultLocation = LatLng(40.416775, -3.703790) // Madrid as default
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(defaultLocation, 5f)
        }

        // Buscar las coordenadas de los aeropuertos de salida y destino usando los códigos IATA
        val departureCoordinates = airportCoordinatesList.find { it.iata == departureAirport }
        val arrivalCoordinates = airportCoordinatesList.find { it.iata == arrivalAirport }

        // Calcular el punto medio de la ruta si ambos aeropuertos tienen coordenadas
        val midpoint = if (departureCoordinates != null && arrivalCoordinates != null) {
            val midLat = (departureCoordinates.latitude + arrivalCoordinates.latitude) / 2
            val midLng = (departureCoordinates.longitude + arrivalCoordinates.longitude) / 2
            LatLng(midLat, midLng)
        } else {
            defaultLocation // Si no se encuentran las coordenadas, usar la ubicación por defecto
        }

        // Calcular la distancia entre los dos aeropuertos y ajustar el zoom
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

        // Calcular el zoom basado en la distancia (ajustar este factor según lo necesites)
        val zoomLevel = calculateZoomLevel(distance)

        // Actualizar la posición de la cámara para que se centre en la ruta
        LaunchedEffect(midpoint, zoomLevel) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(midpoint, zoomLevel)
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Mapa ocupa la mitad superior de la pantalla
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Mapa ocupa la mitad de la pantalla
                cameraPositionState = cameraPositionState
            ) {
                // Dibujar los marcadores para los aeropuertos
                departureCoordinates?.let {
                    Marker(
                        state = MarkerState(position = LatLng(it.latitude, it.longitude)),
                        title = it.iata,
                        snippet = it.iata
                    )
                }

                arrivalCoordinates?.let {
                    Marker(
                        state = MarkerState(position = LatLng(it.latitude, it.longitude)),
                        title = it.iata,
                        snippet = it.iata
                    )
                }

                // Dibujar la polyline si ambos aeropuertos están disponibles
                if (departureCoordinates != null && arrivalCoordinates != null) {
                    Polyline(
                        points = listOf(
                            LatLng(departureCoordinates.latitude, departureCoordinates.longitude),
                            LatLng(arrivalCoordinates.latitude, arrivalCoordinates.longitude)
                        ), color = androidx.compose.ui.graphics.Color.Blue, // Color de la línea
                        width = 5f // Ancho de la línea
                    )
                }
            }

            // Información sobre el vuelo y botones debajo del mapa
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                flightDetails?.let { flight ->
                    Text(text = "Fecha de vuelo: ${flight.flightDate}")
                    Text(text = "Aeropuerto de salida: ${flight.departureAirport}")
                    Text(text = "Aeropuerto de llegada: ${flight.arrivalAirport}")
                    Text(text = "Nombre de la aerolínea: ${flight.airlineName ?: R.string.unknown}")
                    Text(text = "Número de vuelo: ${flight.flightNumber ?: R.string.unknown}")
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            navController.popBackStack()
                        }, modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.go_back))
                    }

                    Button(
                        onClick = {
                            navController.navigate("search")
                        }, modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.search_more_flights))
                    }
                }
            }
        }
    }
}


// Función para calcular la distancia entre dos coordenadas geográficas usando la fórmula Haversine
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371.0 // Radio de la Tierra en kilómetros

    val latDistance = Math.toRadians(lat2 - lat1)
    val lonDistance = Math.toRadians(lon2 - lon1)

    val a =
        sin(latDistance / 2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(
            lonDistance / 2
        ).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadius * c // Distancia en kilómetros
}

fun calculateZoomLevel(distance: Double): Float {
    return when {
        distance < 100 -> 9f  // Muy cerca (alrededor de la ciudad)
        distance < 400 -> 8f  // Cerca (alrededor de la ciudad)
        distance < 1000 -> 7f   // Distancia media (entre ciudades o regiones)
        distance < 1500 -> 6f   // Distancia media (entre ciudades o regiones)
        distance < 2000 -> 5f   // Para distancias medias
        distance < 5000 -> 4f   // Distancia larga (países cercanos)
        distance < 10000 -> 3f // Distancia larga (países cercanos)
        else -> 1f              // Distancias globales
    }
}


