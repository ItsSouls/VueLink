package com.example.vuelink.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FlightEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun flightDao(): FlightDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "flight_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
