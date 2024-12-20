package es.uma.vuelink.model

import com.google.gson.annotations.SerializedName

data class Airport(
    @SerializedName("airport") val name: String,
    val timezone: String,
    val iata: String,
    val icao: String,
    val terminal: String?,
    val gate: String?,
    val delay: String?,
    val scheduled: String,
    val estimated: String,
    val actual: String?,
    @SerializedName("estimated_runway") val estimatedRunway: String?,
    @SerializedName("actual_runway") val actualRunway: String?
)