package es.uma.vuelink.data

import androidx.room.Embedded
import androidx.room.Relation

data class FlightWithAirports(
    @Embedded val flight: FlightEntity, @Relation(
        parentColumn = "departureAirportId", entityColumn = "id"
    ) val departureAirport: AirportEntity, @Relation(
        parentColumn = "arrivalAirportId", entityColumn = "id"
    ) val arrivalAirport: AirportEntity
)
