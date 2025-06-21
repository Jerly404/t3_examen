package com.hcondor.t3_examen.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PlaceEntity::class, PlaceHistoryEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao
    abstract fun placeHistoryDao(): PlaceHistoryDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "places_db"
                )
                    .fallbackToDestructiveMigration()  // 🔴 esto borra la BD y la recrea si hay cambio de versión
                    .build().also { instance = it }
            }
    }
}
