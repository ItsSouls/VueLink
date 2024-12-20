package es.uma.vuelink.model

import com.google.gson.annotations.SerializedName

data class FlightResponse(
    val data: List<Flight>
)

data class Flight(
    @SerializedName("flight_date") val flightDate: String,
    @SerializedName("flight_status") val flightStatus: String,
    val departure: Airport,
    val arrival: Airport,
    val airline: Airline,
    val flight: FlightInfo,
    val aircraft: Any?,
    val live: Any?
)

data class Airline(
    val name: String?, val iata: String?, val icao: String?
)

data class FlightInfo(
    val number: String?, val iata: String?, val icao: String?, val codeshared: Any?
)
