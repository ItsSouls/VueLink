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
}
