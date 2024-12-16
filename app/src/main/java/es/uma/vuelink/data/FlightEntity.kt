package es.uma.vuelink.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flights")
data class FlightEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "flightDate") val flightDate: String?,
    @ColumnInfo(name = "flightStatus") val flightStatus: String,
    @ColumnInfo(name = "departureAirport") val departureAirport: String,
    @ColumnInfo(name = "arrivalAirport") val arrivalAirport: String,
    @ColumnInfo(name = "departureIATA") val departureIATA: String,  // Añadir código IATA
    @ColumnInfo(name = "arrivalIATA") val arrivalIATA: String,      // Añadir código IATA
    @ColumnInfo(name = "airlineName") val airlineName: String?,
    @ColumnInfo(name = "flightNumber") val flightNumber: String?
)

