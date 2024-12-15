package es.uma.vuelink.model

import com.google.gson.annotations.SerializedName

data class FlightResponse(
    val data: List<Flight>
)

data class Flight(
    @SerializedName("flight_date") val flightDate: String,
    @SerializedName("flight_status") val flightStatus: String,
    val departure: AirportDetails,
    val arrival: AirportDetails,
    val airline: Airline,
    val flight: FlightInfo,
    val aircraft: Any?,
    val live: Any?
)

data class AirportDetails(
    val airport: String,
    val timezone: String,
    val iata: String,
    val icao: String,
    val terminal: String?,
    val gate: String?,
    val delay: String?,
    val scheduled: String,
    val estimated: String,
    val actual: String?,
    @SerializedName("estimated_runway") val estimatedRunway: String?,
    @SerializedName("actual_runway") val actualRunway: String?
)

data class Airline(
    val name: String?, val iata: String?, val icao: String?
)

data class FlightInfo(
    val number: String?, val iata: String?, val icao: String?, val codeshared: Any?
)
