package com.example.htmlbrowser.domain.manager

import android.content.Context
import com.example.htmlbrowser.utils.AppConfig
import com.example.htmlbrowser.utils.FileSizeUtils
import com.example.htmlbrowser.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class StorageManager(private val context: Context) {

    /**
     * Returns the total size in bytes of the extracted folder for the given archive.
     */
    fun calculateArchiveSize(archiveId: String): Long {
        val archiveDir = File(context.filesDir, "archives/$archiveId")
        return FileUtils.calculateDirectorySize(archiveDir)
    }

    /**
     * Delegates directory size calculation to FileUtils.
     */
    fun calculateDirectorySize(directory: File): Long {
        return FileUtils.calculateDirectorySize(directory)
    }

    /**
     * Deletes the extracted folder for the given archive.
     * Returns true on success, false if the directory doesn't exist or deletion fails.
     */
    suspend fun deleteArchive(archiveId: String): Boolean {
        return withContext(Dispatchers.IO) {
            val archiveDir = File(context.filesDir, "archives/$archiveId")
            FileUtils.deleteDirectory(archiveDir)
        }
    }

    /**
     * Returns the File representing the archive's extracted directory.
     */
    fun getArchiveDirectory(archiveId: String): File {
        return File(context.filesDir, "${AppConfig.ARCHIVE_DIR_NAME}/$archiveId")
    }

    /**
     * Returns the number of free bytes available in the app's files directory.
     */
    fun getAvailableStorageSpace(): Long {
        return context.filesDir.freeSpace
    }

    /**
     * Returns true if there is at least [requiredBytes] of free storage space available.
     */
    fun hasEnoughSpace(requiredBytes: Long): Boolean {
        return getAvailableStorageSpace() >= requiredBytes
    }

    /**
     * Returns the formatted size string (e.g. "1.50 MB") for the given archive.
     */
    fun formatArchiveSize(archiveId: String): String {
        return FileSizeUtils.formatBytes(calculateArchiveSize(archiveId))
    }

    /**
     * Returns the total bytes used by all archives in the archives directory.
     */
    fun calculateTotalStorageUsed(): Long {
        val archivesDir = File(context.filesDir, AppConfig.ARCHIVE_DIR_NAME)
        if (!archivesDir.exists() || !archivesDir.isDirectory) return 0L
        return archivesDir.listFiles()
            ?.filter { it.isDirectory }
            ?.sumOf { FileUtils.calculateDirectorySize(it) }
            ?: 0L
    }
}
