package com.example.vuelink.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "selected_flights")
data class FlightEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val flightDate: String,
    val flightStatus: String,
    val departureAirport: String,
    val arrivalAirport: String,
    val airlineName: String?,
    val flightNumber: String?
)
