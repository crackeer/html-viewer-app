package com.example.htmlbrowser.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArchiveDao {
    @Query("SELECT * FROM archives ORDER BY importDate DESC")
    fun getAllArchives(): Flow<List<ArchiveEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArchive(archive: ArchiveEntity)

    @Delete
    suspend fun deleteArchive(archive: ArchiveEntity)

    @Query("DELETE FROM archives WHERE id = :archiveId")
    suspend fun deleteArchiveById(archiveId: String)

    @Query("SELECT * FROM archives WHERE id = :archiveId LIMIT 1")
    suspend fun getArchiveById(archiveId: String): ArchiveEntity?
}
