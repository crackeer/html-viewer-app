package com.example.htmlbrowser.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.htmlbrowser.domain.model.HtmlArchive
import java.util.Date

@Entity(tableName = "archives")
data class ArchiveEntity(
    @PrimaryKey val id: String,
    val title: String,
    val originalFileName: String,
    val extractPath: String,
    val sizeBytes: Long,
    val importDate: Long,   // stored as epoch millis
    val lastAccessed: Long  // stored as epoch millis
)

fun ArchiveEntity.toDomain(): HtmlArchive = HtmlArchive(
    id = id,
    title = title,
    originalFileName = originalFileName,
    extractPath = extractPath,
    sizeBytes = sizeBytes,
    importDate = Date(importDate),
    lastAccessed = Date(lastAccessed)
)

fun HtmlArchive.toEntity(): ArchiveEntity = ArchiveEntity(
    id = id,
    title = title,
    originalFileName = originalFileName,
    extractPath = extractPath,
    sizeBytes = sizeBytes,
    importDate = importDate.time,
    lastAccessed = lastAccessed.time
)
