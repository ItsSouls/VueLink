package es.uma.vuelink.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "airports")
data class AirportEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "timezone") val timezone: String,
    @ColumnInfo(name = "iata") val iata: String,
    @ColumnInfo(name = "icao") val icao: String,
    @ColumnInfo(name = "terminal") val terminal: String?,
    @ColumnInfo(name = "gate") val gate: String?,
    @ColumnInfo(name = "delay") val delay: String?,
    @ColumnInfo(name = "scheduled") val scheduled: String,
    @ColumnInfo(name = "estimated") val estimated: String,
    @ColumnInfo(name = "actual") val actual: String?,
    @ColumnInfo(name = "estimatedRunway") val estimatedRunway: String?,
    @ColumnInfo(name = "actualRunway") val actualRunway: String?
)