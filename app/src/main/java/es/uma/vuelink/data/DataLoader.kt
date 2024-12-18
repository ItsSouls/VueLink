package es.uma.vuelink.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import es.uma.vuelink.R
import java.io.InputStreamReader

data class AirportCoordinates(val iata: String, val latitude: Double, val longitude: Double)

fun loadAirportCoordinates(context: Context): List<AirportCoordinates> {
    val inputStream = context.resources.openRawResource(R.raw.airports)
    val reader = InputStreamReader(inputStream)

    val type = object : TypeToken<List<AirportCoordinates>>() {}.type
    return Gson().fromJson(reader, type)
}
