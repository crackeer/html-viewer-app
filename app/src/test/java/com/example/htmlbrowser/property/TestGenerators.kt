package com.example.htmlbrowser.property

import com.example.htmlbrowser.domain.model.HtmlArchive
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import java.util.Date
import java.util.UUID

/**
 * Kotest Arb generators for property-based tests.
 */
object TestGenerators {

    /** Generates a valid HtmlArchive with random field values. */
    val htmlArchiveArb: Arb<HtmlArchive> = arbitrary {
        HtmlArchive(
            id = UUID.randomUUID().toString(),
            title = Arb.string(1..50).bind().ifBlank { "Untitled Archive" },
            originalFileName = "${Arb.string(1..20).bind()}.zip",
            extractPath = "/data/archives/${UUID.randomUUID()}",
            sizeBytes = Arb.long(0L..100L * 1024 * 1024).bind(),
            importDate = Date(Arb.long(0L..System.currentTimeMillis()).bind()),
            lastAccessed = Date(Arb.long(0L..System.currentTimeMillis()).bind())
        )
    }

    /** Generates a list of 1–10 HtmlArchive objects. */
    val archiveListArb: Arb<List<HtmlArchive>> = Arb.list(htmlArchiveArb, 1..10)

    /** Generates a valid HTML title string (no angle brackets). */
    val htmlTitleArb: Arb<String> = arbitrary {
        Arb.string(1..50).bind()
            .replace("<", "")
            .replace(">", "")
            .trim()
            .ifBlank { "Default Title" }
    }

    /** Generates a file size in bytes (0 to 100 MB). */
    val fileSizeArb: Arb<Long> = Arb.long(0L..100L * 1024 * 1024)

    /** Generates a list of file sizes (1–20 files). */
    val fileSizeListArb: Arb<List<Int>> = Arb.list(Arb.int(0..10240), 1..20)

    /** Generates a UUID string. */
    val uuidArb: Arb<String> = arbitrary { UUID.randomUUID().toString() }

    /** Generates a list of unique UUID strings. */
    val uniqueIdListArb: Arb<List<String>> = arbitrary {
        val count = Arb.int(2..10).bind()
        (1..count).map { UUID.randomUUID().toString() }
    }
}
