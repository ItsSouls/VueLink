package es.uma.vuelink.data

import androidx.room.*

@Dao
interface FlightDao {
    @Query("SELECT * FROM flights")
    suspend fun getAllFlights(): List<FlightEntity>

    @Insert
    suspend fun insertFlight(flight: FlightEntity): Long

    @Delete
    suspend fun deleteFlight(flight: FlightEntity): Int

    @Query("SELECT * FROM flights WHERE departureIATA = :departureIATA AND arrivalIATA = :arrivalIATA LIMIT 1")
    suspend fun getFlightDetails(departureIATA: String, arrivalIATA: String): FlightEntity?
}
