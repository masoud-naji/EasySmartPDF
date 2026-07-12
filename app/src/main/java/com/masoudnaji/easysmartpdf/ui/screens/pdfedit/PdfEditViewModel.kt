package com.masoudnaji.easysmartpdf.ui.screens.pdfedit

import android.app.Application
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.masoudnaji.easysmartpdf.data.repository.MergeRepositoryImpl
import com.masoudnaji.easysmartpdf.data.repository.PageEditorRepositoryImpl
import com.masoudnaji.easysmartpdf.domain.model.MergeConfig
import com.masoudnaji.easysmartpdf.domain.model.MergeEvent
import com.masoudnaji.easysmartpdf.domain.model.PageItem
import com.masoudnaji.easysmartpdf.domain.usecase.LoadPageThumbnailsUseCase
import com.masoudnaji.easysmartpdf.domain.usecase.MergePdfUseCase
import com.masoudnaji.easysmartpdf.ui.screens.merge.PdfEntry
import com.masoudnaji.easysmartpdf.ui.screens.pageorganizer.PageOperationsDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfEditViewModel(application: Application) : AndroidViewModel(application) {

    private val saveUseCase = MergePdfUseCase(MergeRepositoryImpl(application))
    private val delegate = PageOperationsDelegate(
        loadThumbnailsUseCase = LoadPageThumbnailsUseCase(PageEditorRepositoryImpl(application)),
        scope = viewModelScope
    )

    private data class FileState(
        val selectedFile: PdfEntry? = null,
        val saveState: PdfEditSaveState = PdfEditSaveState.Idle,
        val errorMessage: String? = null
    )

    private val _fileState = MutableStateFlow(FileState())
    private var saveJob: Job? = null

    val uiState: StateFlow<PdfEditUiState> = combine(
        _fileState, delegate.state
    ) { file, org ->
        PdfEditUiState(
            selectedFile = file.selectedFile,
            saveState = file.saveState,
            errorMessage = file.errorMessage,
            pages = org.pages,
            thumbnailsLoaded = org.thumbnailsLoaded,
            thumbnailsTotal = org.thumbnailsTotal,
            zoomedPageId = org.zoomedPageId
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, PdfEditUiState())

    fun selectPdf(uri: Uri) {
        delegate.clear()
        val displayName = queryDisplayName(uri)
        _fileState.update {
            it.copy(
                selectedFile = PdfEntry(uri, displayName, isLoadingMetadata = true),
                saveState = PdfEditSaveState.Idle
            )
        }
        viewModelScope.launch {
            val (pageCount, fileSize) = withContext(Dispatchers.IO) { readSizeAndPageCount(uri) }
            _fileState.update { state ->
                state.copy(
                    selectedFile = state.selectedFile?.copy(
                        pageCount = pageCount,
                        fileSize = fileSize,
                        isLoadingMetadata = false
                    )
                )
            }
        }
    }

    fun loadPageThumbnails() {
        val entry = _fileState.value.selectedFile ?: return
        val pages = (0 until entry.pageCount).map { i ->
            PageItem(
                id = "${Uri.encode(entry.uri.toString())}#$i",
                sourceUri = entry.uri,
                sourcePageIndex = i,
                sourceFileName = entry.displayName
            )
        }
        delegate.loadThumbnails(pages)
    }

    fun rotatePage(pageId: String, clockwise: Boolean) = delegate.rotate(pageId, clockwise)
    fun deletePage(pageId: String) = delegate.delete(pageId)
    fun movePage(from: Int, to: Int) = delegate.move(from, to)
    fun setZoomedPage(pageId: String?) = delegate.setZoomedPage(pageId)
    fun updatePage(updated: PageItem) = delegate.updatePage(updated)

    fun startSave() {
        val pages = delegate.state.value.pages
        val current = _fileState.value.saveState
        if (current !is PdfEditSaveState.Idle && current !is PdfEditSaveState.Cancelled) return
        if (pages.isEmpty()) return

        val config = MergeConfig(pages = pages, outputFileName = buildOutputFileName())
        saveJob = viewModelScope.launch {
            saveUseCase(config).collect { event ->
                when (event) {
                    is MergeEvent.Started ->
                        _fileState.update { it.copy(saveState = PdfEditSaveState.Started) }
                    is MergeEvent.Progress ->
                        _fileState.update { it.copy(saveState = PdfEditSaveState.InProgress(event.current, event.total)) }
                    is MergeEvent.Completed ->
                        _fileState.update { it.copy(saveState = PdfEditSaveState.Completed(event.fileName)) }
                    is MergeEvent.Failed ->
                        _fileState.update { it.copy(saveState = PdfEditSaveState.Idle, errorMessage = event.userMessage) }
                }
            }
        }
    }

    fun cancelSave() {
        saveJob?.cancel()
        _fileState.update { it.copy(saveState = PdfEditSaveState.Cancelled) }
    }

    fun onErrorDismissed() = _fileState.update { it.copy(errorMessage = null) }

    private fun buildOutputFileName(): String {
        val ts = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US).format(Date())
        val baseName = _fileState.value.selectedFile?.displayName?.removeSuffix(".pdf") ?: "PDF"
        return "Edited_${baseName}_$ts.pdf"
    }

    private fun queryDisplayName(uri: Uri): String {
        val cr = getApplication<Application>().contentResolver
        cr.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                    ?: uri.lastPathSegment ?: "PDF"
            }
        }
        return uri.lastPathSegment ?: "PDF"
    }

    private fun readSizeAndPageCount(uri: Uri): Pair<Int, Long> {
        val cr = getApplication<Application>().contentResolver
        var fileSize = 0L
        cr.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))
        }
        var pageCount = 0
        try {
            cr.openFileDescriptor(uri, "r")?.let { pfd ->
                PdfRenderer(pfd).use { pageCount = it.pageCount }
            }
        } catch (_: Exception) {}
        return Pair(pageCount, fileSize)
    }

    override fun onCleared() {
        super.onCleared()
        delegate.clear()
    }
}
