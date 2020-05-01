package org.cerion.projecthub.database

import android.content.Context
import androidx.room.*


@Entity(tableName = "projects")
data class DbProject(
    @PrimaryKey val id: Int,
    val nodeId: String,
    val type: Int,
    val owner: String,
    val repo: String,
    val name: String,
    val description: String
)

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects")
    fun getAll(): List<DbProject>
}

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
                        .allowMainThreadQueries() // TODO try removing later
                        .build()

                    instance = db
                }

                return db
            }
        }
    }

}