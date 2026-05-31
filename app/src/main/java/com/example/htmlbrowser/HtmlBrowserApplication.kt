package com.example.htmlbrowser

import android.app.Application
import com.example.htmlbrowser.data.database.AppDatabase

class HtmlBrowserApplication : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize the database eagerly on app start
        AppDatabase.getInstance(this)
    }
}
