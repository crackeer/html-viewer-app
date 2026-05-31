package com.example.htmlbrowser.data.preferences

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(private val context: Context) {

    companion object {
        const val PREFS_NAME = "html_browser_prefs"
        const val KEY_SORT_ORDER = "sort_order"
        const val KEY_LAST_VIEWED_ARCHIVE = "last_viewed_archive"
        const val KEY_SHOW_FILE_SIZE = "show_file_size"
        const val KEY_DARK_MODE = "dark_mode"
        const val DEFAULT_SORT_ORDER = "import_date_desc"
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSortOrder(): String {
        return prefs.getString(KEY_SORT_ORDER, DEFAULT_SORT_ORDER) ?: DEFAULT_SORT_ORDER
    }

    fun setSortOrder(order: String) {
        prefs.edit().putString(KEY_SORT_ORDER, order).apply()
    }

    fun getLastViewedArchiveId(): String? {
        return prefs.getString(KEY_LAST_VIEWED_ARCHIVE, null)
    }

    fun setLastViewedArchiveId(archiveId: String) {
        prefs.edit().putString(KEY_LAST_VIEWED_ARCHIVE, archiveId).apply()
    }

    fun isShowFileSize(): Boolean {
        return prefs.getBoolean(KEY_SHOW_FILE_SIZE, true)
    }

    fun setShowFileSize(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_FILE_SIZE, show).apply()
    }

    fun isDarkMode(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
