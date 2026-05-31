package com.example.htmlbrowser.property

import com.example.htmlbrowser.domain.ArchiveManagerTest.ZipValidationHelper
import com.example.htmlbrowser.property.TestGenerators.htmlTitleArb
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.forAll
import kotlinx.coroutines.test.runTest
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Property-based tests for archive import logic.
 * Covers PBT 1.1, 1.2, 1.3 from the design document.
 */
class ArchiveImportPropertyTest {

    @get:Rule
    val tempDir = TemporaryFolder()

    private val helper = ZipValidationHelper()

    private fun createZip(vararg entries: Pair<String, String>): File {
        val zipPath = tempDir.newFile("pbt_${System.nanoTime()}.zip")
        zipPath.delete()
        val zip = ZipFile(zipPath)
        for ((name, content) in entries) {
            val tmp = tempDir.newFile("tmp_${System.nanoTime()}.tmp")
            tmp.writeText(content, Charsets.UTF_8)
            val params = ZipParameters().apply { fileNameInZip = name }
            zip.addFile(tmp, params)
        }
        return zipPath
    }

    /** PBT 1.1: Valid ZIP with index.html should always pass validation. */
    @Test
    fun pbt_1_1_validZipWithIndexHtml_alwaysPassesValidation() = runTest {
        forAll(htmlTitleArb) { title ->
            val zip = createZip("index.html" to "<html><head><title>$title</title></head></html>")
            helper.validateZipContainsIndexHtml(zip)
        }
    }

    /** PBT 1.2: ZIP without index.html should always fail validation. */
    @Test
    fun pbt_1_2_zipWithoutIndexHtml_alwaysFailsValidation() = runTest {
        forAll(Arb.string(1..20)) { filename ->
            val safeName = filename
                .replace("/", "_")
                .replace("\\", "_")
                .replace(".", "_")
                .ifBlank { "file" }
            // Ensure it's not index.html
            if (safeName.equals("index", ignoreCase = true)) return@forAll true
            val zip = createZip("${safeName}.txt" to "content")
            !helper.validateZipContainsIndexHtml(zip)
        }
    }

    /** PBT 1.3: Title extraction should preserve the HTML title content. */
    @Test
    fun pbt_1_3_titleExtraction_preservesContent() = runTest {
        forAll(htmlTitleArb) { title ->
            if (title.isBlank()) return@forAll true
            val zip = createZip("index.html" to "<html><head><title>$title</title></head></html>")
            val extracted = helper.extractTitleFromZip(zip)
            extracted == title.trim()
        }
    }

    /** PBT: extractTitleFromZip never throws, always returns non-null. */
    @Test
    fun pbt_extractTitle_neverThrows() = runTest {
        forAll(Arb.string(0..100)) { htmlContent ->
            val zip = createZip("index.html" to htmlContent)
            val result = runCatching { helper.extractTitleFromZip(zip) }.getOrNull()
            result != null
        }
    }

    /** PBT: validateZipContainsIndexHtml never throws. */
    @Test
    fun pbt_validation_neverThrows() = runTest {
        forAll(Arb.string(0..100)) { content ->
            val zip = createZip("index.html" to content)
            val result = runCatching { helper.validateZipContainsIndexHtml(zip) }.getOrNull()
            result != null
        }
    }
}
