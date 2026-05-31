package com.example.htmlbrowser.utils

import android.util.Log
import com.example.htmlbrowser.domain.model.AppError

object ErrorHandler {

    fun handleError(error: AppError): String {
        return when (error) {
            is AppError.FileNotFound -> "File not found: ${error.message}"
            is AppError.InvalidArchive -> "Invalid archive: ${error.message}"
            is AppError.StorageFull -> "Insufficient storage space. Please free up at least ${error.message}."
            is AppError.NetworkError -> "Network error: ${error.message}"
            is AppError.UnknownError -> "An error occurred: ${error.message}"
        }
    }

    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }

    fun getImportErrorMessage(errorMessage: String?): String {
        return if (!errorMessage.isNullOrBlank()) {
            errorMessage
        } else {
            "Import failed. Please try again."
        }
    }

    fun getDeleteErrorMessage(archiveTitle: String): String {
        return "Failed to delete \"$archiveTitle\". Please try again."
    }
}
