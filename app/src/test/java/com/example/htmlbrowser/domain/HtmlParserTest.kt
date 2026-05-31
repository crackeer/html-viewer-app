package com.example.htmlbrowser.domain

import com.example.htmlbrowser.domain.parser.HtmlParser
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.forAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class HtmlParserTest {

    @get:Rule
    val tempDir = TemporaryFolder()

    // ─── extractTitle ────────────────────────────────────────────────────────────

    @Test
    fun simpleTitleTag_extractsTitle() {
        assertEquals("Hello World", HtmlParser.extractTitle("<html><head><title>Hello World</title></head></html>"))
    }

    @Test
    fun titleWithAttributes_extractsTitle() {
        assertEquals("My Page", HtmlParser.extractTitle("<title lang=\"en\">My Page</title>"))
    }

    @Test
    fun titleWithWhitespace_trimmed() {
        assertEquals("Trimmed", HtmlParser.extractTitle("<title>  Trimmed  </title>"))
    }

    @Test
    fun noTitleTag_returnsDefault() {
        assertEquals("Untitled Archive", HtmlParser.extractTitle("<html><body>No title</body></html>"))
    }

    @Test
    fun emptyTitleTag_returnsDefault() {
        assertEquals("Untitled Archive", HtmlParser.extractTitle("<title></title>"))
    }

    @Test
    fun blankTitleTag_returnsDefault() {
        assertEquals("Untitled Archive", HtmlParser.extractTitle("<title>   </title>"))
    }

    @Test
    fun emptyString_returnsDefault() {
        assertEquals("Untitled Archive", HtmlParser.extractTitle(""))
    }

    @Test
    fun caseInsensitiveTitleTag_extractsTitle() {
        assertEquals("Case Test", HtmlParser.extractTitle("<TITLE>Case Test</TITLE>"))
    }

    @Test
    fun multilineTitleContent_extractsAndTrims() {
        assertEquals("Multi Line", HtmlParser.extractTitle("<title>\n  Multi Line\n</title>"))
    }

    // ─── extractTitleFromFile ────────────────────────────────────────────────────

    @Test
    fun fileWithTitle_extractsTitle() {
        val file = tempDir.newFile("test.html")
        file.writeText("<html><head><title>File Title</title></head></html>", Charsets.UTF_8)
        assertEquals("File Title", HtmlParser.extractTitleFromFile(file))
    }

    @Test
    fun nonExistentFile_returnsDefault() {
        val file = File(tempDir.root, "missing.html")
        assertEquals("Untitled Archive", HtmlParser.extractTitleFromFile(file))
    }

    // ─── isValidHtml ─────────────────────────────────────────────────────────────

    @Test
    fun htmlWithHtmlTag_isValid() {
        assertTrue(HtmlParser.isValidHtml("<html><body>Content</body></html>"))
    }

    @Test
    fun htmlWithUppercaseHtmlTag_isValid() {
        assertTrue(HtmlParser.isValidHtml("<HTML><BODY>Content</BODY></HTML>"))
    }

    @Test
    fun emptyString_isNotValid() {
        assertFalse(HtmlParser.isValidHtml(""))
    }

    @Test
    fun blankString_isNotValid() {
        assertFalse(HtmlParser.isValidHtml("   "))
    }

    @Test
    fun plainText_isNotValid() {
        assertFalse(HtmlParser.isValidHtml("Just plain text without html tag"))
    }

    // ─── extractMetaDescription ──────────────────────────────────────────────────

    @Test
    fun metaDescriptionPresent_extracted() {
        val html = """<meta name="description" content="A great page">"""
        assertEquals("A great page", HtmlParser.extractMetaDescription(html))
    }

    @Test
    fun metaDescriptionReversedOrder_extracted() {
        val html = """<meta content="Reversed order" name="description">"""
        assertEquals("Reversed order", HtmlParser.extractMetaDescription(html))
    }

    @Test
    fun noMetaDescription_returnsNull() {
        assertNull(HtmlParser.extractMetaDescription("<html><body>No meta</body></html>"))
    }

    // ─── Property-Based Tests ────────────────────────────────────────────────────

    @Test
    fun pbt_extractTitle_neverThrows_alwaysNonNull() = runTest {
        forAll(Arb.string(0..200)) { html ->
            val result = runCatching { HtmlParser.extractTitle(html) }.getOrNull()
            result != null
        }
    }

    @Test
    fun pbt_extractTitle_alwaysReturnsNonEmptyString() = runTest {
        forAll(Arb.string(0..200)) { html ->
            val result = HtmlParser.extractTitle(html)
            result.isNotEmpty()
        }
    }
}
