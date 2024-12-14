package com.example.vuelink.data

import androidx.room.*

@Dao
interface FlightDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlight(flight: FlightEntity)

    @Delete
    suspend fun deleteFlight(flight: FlightEntity)

    @Query("SELECT * FROM selected_flights")
    suspend fun getAllFlights(): List<FlightEntity>
}
