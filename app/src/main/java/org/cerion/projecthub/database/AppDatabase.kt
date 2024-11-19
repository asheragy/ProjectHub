package org.cerion.projecthub.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase




@androidx.room.Database(entities = [DbProject::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao

    companion object {

        private const val DATABASE_NAME = "app.db"
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var db = instance
                if (db == null) {
                    db = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DATABASE_NAME)
                        // Allowing since database is very minimal
                        .allowMainThreadQueries()
                        .build()

                    instance = db
                }

                return db
            }
        }
    }

}