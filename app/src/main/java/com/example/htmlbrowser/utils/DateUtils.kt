package com.example.htmlbrowser.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    private val importDateFormat = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())

    /**
     * Formats a date as a human-readable import date string.
     * Example: "Jan 5, 2024 at 3:45 PM"
     */
    fun formatImportDate(date: Date): String {
        return importDateFormat.format(date)
    }

    /**
     * Formats a date as a relative time string.
     * Examples: "just now", "5 minutes ago", "2 hours ago", "3 days ago"
     */
    fun formatLastAccessed(date: Date): String {
        val now = System.currentTimeMillis()
        val diffMs = now - date.time

        if (diffMs < 0) return "just now"

        val seconds = diffMs / 1_000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        val years = days / 365

        return when {
            seconds < 60 -> "just now"
            minutes < 60 -> if (minutes == 1L) "1 minute ago" else "$minutes minutes ago"
            hours < 24 -> if (hours == 1L) "1 hour ago" else "$hours hours ago"
            days < 7 -> if (days == 1L) "1 day ago" else "$days days ago"
            weeks < 5 -> if (weeks == 1L) "1 week ago" else "$weeks weeks ago"
            months < 12 -> if (months == 1L) "1 month ago" else "$months months ago"
            else -> if (years == 1L) "1 year ago" else "$years years ago"
        }
    }
}
