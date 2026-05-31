package com.example.htmlbrowser.domain.model

sealed class AppError {
    data class FileNotFound(val message: String) : AppError()
    data class InvalidArchive(val message: String) : AppError()
    data class StorageFull(val message: String) : AppError()
    data class NetworkError(val message: String) : AppError()
    data class UnknownError(val message: String) : AppError()
}