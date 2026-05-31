package com.example.htmlbrowser.domain.helper

import android.content.Context
import com.example.htmlbrowser.utils.AppConfig
import com.example.htmlbrowser.utils.FileUtils
import java.io.File
import java.io.IOException

/**
 * Abstracts file system operations for the HTML Browser application.
 * Provides safe file copying, moving, deletion, path sanitization,
 * and directory management utilities.
 */
class FileSystemHelper(private val context: Context) {

    /**
     * Copies a file from [source] to [destination].
     * Creates parent directories of [destination] if they do not exist.
     *
     * @param source      The file to copy.
     * @param destination The target file path.
     * @return `true` if the copy succeeded, `false` on [IOException].
     */
    fun copyFile(source: File, destination: File): Boolean {
        return try {
            destination.parentFile?.mkdirs()
            source.inputStream().use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: IOException) {
            false
        }
    }

    /**
     * Moves a file from [source] to [destination] by copying then deleting the source.
     *
     * @param source      The file to move.
     * @param destination The target file path.
     * @return `true` if the move succeeded, `false` if the copy step fails.
     */
    fun moveFile(source: File, destination: File): Boolean {
        if (!copyFile(source, destination)) return false
        source.delete()
        return true
    }

    /**
     * Deletes a single file, or recursively deletes a directory and all its contents.
     *
     * @param file The file or directory to delete.
     * @return `true` if deletion succeeded, `false` otherwise.
     */
    fun deleteFile(file: File): Boolean {
        return if (file.isDirectory) {
            FileUtils.deleteDirectory(file)
        } else {
            file.delete()
        }
    }

    /**
     * Sanitizes a file path by removing path traversal sequences and leading slashes.
     *
     * Removes:
     * - `../` sequences
     * - `./` sequences
     * - Leading `/` characters
     * - Leading and trailing whitespace
     *
     * @param path The raw path string to sanitize.
     * @return The sanitized path string.
     */
    fun sanitizePath(path: String): String {
        return path
            .trim()
            .replace("../", "")
            .replace("./", "")
            .trimStart('/')
    }

    /**
     * Checks whether [targetPath] is safely contained within [basePath],
     * preventing directory traversal attacks.
     *
     * @param basePath   The allowed root directory.
     * @param targetPath The path to validate.
     * @return `true` only if [targetPath]'s canonical path starts with [basePath]'s canonical path.
     */
    fun isPathSafe(basePath: File, targetPath: File): Boolean {
        return targetPath.canonicalPath.startsWith(basePath.canonicalPath)
    }

    /**
     * Creates and returns the archive directory for the given [archiveId].
     * The directory is located at `<filesDir>/<ARCHIVE_DIR_NAME>/<archiveId>`.
     * Calls [File.mkdirs] to ensure the directory exists.
     *
     * @param archiveId The unique identifier for the archive.
     * @return The [File] representing the archive directory.
     */
    fun createArchiveDirectory(archiveId: String): File {
        val archiveDir = File(context.filesDir, "${AppConfig.ARCHIVE_DIR_NAME}/$archiveId")
        archiveDir.mkdirs()
        return archiveDir
    }

    /**
     * Deletes all temporary files in the app's cache directory whose names start
     * with the prefix `"archive_import_"`.
     */
    fun cleanupTempFiles() {
        context.cacheDir.listFiles { file ->
            file.name.startsWith("archive_import_")
        }?.forEach { it.delete() }
    }
}
