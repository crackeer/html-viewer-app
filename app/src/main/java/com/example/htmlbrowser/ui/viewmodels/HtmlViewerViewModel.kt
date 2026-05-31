package com.example.htmlbrowser.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.htmlbrowser.HtmlBrowserApplication
import com.example.htmlbrowser.data.repository.ArchiveRepository
import com.example.htmlbrowser.domain.manager.StorageManager
import com.example.htmlbrowser.domain.model.HtmlArchive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HtmlViewerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as HtmlBrowserApplication).database
    private val storageManager = StorageManager(application)
    private val repository = ArchiveRepository(db.archiveDao(), storageManager)

    private val _archive = MutableStateFlow<HtmlArchive?>(null)
    val archive: StateFlow<HtmlArchive?> = _archive.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadArchive(archiveId: String) {
        viewModelScope.launch {
            val result = repository.getArchiveById(archiveId)
            if (result != null) {
                _archive.value = result
            } else {
                _errorMessage.value = "Archive not found"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
