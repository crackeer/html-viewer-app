package com.example.htmlbrowser.ui.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.htmlbrowser.HtmlBrowserApplication
import com.example.htmlbrowser.data.repository.ArchiveRepository
import com.example.htmlbrowser.domain.manager.ArchiveManager
import com.example.htmlbrowser.domain.manager.StorageManager
import com.example.htmlbrowser.domain.model.HtmlArchive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ImportState {
    object Idle : ImportState()
    object Loading : ImportState()
    data class Success(val archive: HtmlArchive) : ImportState()
    data class Error(val message: String) : ImportState()
}

class ArchiveListViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as HtmlBrowserApplication).database
    private val storageManager = StorageManager(application)
    private val archiveManager = ArchiveManager(application)
    private val repository = ArchiveRepository(db.archiveDao(), storageManager)

    val archives = repository.getAllArchives()

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    private val _deleteState = MutableStateFlow<Boolean?>(null)
    val deleteState: StateFlow<Boolean?> = _deleteState.asStateFlow()

    fun importArchive(uri: Uri) {
        viewModelScope.launch {
            _importState.value = ImportState.Loading
            val result = archiveManager.importArchive(uri)
            if (result.success && result.archive != null) {
                repository.insertArchive(result.archive)
                _importState.value = ImportState.Success(result.archive)
            } else {
                _importState.value = ImportState.Error(result.errorMessage ?: "Import failed")
            }
        }
    }

    fun deleteArchive(archive: HtmlArchive) {
        viewModelScope.launch {
            val success = repository.deleteArchive(archive)
            _deleteState.value = success
        }
    }

    fun resetImportState() {
        _importState.value = ImportState.Idle
    }
}
