package es.uma.vuelink.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface FlightDao {
    @Transaction
    @Query("SELECT * FROM flights")
    suspend fun getAllFlightsWithAirports(): List<FlightWithAirports>

    @Transaction
    @Query("SELECT * FROM flights WHERE id = :flightId")
    suspend fun getFlightWithAirports(flightId: Int): FlightWithAirports

    @Insert
    suspend fun insertFlight(flight: FlightEntity): Long

    @Delete
    suspend fun deleteFlight(flight: FlightEntity): Int

    @Transaction
    @Query(
        """
    SELECT * 
    FROM flights f
    JOIN airports dep_airport ON dep_airport.id = f.departureAirportId
    JOIN airports arr_airport ON arr_airport.id = f.arrivalAirportId
    WHERE dep_airport.iata = :departureIata 
      AND arr_airport.iata = :arrivalIata
    LIMIT 1
    """
    )
    suspend fun getFlightWithAirportsByIata(
        departureIata: String, arrivalIata: String
    ): FlightWithAirports?

}