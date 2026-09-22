package br.edu.utfpr.patotour.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.edu.utfpr.patotour.data.local.PontoTuristicoDao
import br.edu.utfpr.patotour.data.model.PontoTuristico

@Database(entities = [PontoTuristico::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pontoTuristicoDao(): PontoTuristicoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pontos_turisticos_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}