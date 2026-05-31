package com.example.htmlbrowser.integration

import com.example.htmlbrowser.domain.model.AppError
import com.example.htmlbrowser.utils.ErrorHandler
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.forAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for error handling scenarios and the ErrorHandler utility.
 */
class ErrorHandlingTest {

    // ─── handleError ─────────────────────────────────────────────────────────────

    @Test
    fun fileNotFound_producesCorrectMessage() {
        val error = AppError.FileNotFound("archive.zip")
        val msg = ErrorHandler.handleError(error)
        assertTrue(msg.contains("File not found"))
        assertTrue(msg.contains("archive.zip"))
    }

    @Test
    fun invalidArchive_producesCorrectMessage() {
        val error = AppError.InvalidArchive("missing index.html")
        val msg = ErrorHandler.handleError(error)
        assertTrue(msg.contains("Invalid archive"))
    }

    @Test
    fun storageFull_producesCorrectMessage() {
        val error = AppError.StorageFull("50 MB")
        val msg = ErrorHandler.handleError(error)
        assertTrue(msg.contains("storage space"))
    }

    @Test
    fun unknownError_producesCorrectMessage() {
        val error = AppError.UnknownError("something went wrong")
        val msg = ErrorHandler.handleError(error)
        assertTrue(msg.contains("error occurred"))
    }

    // ─── getImportErrorMessage ────────────────────────────────────────────────────

    @Test
    fun importError_withMessage_returnsMessage() {
        val msg = ErrorHandler.getImportErrorMessage("ZIP file is corrupted")
        assertEquals("ZIP file is corrupted", msg)
    }

    @Test
    fun importError_nullMessage_returnsDefault() {
        val msg = ErrorHandler.getImportErrorMessage(null)
        assertEquals("Import failed. Please try again.", msg)
    }

    @Test
    fun importError_blankMessage_returnsDefault() {
        val msg = ErrorHandler.getImportErrorMessage("   ")
        assertEquals("Import failed. Please try again.", msg)
    }

    // ─── getDeleteErrorMessage ────────────────────────────────────────────────────

    @Test
    fun deleteError_containsArchiveTitle() {
        val msg = ErrorHandler.getDeleteErrorMessage("My Archive")
        assertTrue(msg.contains("My Archive"))
        assertTrue(msg.contains("Failed to delete"))
    }

    // ─── Property-Based Tests ────────────────────────────────────────────────────

    @Test
    fun pbt_3_1_allErrors_produceNonEmptyMessages() = runTest {
        forAll(Arb.string(1..50)) { message ->
            val errors = listOf(
                AppError.FileNotFound(message),
                AppError.InvalidArchive(message),
                AppError.StorageFull(message),
                AppError.NetworkError(message),
                AppError.UnknownError(message)
            )
            errors.all { ErrorHandler.handleError(it).isNotEmpty() }
        }
    }

    @Test
    fun pbt_3_2_errorHandling_isDeterministic() = runTest {
        forAll(Arb.string(1..50)) { message ->
            val error = AppError.UnknownError(message)
            val result1 = ErrorHandler.handleError(error)
            val result2 = ErrorHandler.handleError(error)
            result1 == result2
        }
    }
}
