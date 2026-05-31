package com.example.htmlbrowser.utils

import java.math.BigDecimal
import java.math.RoundingMode

object FileSizeUtils {

    private const val BYTES_PER_MB = 1_048_576.0 // 1024 * 1024

    /**
     * Formats a byte count as a storage size string in "X.XX MB" format.
     * Satisfies Requirement 4.8: display storage sizes using the format "X.XX MB"
     * where X.XX is the size in megabytes with 2 decimal places.
     *
     * Examples:
     *   0L         → "0.00 MB"
     *   1048576L   → "1.00 MB"
     *   1572864L   → "1.50 MB"
     */
    fun formatBytes(bytes: Long): String {
        val mb = bytesToMb(bytes)
        return "%.2f MB".format(mb)
    }

    /**
     * Converts a byte count to megabytes, rounded to 2 decimal places.
     *
     * Examples:
     *   0L         → 0.00
     *   1048576L   → 1.00
     *   1572864L   → 1.50
     */
    fun bytesToMb(bytes: Long): Double {
        val raw = bytes / BYTES_PER_MB
        return BigDecimal(raw).setScale(2, RoundingMode.HALF_UP).toDouble()
    }
}
