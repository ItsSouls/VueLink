package es.uma.vuelink.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import es.uma.vuelink.R
import java.io.InputStreamReader

// Definimos la clase que representa un aeropuerto con sus coordenadas
data class AirportCoordinates(val iata: String, val latitude: Double, val longitude: Double)

// Función que carga las coordenadas de los aeropuertos desde un archivo JSON en raw/
fun loadAirportCoordinates(context: Context): List<AirportCoordinates> {
    // Abrir el archivo JSON desde los recursos
    val inputStream = context.resources.openRawResource(R.raw.airports)
    val reader = InputStreamReader(inputStream)

    // Usamos Gson para convertir el JSON en una lista de objetos AirportCoordinates
    val type = object : TypeToken<List<AirportCoordinates>>() {}.type
    return Gson().fromJson(reader, type)
}
