package es.uma.vuelink.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import es.uma.vuelink.data.FlightDao
import es.uma.vuelink.data.FlightEntity
import es.uma.vuelink.data.loadAirportCoordinates


@Composable
fun AppNavigation(flightDao: FlightDao) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "search") {
        composable("search") {
            FlightSearchScreen(navController)
        }
        composable("saved") {
            SavedFlightsScreen(navController, flightDao)
        }
        composable("map/{departureAirport}/{arrivalAirport}") { backStackEntry ->
            val departureAirport =
                backStackEntry.arguments?.getString("departureAirport")
            val arrivalAirport = backStackEntry.arguments?.getString("arrivalAirport")

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
