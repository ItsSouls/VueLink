package es.uma.vuelink.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AirportDao {
    @Query("SELECT * FROM airports WHERE id = :id LIMIT 1")
    suspend fun getAirportById(id: Int): AirportEntity?

    @Query("SELECT * FROM airports WHERE iata = :iata LIMIT 1")
    suspend fun getAirportByIATA(iata: String): AirportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAirport(airport: AirportEntity): Long

    @Delete
    suspend fun deleteAirport(airport: AirportEntity): Int
}