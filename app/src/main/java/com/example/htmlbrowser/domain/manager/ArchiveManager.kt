package com.example.htmlbrowser.domain.manager

import android.content.Context
import android.net.Uri
import com.example.htmlbrowser.domain.model.ArchiveImportResult
import com.example.htmlbrowser.domain.model.HtmlArchive
import com.example.htmlbrowser.utils.AppConfig
import com.example.htmlbrowser.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import java.io.File
import java.util.Date
import java.util.UUID

class ArchiveManager(private val context: Context) {

    /**
     * Imports a ZIP archive from the given URI.
     *
     * Steps:
     * 1. Copy URI content to a temp file
     * 2. Validate the ZIP contains index.html
     * 3. Extract title from index.html inside the ZIP
     * 4. Generate a UUID archive ID and create extraction directory
     * 5. Extract all ZIP entries to the directory
     * 6. Calculate extracted size and return the result
     */
    suspend fun importArchive(zipUri: Uri): ArchiveImportResult = withContext(Dispatchers.IO) {
        try {
            // 1. Copy URI content to a temp file
            val tempZipFile = copyUriToTempFile(zipUri)

            try {
                // 3. Validate ZIP contains index.html
                if (!validateZipContainsIndexHtml(tempZipFile)) {
                    return@withContext ArchiveImportResult(
                        success = false,
                        archive = null,
                        errorMessage = "Selected ZIP file must contain an index.html file"
                    )
                }

                // 4. Extract title from index.html inside the ZIP
                val title = extractTitleFromZip(tempZipFile)

                // 5. Generate UUID archive ID and create extraction directory
                val archiveId = UUID.randomUUID().toString()
                val extractDir = File(context.filesDir, "${AppConfig.ARCHIVE_DIR_NAME}/$archiveId")
                extractDir.mkdirs()

                // 6. Extract all ZIP entries to the directory
                extractZipFile(tempZipFile, extractDir)

                // 7. Calculate size
                val sizeBytes = FileUtils.calculateDirectorySize(extractDir)

                // 8. Build and return the result
                val archive = HtmlArchive(
                    id = archiveId,
                    title = title,
                    originalFileName = FileUtils.getFileNameFromUri(context, zipUri),
                    extractPath = extractDir.absolutePath,
                    sizeBytes = sizeBytes,
                    importDate = Date(),
                    lastAccessed = Date()
                )

                ArchiveImportResult(success = true, archive = archive, errorMessage = null)
            } finally {
                // Clean up temp file
                tempZipFile.delete()
            }
        } catch (e: Exception) {
            ArchiveImportResult(success = false, archive = null, errorMessage = e.message)
        }
    }

    /**
     * Validates that the given ZIP file contains an entry named "index.html"
     * (case-insensitive).
     */
    suspend fun validateZipContainsIndexHtml(zipFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            ZipFile(zipFile).use { zip ->
                zip.fileHeaders.any { header ->
                    header.fileName.equals("index.html", ignoreCase = true)
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Opens the ZIP file, reads the index.html entry, and extracts the content
     * of the first <title>...</title> element using a regex.
     * Returns "Untitled Archive" if no title is found or on any error.
     */
    suspend fun extractTitleFromZip(zipFile: File): String = withContext(Dispatchers.IO) {
        try {
            ZipFile(zipFile).use { zip ->
                val indexHeader = zip.fileHeaders.firstOrNull { header ->
                    header.fileName.equals("index.html", ignoreCase = true)
                } ?: return@withContext "Untitled Archive"

                val htmlContent = zip.getInputStream(indexHeader).use { stream ->
                    stream.bufferedReader(Charsets.UTF_8).readText()
                }

                val titleRegex = Regex("<title[^>]*>(.*?)</title>", RegexOption.IGNORE_CASE)
                val matchResult = titleRegex.find(htmlContent)
                val title = matchResult?.groupValues?.getOrNull(1)?.trim()

                if (title.isNullOrEmpty()) "Untitled Archive" else title
            }
        } catch (e: Exception) {
            "Untitled Archive"
        }
    }

    /**
     * Extracts all entries from the given ZIP file into the destination directory
     * using zip4j.
     */
    suspend fun extractZipFile(zipFile: File, destDir: File): Unit = withContext(Dispatchers.IO) {
        ZipFile(zipFile).use { zip ->
            zip.extractAll(destDir.absolutePath)
        }
    }

    /**
     * Returns the extraction directory for the given archive ID.
     */
    fun getArchivePath(archiveId: String): File {
        return File(context.filesDir, "${AppConfig.ARCHIVE_DIR_NAME}/$archiveId")
    }

    /**
     * Copies the content of the given URI to a temporary .zip file in the app's
     * cache directory and returns that file.
     */
    private fun copyUriToTempFile(uri: Uri): File {
        val tempFile = FileUtils.createTempFile(context, "archive_import_", ".zip")
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw IllegalStateException("Cannot open input stream for URI: $uri")
        return tempFile
    }

    /**
     * Returns the file size in bytes for the given URI, or 0 if it cannot be determined.
     */
    private fun getUriFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                pfd.statSize
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}
