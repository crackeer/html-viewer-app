package com.example.htmlbrowser.ui.dialogs

import android.content.Context
import androidx.appcompat.app.AlertDialog

object DialogHelper {

    fun showDeleteConfirmation(context: Context, archiveTitle: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("Delete Archive")
            .setMessage("Delete \"$archiveTitle\"? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> onConfirm() }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    fun showError(context: Context, message: String) {
        AlertDialog.Builder(context)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    fun showImportProgress(context: Context): AlertDialog {
        return AlertDialog.Builder(context)
            .setMessage("Importing archive…")
            .setCancelable(false)
            .show()
    }
}
