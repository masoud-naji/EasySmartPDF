package com.masoudnaji.easysmartpdf.ui.screens.scanner

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.masoudnaji.easysmartpdf.data.repository.PageEditorRepositoryImpl
import com.masoudnaji.easysmartpdf.data.repository.ScanToPdfRepositoryImpl
import com.masoudnaji.easysmartpdf.domain.model.PageItem
import com.masoudnaji.easysmartpdf.domain.model.PageSourceType
import com.masoudnaji.easysmartpdf.domain.model.ScanToPdfEvent
import com.masoudnaji.easysmartpdf.domain.usecase.LoadPageThumbnailsUseCase
import com.masoudnaji.easysmartpdf.domain.usecase.ScanToPdfUseCase
import com.masoudnaji.easysmartpdf.ui.screens.pageorganizer.PageOperationsDelegate
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val scanUseCase = ScanToPdfUseCase(ScanToPdfRepositoryImpl(application))
    private val delegate = PageOperationsDelegate(
        loadThumbnailsUseCase = LoadPageThumbnailsUseCase(PageEditorRepositoryImpl(application)),
        scope = viewModelScope
    )

    private val sessionDir = File(application.filesDir, "scanner_session")

    private data class ScanFileState(
        val scanState: ScannerScanState = ScannerScanState.Idle,
        val errorMessage: String? = null
    )

    init {
        sessionDir.mkdirs()
    }

    private val _fileState = MutableStateFlow(ScanFileState())
    private var scanJob: Job? = null

    val uiState: StateFlow<ScannerUiState> = combine(
        _fileState, delegate.state
    ) { file, org ->
        ScannerUiState(
            pages = org.pages,
            thumbnailsLoaded = org.thumbnailsLoaded,
            thumbnailsTotal = org.thumbnailsTotal,
            zoomedPageId = org.zoomedPageId,
            errorMessage = file.errorMessage,
            scanState = file.scanState
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ScannerUiState())

    fun addPhotos(uris: List<Uri>) {
        viewModelScope.launch {
            val sessionUris = uris.mapNotNull { uri ->
                copyUriToSession(uri)
            }
            if (sessionUris.isEmpty()) return@launch

            val existing = delegate.state.value.pages
            val newPages = sessionUris.mapIndexed { i, uri ->
                val sessionIndex = existing.size + i
                PageItem(
                    id = "scan_${UUID.randomUUID()}",
                    sourceUri = uri,
                    sourcePageIndex = 0,
                    sourceFileName = "page_${sessionIndex + 1}.jpg",
                    sourceType = PageSourceType.IMAGE
                )
            }
            delegate.loadThumbnails(existing + newPages)
        }
    }

    private fun copyUriToSession(uri: Uri): Uri? {
        return try {
            val fileName = "scan_${UUID.randomUUID()}.jpg"
            val destFile = File(sessionDir, fileName)
            getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(destFile)
        } catch (e: Exception) {
            null
        }
    }

    fun clearSession() {
        sessionDir.deleteRecursively()
        sessionDir.mkdirs()
    }

    fun rotatePage(pageId: String, clockwise: Boolean) = delegate.rotate(pageId, clockwise)
    fun deletePage(pageId: String) = delegate.delete(pageId)
    fun movePage(from: Int, to: Int) = delegate.move(from, to)
    fun setZoomedPage(pageId: String?) = delegate.setZoomedPage(pageId)
    fun updatePage(updated: PageItem) = delegate.updatePage(updated)
    fun onErrorDismissed() = _fileState.update { it.copy(errorMessage = null) }

    fun startScan() {
        val pages = delegate.state.value.pages
        val current = _fileState.value.scanState
        if (current !is ScannerScanState.Idle && current !is ScannerScanState.Cancelled) return
        if (pages.isEmpty()) return

        val outputFileName = buildOutputFileName()
        scanJob = viewModelScope.launch {
            scanUseCase(pages, outputFileName).collect { event ->
                when (event) {
                    is ScanToPdfEvent.Started ->
                        _fileState.update { it.copy(scanState = ScannerScanState.Started) }
                    is ScanToPdfEvent.Progress ->
                        _fileState.update {
                            it.copy(scanState = ScannerScanState.InProgress(event.current, event.total))
                        }
                    is ScanToPdfEvent.Completed ->
                        _fileState.update { it.copy(scanState = ScannerScanState.Completed(event.fileName)) }
                    is ScanToPdfEvent.Failed ->
                        _fileState.update { it.copy(scanState = ScannerScanState.Idle, errorMessage = event.userMessage) }
                }
            }
        }
    }

    fun cancelScan() {
        scanJob?.cancel()
        _fileState.update { it.copy(scanState = ScannerScanState.Cancelled) }
    }

    private fun buildOutputFileName(): String {
        val ts = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US).format(Date())
        return "Scan_$ts.pdf"
    }

    override fun onCleared() {
        super.onCleared()
        delegate.clear()
        clearSession()
    }
}
