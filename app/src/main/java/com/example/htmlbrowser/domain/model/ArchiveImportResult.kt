package com.example.htmlbrowser.domain.model

data class ArchiveImportResult(
    val success: Boolean,
    val archive: HtmlArchive? = null,
    val errorMessage: String? = null
)