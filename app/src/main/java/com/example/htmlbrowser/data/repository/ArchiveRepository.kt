package com.example.htmlbrowser.data.repository

import com.example.htmlbrowser.data.database.ArchiveDao
import com.example.htmlbrowser.data.database.toDomain
import com.example.htmlbrowser.data.database.toEntity
import com.example.htmlbrowser.domain.manager.StorageManager
import com.example.htmlbrowser.domain.model.HtmlArchive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ArchiveRepository(
    private val archiveDao: ArchiveDao,
    private val storageManager: StorageManager
) {

    fun getAllArchives(): Flow<List<HtmlArchive>> {
        return archiveDao.getAllArchives().map { list -> list.map { it.toDomain() } }
    }

    suspend fun insertArchive(archive: HtmlArchive) {
        archiveDao.insertArchive(archive.toEntity())
    }

    suspend fun deleteArchive(archive: HtmlArchive): Boolean {
        val deleted = storageManager.deleteArchive(archive.id)
        archiveDao.deleteArchiveById(archive.id)
        return deleted
    }

    suspend fun getArchiveById(archiveId: String): HtmlArchive? {
        return archiveDao.getArchiveById(archiveId)?.toDomain()
    }

    suspend fun updateArchiveSize(archiveId: String) {
        val newSize = storageManager.calculateArchiveSize(archiveId)
        val entity = archiveDao.getArchiveById(archiveId) ?: return
        archiveDao.insertArchive(entity.copy(sizeBytes = newSize))
    }
}
