package es.uma.vuelink.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [FlightEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun flightDao(): FlightDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migración de la versión 1 a la versión 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Aquí puedes agregar las modificaciones que hayas hecho al esquema de la base de datos
                // Ejemplo: agregar columnas a la tabla "flights"
                database.execSQL("ALTER TABLE flights ADD COLUMN iataDeparture TEXT")
                database.execSQL("ALTER TABLE flights ADD COLUMN iataArrival TEXT")
            }
        }

        // Función para obtener la instancia de la base de datos
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "flight_database"
                )
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
            }
        }
    }
}
