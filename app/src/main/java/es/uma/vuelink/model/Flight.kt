data class FlightResponse(
    val data: List<Flight>
)

data class Flight(
    val flight_date: String,
    val flight_status: String,
    val departure: AirportDetails,
    val arrival: AirportDetails,
    val airline: Airline,
    val flight: FlightInfo,
    val aircraft: Any?, // Puede ser cualquier cosa, según la respuesta de la API
    val live: Any? // Puede ser cualquier cosa, según la respuesta de la API
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
    val estimated_runway: String?,
    val actual_runway: String?
)

data class Airline(
    val name: String,
    val iata: String?,
    val icao: String?
)

data class FlightInfo(
    val number: String?,
    val iata: String?,
    val icao: String?,
    val codeshared: Any? // Puede ser un objeto o null
)
