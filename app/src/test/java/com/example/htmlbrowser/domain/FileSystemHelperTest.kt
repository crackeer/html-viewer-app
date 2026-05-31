package com.example.htmlbrowser.domain

import io.kotest.property.Arb
import io.kotest.property.arbitrary.byteArray
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.forAll
import kotlinx.coroutines.test.runTest
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FileSystemHelperTest {

    @get:Rule
    val tempDir = TemporaryFolder()

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private fun createZipWithEntries(vararg entries: Pair<String, ByteArray>): File {
        val zipFile = tempDir.newFile("test_${System.nanoTime()}.zip")
        ZipOutputStream(zipFile.outputStream()).use { zos ->
            for ((name, content) in entries) {
                zos.putNextEntry(ZipEntry(name))
                zos.write(content)
                zos.closeEntry()
            }
        }
        return zipFile
    }

    private fun extractZipToDir(zipFile: File): File {
        val destDir = tempDir.newFolder("extracted_${System.nanoTime()}")
        ZipFile(zipFile).extractAll(destDir.absolutePath)
        return destDir
    }

    private fun listAllFiles(dir: File): List<File> =
        dir.walk().filter { it.isFile }.toList()

    // ─── ZIP extraction completeness (PBT 5.1) ──────────────────────────────────

    @Test
    fun extractZip_allEntriesPresent() {
        val entries = listOf(
            "index.html" to "<html></html>".toByteArray(),
            "style.css" to "body{}".toByteArray(),
            "script.js" to "console.log('hi')".toByteArray()
        )
        val zip = createZipWithEntries(*entries.toTypedArray())
        val dest = extractZipToDir(zip)
        val extractedNames = listAllFiles(dest).map { it.name }.sorted()
        val expectedNames = entries.map { it.first }.sorted()
        assertEquals(expectedNames, extractedNames)
    }

    @Test
    fun extractZip_emptyZip_noFilesExtracted() {
        val zip = createZipWithEntries()
        val dest = extractZipToDir(zip)
        assertEquals(0, listAllFiles(dest).size)
    }

    // ─── File content preservation (PBT 5.2) ────────────────────────────────────

    @Test
    fun extractZip_fileContentsPreserved() {
        val content = "Hello, World! This is test content.".toByteArray()
        val zip = createZipWithEntries("index.html" to content)
        val dest = extractZipToDir(zip)
        val extractedFile = File(dest, "index.html")
        assertTrue(extractedFile.exists())
        assertTrue(extractedFile.readBytes().contentEquals(content))
    }

    @Test
    fun extractZip_multipleFiles_contentsPreserved() {
        val files = mapOf(
            "index.html" to "<html><title>Test</title></html>".toByteArray(),
            "style.css" to "body { color: red; }".toByteArray(),
            "app.js" to "var x = 1;".toByteArray()
        )
        val zip = createZipWithEntries(*files.entries.map { it.key to it.value }.toTypedArray())
        val dest = extractZipToDir(zip)
        for ((name, expectedContent) in files) {
            val file = File(dest, name)
            assertTrue("$name should exist", file.exists())
            assertTrue("$name content should match", file.readBytes().contentEquals(expectedContent))
        }
    }

    // ─── Property-Based Tests ────────────────────────────────────────────────────

    @Test
    fun pbt_5_1_extractionCompleteness() = runTest {
        forAll(Arb.list(Arb.int(1..100), 1..5)) { sizes ->
            val entries = sizes.mapIndexed { i, size ->
                "file_$i.bin" to ByteArray(size) { it.toByte() }
            }
            val zip = createZipWithEntries(*entries.toTypedArray())
            val dest = extractZipToDir(zip)
            val extractedNames = listAllFiles(dest).map { it.name }.sorted()
            val expectedNames = entries.map { it.first }.sorted()
            extractedNames == expectedNames
        }
    }

    @Test
    fun pbt_5_2_filePreservationDuringExtraction() = runTest {
        forAll(Arb.list(Arb.byteArray(Arb.int(0..256)), 1..3)) { contentList ->
            val entries = contentList.mapIndexed { i, bytes ->
                "file_$i.bin" to bytes
            }
            val zip = createZipWithEntries(*entries.toTypedArray())
            val dest = extractZipToDir(zip)
            entries.all { (name, expectedBytes) ->
                val file = File(dest, name)
                file.exists() && file.readBytes().contentEquals(expectedBytes)
            }
        }
    }
}
