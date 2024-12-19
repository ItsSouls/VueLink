package es.uma.vuelink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import es.uma.vuelink.ui.screens.AppNavigation

import es.uma.vuelink.data.AppDatabase

import es.uma.vuelink.ui.theme.VueLinkTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val flightDao = AppDatabase.getInstance(this).flightDao()

        setContent {
            VueLinkTheme {
                AppNavigation(flightDao)
            }
        }
    }
}







