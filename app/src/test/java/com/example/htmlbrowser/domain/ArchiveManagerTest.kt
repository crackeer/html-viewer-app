package com.example.htmlbrowser.domain

import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.forAll
import kotlinx.coroutines.test.runTest
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ArchiveManagerTest {

    @get:Rule
    val tempDir = TemporaryFolder()

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private fun createZipWithEntries(vararg entries: Pair<String, String>): File {
        val zipFile = tempDir.newFile("test_${System.nanoTime()}.zip")
        ZipOutputStream(zipFile.outputStream()).use { zos ->
            for ((name, content) in entries) {
                zos.putNextEntry(ZipEntry(name))
                zos.write(content.toByteArray(Charsets.UTF_8))
                zos.closeEntry()
            }
        }
        return zipFile
    }

    private fun createZip4jWithEntries(vararg entries: Pair<String, String>): File {
        val zipPath = tempDir.newFile("test4j_${System.nanoTime()}.zip")
        zipPath.delete() // zip4j creates the file itself
        val zip = ZipFile(zipPath)
        for ((name, content) in entries) {
            val tmpEntry = tempDir.newFile("entry_${System.nanoTime()}.tmp")
            tmpEntry.writeText(content, Charsets.UTF_8)
            val params = ZipParameters().apply { fileNameInZip = name }
            zip.addFile(tmpEntry, params)
        }
        return zipPath
    }

    // ─── validateZipContainsIndexHtml ───────────────────────────────────────────

    @Test
    fun validZipWithIndexHtml_returnsTrue() = runTest {
        val zip = createZip4jWithEntries("index.html" to "<html><title>Test</title></html>")
        val helper = ZipValidationHelper()
        assertTrue(helper.validateZipContainsIndexHtml(zip))
    }

    @Test
    fun zipWithoutIndexHtml_returnsFalse() = runTest {
        val zip = createZip4jWithEntries("other.html" to "<html></html>")
        val helper = ZipValidationHelper()
        assertFalse(helper.validateZipContainsIndexHtml(zip))
    }

    @Test
    fun emptyZip_returnsFalse() = runTest {
        val zip = createZipWithEntries() // empty ZIP
        val helper = ZipValidationHelper()
        assertFalse(helper.validateZipContainsIndexHtml(zip))
    }

    // ─── extractTitleFromZip ────────────────────────────────────────────────────

    @Test
    fun zipWithTitleTag_extractsTitle() = runTest {
        val zip = createZip4jWithEntries("index.html" to "<html><head><title>My Page</title></head></html>")
        val helper = ZipValidationHelper()
        assertEquals("My Page", helper.extractTitleFromZip(zip))
    }

    @Test
    fun zipWithNoTitleTag_returnsDefault() = runTest {
        val zip = createZip4jWithEntries("index.html" to "<html><body>No title here</body></html>")
        val helper = ZipValidationHelper()
        assertEquals("Untitled Archive", helper.extractTitleFromZip(zip))
    }

    @Test
    fun zipWithEmptyTitle_returnsDefault() = runTest {
        val zip = createZip4jWithEntries("index.html" to "<html><head><title></title></head></html>")
        val helper = ZipValidationHelper()
        assertEquals("Untitled Archive", helper.extractTitleFromZip(zip))
    }

    // ─── Property-Based Tests ───────────────────────────────────────────────────

    @Test
    fun pbt_1_1_anyNonEmptyTitle_isExtractedCorrectly() = runTest {
        forAll(Arb.string(1..50)) { title ->
            // Skip titles that contain HTML special chars that would break the regex
            if (title.contains('<') || title.contains('>')) return@forAll true
            val zip = createZip4jWithEntries(
                "index.html" to "<html><head><title>$title</title></head></html>"
            )
            val helper = ZipValidationHelper()
            val extracted = helper.extractTitleFromZip(zip)
            extracted == title.trim()
        }
    }

    @Test
    fun pbt_1_2_nonIndexFilename_failsValidation() = runTest {
        forAll(Arb.string(1..20)) { filename ->
            // Ensure the filename is not "index.html" (case-insensitive)
            if (filename.equals("index.html", ignoreCase = true)) return@forAll true
            val safeName = filename.replace("/", "_").replace("\\", "_").ifBlank { "file.txt" }
            val zip = createZip4jWithEntries("$safeName.html" to "<html></html>")
            val helper = ZipValidationHelper()
            !helper.validateZipContainsIndexHtml(zip)
        }
    }

    @Test
    fun pbt_1_3_extractTitle_neverThrows_alwaysReturnsNonNull() = runTest {
        forAll(Arb.string(0..100)) { htmlContent ->
            val helper = ZipValidationHelper()
            val result = runCatching {
                val zip = createZip4jWithEntries("index.html" to htmlContent)
                helper.extractTitleFromZip(zip)
            }.getOrNull()
            result != null
        }
    }
}

/**
 * Extracted pure logic from ArchiveManager for testability without Android Context.
 */
class ZipValidationHelper {

    suspend fun validateZipContainsIndexHtml(zipFile: File): Boolean {
        return try {
            ZipFile(zipFile).use { zip ->
                zip.fileHeaders.any { header ->
                    header.fileName.equals("index.html", ignoreCase = true)
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun extractTitleFromZip(zipFile: File): String {
        return try {
            ZipFile(zipFile).use { zip ->
                val indexHeader = zip.fileHeaders.firstOrNull { header ->
                    header.fileName.equals("index.html", ignoreCase = true)
                } ?: return "Untitled Archive"

                val htmlContent = zip.getInputStream(indexHeader).use { stream ->
                    stream.bufferedReader(Charsets.UTF_8).readText()
                }

                val titleRegex = Regex("<title[^>]*>(.*?)</title>", RegexOption.IGNORE_CASE)
                val title = titleRegex.find(htmlContent)?.groupValues?.getOrNull(1)?.trim()
                if (title.isNullOrEmpty()) "Untitled Archive" else title
            }
        } catch (e: Exception) {
            "Untitled Archive"
        }
    }
}
