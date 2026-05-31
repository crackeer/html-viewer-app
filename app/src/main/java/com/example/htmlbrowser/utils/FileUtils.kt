package com.example.htmlbrowser.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File

object FileUtils {

    /**
     * Resolves the display name of a file from a content URI.
     * Falls back to the last path segment if the content resolver query fails.
     *
     * @param context Application context used to access the ContentResolver.
     * @param uri     The URI of the file whose name should be retrieved.
     * @return The file's display name, or an empty string if it cannot be determined.
     */
    fun getFileNameFromUri(context: Context, uri: Uri): String {
        // Try content resolver first (works for content:// URIs)
        if (uri.scheme == "content") {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        val name = cursor.getString(nameIndex)
                        if (!name.isNullOrEmpty()) return name
                    }
                }
            }
        }

        // Fall back to the last path segment for file:// URIs or when query fails
        return uri.lastPathSegment ?: ""
    }

    /**
     * Creates a temporary file in the app's cache directory.
     *
     * @param context Application context used to locate the cache directory.
     * @param prefix  Prefix for the temporary file name.
     * @param suffix  Suffix (extension) for the temporary file name, e.g. ".zip".
     * @return The created [File] object.
     */
    fun createTempFile(context: Context, prefix: String, suffix: String): File {
        val cacheDir = context.cacheDir
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return File.createTempFile(prefix, suffix, cacheDir)
    }

    /**
     * Recursively deletes a directory and all of its contents.
     *
     * @param directory The directory to delete.
     * @return `true` if the directory (and all contents) were deleted successfully,
     *         `false` if the directory does not exist or deletion fails for any entry.
     */
    fun deleteDirectory(directory: File): Boolean {
        if (!directory.exists()) return false

        if (directory.isDirectory) {
            val children = directory.listFiles()
            if (children != null) {
                for (child in children) {
                    val childDeleted = deleteDirectory(child)
                    if (!childDeleted) return false
                }
            }
        }

        return directory.delete()
    }

    /**
     * Calculates the total size of all files within a directory, recursively.
     * Returns 0 if the directory does not exist or is empty.
     *
     * @param directory The root directory to measure.
     * @return Total size in bytes of all files under [directory].
     */
    fun calculateDirectorySize(directory: File): Long {
        if (!directory.exists()) return 0L

        return directory.walk()
            .filter { it.isFile }
            .map { it.length() }
            .sum()
    }
}
