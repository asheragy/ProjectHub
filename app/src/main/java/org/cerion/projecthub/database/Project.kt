package org.cerion.projecthub.database

import androidx.lifecycle.LiveData
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

    @Query("SELECT * FROM projects")
    fun getAllAsync(): LiveData<List<DbProject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: DbProject)

    @Delete
    suspend fun delete(project: DbProject)
}