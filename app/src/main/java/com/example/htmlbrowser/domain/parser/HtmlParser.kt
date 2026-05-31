package com.example.htmlbrowser.domain.parser

import java.io.File
import java.io.IOException
import java.util.regex.Pattern

object HtmlParser {

    /**
     * Extracts the content of the first <title> tag (case-insensitive, handles attributes).
     * Trims whitespace from the result.
     * Returns "Untitled Archive" if no title tag found, title is blank, or on any exception.
     */
    fun extractTitle(htmlContent: String): String {
        return try {
            // Pattern handles optional attributes on the <title> tag, e.g. <title lang="en">
            val pattern = Pattern.compile(
                "<title[^>]*>(.*?)</title>",
                Pattern.CASE_INSENSITIVE or Pattern.DOTALL
            )
            val matcher = pattern.matcher(htmlContent)
            if (matcher.find()) {
                val title = matcher.group(1)?.trim().orEmpty()
                if (title.isNotBlank()) title else "Untitled Archive"
            } else {
                "Untitled Archive"
            }
        } catch (e: Exception) {
            "Untitled Archive"
        }
    }

    /**
     * Reads the file as UTF-8 text and delegates to extractTitle().
     * Returns "Untitled Archive" on any IOException.
     */
    fun extractTitleFromFile(file: File): String {
        return try {
            val htmlContent = file.readText(Charsets.UTF_8)
            extractTitle(htmlContent)
        } catch (e: IOException) {
            "Untitled Archive"
        }
    }

    /**
     * Returns true if the content contains at least one <html tag (case-insensitive).
     * Returns false for blank/empty content.
     */
    fun isValidHtml(htmlContent: String): Boolean {
        if (htmlContent.isBlank()) return false
        return htmlContent.contains("<html", ignoreCase = true)
    }

    /**
     * Extracts content from <meta name="description" content="..."> (case-insensitive).
     * Returns null if not found.
     */
    fun extractMetaDescription(htmlContent: String): String? {
        return try {
            // Match <meta ... name="description" ... content="..."> in any attribute order
            val pattern = Pattern.compile(
                """<meta[^>]+name\s*=\s*["']description["'][^>]+content\s*=\s*["']([^"']*)["'][^>]*>""",
                Pattern.CASE_INSENSITIVE
            )
            val matcher = pattern.matcher(htmlContent)
            if (matcher.find()) {
                matcher.group(1)
            } else {
                // Also try content before name attribute order
                val patternReversed = Pattern.compile(
                    """<meta[^>]+content\s*=\s*["']([^"']*)["'][^>]+name\s*=\s*["']description["'][^>]*>""",
                    Pattern.CASE_INSENSITIVE
                )
                val matcherReversed = patternReversed.matcher(htmlContent)
                if (matcherReversed.find()) matcherReversed.group(1) else null
            }
        } catch (e: Exception) {
            null
        }
    }
}
