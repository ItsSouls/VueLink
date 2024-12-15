package com.example.vuelink

import android.app.Application
import com.example.vuelink.data.AppDatabase

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppDatabase.getInstance(this)
    }
}