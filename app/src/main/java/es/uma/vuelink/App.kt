package es.uma.vuelink

import android.app.Application
import es.uma.vuelink.data.AppDatabase

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppDatabase.getInstance(this)
    }
}