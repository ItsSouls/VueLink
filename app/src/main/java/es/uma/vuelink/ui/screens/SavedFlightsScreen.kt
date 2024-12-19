package es.uma.vuelink.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import es.uma.vuelink.R
import es.uma.vuelink.data.FlightDao
import es.uma.vuelink.data.FlightEntity
import es.uma.vuelink.ui.theme.VueLinkTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedFlightsScreen(navController: NavHostController, flightDao: FlightDao) {
    VueLinkTheme {
        val savedFlights = remember { mutableStateListOf<FlightEntity>() }
        val scope = rememberCoroutineScope()
        var showDeleteDialog by remember { mutableStateOf(false) }
        var flightToDelete by remember { mutableStateOf<FlightEntity?>(null) }
        val listState = rememberLazyListState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

        LaunchedEffect(Unit) {
            val flights = flightDao.getAllFlights()
            savedFlights.clear()
            savedFlights.addAll(flights)
        }

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(title = {
                    Text(
                        text = stringResource(R.string.saved_flights),
                    )
                }, navigationIcon = {
                    Box(
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Localized description"
                            )
                        }
                    }
                }, scrollBehavior = scrollBehavior
                )
            },
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {

                    if (savedFlights.isEmpty()) {
                        Text(text = stringResource(R.string.no_saved_flights))
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(), state = listState
                        ) {
                            items(savedFlights) { flight ->
                                val cardColor = when (flight.flightStatus) {
                                    "cancelled" -> Color.Red
                                    "active" -> Color.Green
                                    else -> MaterialTheme.colorScheme.surface
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .padding(horizontal = 16.dp),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(cardColor.copy(alpha = 0.3f))  // Aplicar el fondo con opacidad
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = stringResource(
                                                    R.string.flight_number,
                                                    flight.flightNumber
                                                        ?: stringResource(R.string.not_available)
                                                )
                                            )
                                            Text(
                                                text = stringResource(
                                                    R.string.airline,
                                                    flight.airlineName
                                                        ?: stringResource(R.string.not_available)
                                                )
                                            )
                                            Text(
                                                text = stringResource(
                                                    R.string.flight_date,
                                                    flight.flightDate
                                                        ?: stringResource(R.string.not_available)
                                                )
                                            )
                                            Text(
                                                text = stringResource(
                                                    R.string.status, flight.flightStatus
                                                )
                                            )
                                            Text(
                                                text = stringResource(
                                                    R.string.departure, flight.departureAirport
                                                )
                                            )
                                            Text(
                                                text = stringResource(
                                                    R.string.arrival, flight.arrivalAirport
                                                )
                                            )

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(56.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Button(onClick = {
                                                    val departureIATA = flight.departureIATA
                                                    val arrivalIATA = flight.arrivalIATA
                                                    navController.navigate("map/$departureIATA/$arrivalIATA")
                                                }) {
                                                    Text(stringResource(R.string.show_map))
                                                }

                                                Spacer(modifier = Modifier.width(8.dp))

                                                Button(onClick = {
                                                    flightToDelete = flight
                                                    showDeleteDialog = true
                                                }) {
                                                    Text(stringResource(R.string.remove_flight))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
        )

        if (showDeleteDialog && flightToDelete != null) {
            AlertDialog(onDismissRequest = {
                showDeleteDialog = false
            }, title = {
                Text(stringResource(R.string.confirm_delete))
            }, text = {
                Text(
                    stringResource(
                        R.string.are_you_sure_delete,
                        flightToDelete?.flightNumber ?: stringResource(R.string.not_available)
                    )
                )
            }, confirmButton = {
                Button(onClick = {
                    scope.launch {
                        flightDao.deleteFlight(flightToDelete!!)
                        savedFlights.remove(flightToDelete)
                    }
                    showDeleteDialog = false
                }) {
                    Text(stringResource(R.string.confirm))
                }
            }, dismissButton = {
                Button(onClick = {
                    showDeleteDialog = false
                }) {
                    Text(stringResource(R.string.cancel))
                }
            })
        }
    }
}
