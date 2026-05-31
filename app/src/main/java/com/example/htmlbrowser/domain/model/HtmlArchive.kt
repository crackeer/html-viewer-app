package com.example.htmlbrowser.domain.model

import java.util.Date
import java.util.UUID

data class HtmlArchive(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val originalFileName: String,
    val extractPath: String,
    val sizeBytes: Long,
    val importDate: Date = Date(),
    val lastAccessed: Date = Date()
)