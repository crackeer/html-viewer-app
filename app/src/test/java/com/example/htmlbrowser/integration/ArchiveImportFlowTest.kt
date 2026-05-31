package com.example.htmlbrowser.integration

import com.example.htmlbrowser.domain.ArchiveManagerTest.ZipValidationHelper
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

/**
 * Integration tests for the complete archive import flow:
 * ZIP validation → title extraction → extraction completeness.
 */
class ArchiveImportFlowTest {

    @get:Rule
    val tempDir = TemporaryFolder()

    private val helper = ZipValidationHelper()

    private fun createZip4jWithEntries(vararg entries: Pair<String, String>): File {
        val zipPath = tempDir.newFile("flow_${System.nanoTime()}.zip")
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

    // ─── Full happy-path flow ────────────────────────────────────────────────────

    @Test
    fun fullImportFlow_validZip_succeeds() = runTest {
        val zip = createZip4jWithEntries(
            "index.html" to "<html><head><title>Integration Test</title></head><body>Hello</body></html>",
            "style.css" to "body { font-family: sans-serif; }",
            "app.js" to "console.log('loaded');"
        )

        // Step 1: Validate ZIP contains index.html
        assertTrue("ZIP should contain index.html", helper.validateZipContainsIndexHtml(zip))

        // Step 2: Extract title
        val title = helper.extractTitleFromZip(zip)
        assertEquals("Integration Test", title)

        // Step 3: Extract to destination
        val destDir = tempDir.newFolder("dest_${System.nanoTime()}")
        ZipFile(zip).extractAll(destDir.absolutePath)

        // Step 4: Verify all files extracted
        val extractedFiles = destDir.walk().filter { it.isFile }.map { it.name }.toSet()
        assertTrue("index.html should be extracted", "index.html" in extractedFiles)
        assertTrue("style.css should be extracted", "style.css" in extractedFiles)
        assertTrue("app.js should be extracted", "app.js" in extractedFiles)

        // Step 5: Verify index.html content
        val indexFile = File(destDir, "index.html")
        assertTrue("index.html should exist", indexFile.exists())
        assertTrue("index.html should contain title", indexFile.readText().contains("Integration Test"))
    }

    // ─── Error path: missing index.html ─────────────────────────────────────────

    @Test
    fun importFlow_missingIndexHtml_failsValidation() = runTest {
        val zip = createZip4jWithEntries(
            "readme.txt" to "This ZIP has no index.html",
            "data.json" to "{}"
        )
        assertFalse("ZIP without index.html should fail validation",
            helper.validateZipContainsIndexHtml(zip))
    }

    // ─── Error path: no title tag ────────────────────────────────────────────────

    @Test
    fun importFlow_noTitleTag_usesDefaultTitle() = runTest {
        val zip = createZip4jWithEntries(
            "index.html" to "<html><body>No title here</body></html>"
        )
        assertTrue(helper.validateZipContainsIndexHtml(zip))
        assertEquals("Untitled Archive", helper.extractTitleFromZip(zip))
    }

    // ─── Content integrity after extraction ──────────────────────────────────────

    @Test
    fun importFlow_extractedContentMatchesOriginal() = runTest {
        val cssContent = "body { color: blue; margin: 0; }"
        val jsContent = "function init() { return true; }"
        val htmlContent = "<html><head><title>Content Test</title></head><body></body></html>"

        val zip = createZip4jWithEntries(
            "index.html" to htmlContent,
            "style.css" to cssContent,
            "app.js" to jsContent
        )

        val destDir = tempDir.newFolder("content_${System.nanoTime()}")
        ZipFile(zip).extractAll(destDir.absolutePath)

        assertEquals(htmlContent, File(destDir, "index.html").readText(Charsets.UTF_8))
        assertEquals(cssContent, File(destDir, "style.css").readText(Charsets.UTF_8))
        assertEquals(jsContent, File(destDir, "app.js").readText(Charsets.UTF_8))
    }
}
