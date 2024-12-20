package es.uma.vuelink.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flights", foreignKeys = [ForeignKey(
        entity = AirportEntity::class,
        parentColumns = ["id"],
        childColumns = ["departureAirportId"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = AirportEntity::class,
        parentColumns = ["id"],
        childColumns = ["arrivalAirportId"],
        onDelete = ForeignKey.CASCADE
    )], indices = [Index("departureAirportId"), Index("arrivalAirportId")]
)
data class FlightEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "flightDate") val flightDate: String?,
    @ColumnInfo(name = "flightStatus") val flightStatus: String,
    @ColumnInfo(name = "departureAirportId") val departureAirportId: Int,
    @ColumnInfo(name = "arrivalAirportId") val arrivalAirportId: Int,
    @ColumnInfo(name = "airlineName") val airlineName: String?,
    @ColumnInfo(name = "flightNumber") val flightNumber: String?
)

