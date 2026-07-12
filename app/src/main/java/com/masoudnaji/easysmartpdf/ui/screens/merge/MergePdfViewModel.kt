package com.masoudnaji.easysmartpdf.ui.screens.merge

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

class MergePdfViewModel(application: Application) : AndroidViewModel(application) {

    private val mergeRepository = MergeRepositoryImpl(application)
    private val mergeUseCase = MergePdfUseCase(mergeRepository)
    private val delegate = PageOperationsDelegate(
        loadThumbnailsUseCase = LoadPageThumbnailsUseCase(PageEditorRepositoryImpl(application)),
        scope = viewModelScope
    )

    private data class MergeFileState(
        val pdfList: List<PdfEntry> = emptyList(),
        val mergeState: MergeState = MergeState.Idle,
        val errorMessage: String? = null
    )

    private val _fileState = MutableStateFlow(MergeFileState())
    private var mergeJob: Job? = null

    val uiState: StateFlow<MergePdfUiState> = combine(
        _fileState,
        delegate.state
    ) { file, org ->
        MergePdfUiState(
            pdfList = file.pdfList,
            mergeState = file.mergeState,
            errorMessage = file.errorMessage,
            pages = org.pages,
            thumbnailsLoaded = org.thumbnailsLoaded,
            thumbnailsTotal = org.thumbnailsTotal,
            zoomedPageId = org.zoomedPageId
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MergePdfUiState())

    // ── Phase 1: File selection ──────────────────────────────────────────────

    fun addPdf(uri: Uri) {
        if (_fileState.value.pdfList.any { it.uri == uri }) {
            _fileState.update { it.copy(errorMessage = "This PDF has already been added.") }
            return
        }
        delegate.clear()
        val displayName = queryDisplayName(uri)
        _fileState.update { state ->
            state.copy(pdfList = state.pdfList + PdfEntry(uri, displayName, isLoadingMetadata = true))
        }
        viewModelScope.launch {
            val (pageCount, fileSize) = withContext(Dispatchers.IO) { readSizeAndPageCount(uri) }
            _fileState.update { state ->
                val idx = state.pdfList.indexOfFirst { it.uri == uri }
                if (idx == -1) return@update state
                val updated = state.pdfList.toMutableList()
                updated[idx] = updated[idx].copy(
                    pageCount = pageCount,
                    fileSize = fileSize,
                    isLoadingMetadata = false
                )
                state.copy(pdfList = updated)
            }
        }
    }

    fun removePdf(index: Int) {
        delegate.clear()
        _fileState.update { state ->
            val updated = state.pdfList.toMutableList().also { it.removeAt(index) }
            state.copy(pdfList = updated)
        }
    }

    fun movePdf(from: Int, to: Int) {
        val list = _fileState.value.pdfList.toMutableList()
        if (from < 0 || to < 0 || from >= list.size || to >= list.size) return
        delegate.clear()
        val item = list.removeAt(from)
        list.add(to, item)
        _fileState.update { it.copy(pdfList = list) }
    }

    // ── Phase 2: Page organizer ──────────────────────────────────────────────

    fun loadPageThumbnails() {
        val flatPages = mutableListOf<PageItem>()
        _fileState.value.pdfList.forEach { entry ->
            for (i in 0 until entry.pageCount) {
                flatPages.add(
                    PageItem(
                        id = "${Uri.encode(entry.uri.toString())}#$i",
                        sourceUri = entry.uri,
                        sourcePageIndex = i,
                        sourceFileName = entry.displayName
                    )
                )
            }
        }
        delegate.loadThumbnails(flatPages)
    }

    fun rotatePage(pageId: String, clockwise: Boolean) = delegate.rotate(pageId, clockwise)
    fun deletePage(pageId: String) = delegate.delete(pageId)
    fun movePage(from: Int, to: Int) = delegate.move(from, to)
    fun setZoomedPage(pageId: String?) = delegate.setZoomedPage(pageId)

    // ── Phase 3: Merge ───────────────────────────────────────────────────────

    fun startMerge() {
        val pages = delegate.state.value.pages
        val currentMergeState = _fileState.value.mergeState
        if (currentMergeState !is MergeState.Idle && currentMergeState !is MergeState.Cancelled) return
        if (pages.isEmpty()) return

        val config = MergeConfig(
            pages = pages,
            outputFileName = buildOutputFileName()
        )

        mergeJob = viewModelScope.launch {
            mergeUseCase(config).collect { event ->
                when (event) {
                    is MergeEvent.Started ->
                        _fileState.update { it.copy(mergeState = MergeState.Started) }
                    is MergeEvent.Progress ->
                        _fileState.update { it.copy(mergeState = MergeState.InProgress(event.current, event.total)) }
                    is MergeEvent.Completed ->
                        _fileState.update { it.copy(mergeState = MergeState.Completed(event.fileName)) }
                    is MergeEvent.Failed ->
                        _fileState.update { it.copy(mergeState = MergeState.Idle, errorMessage = event.userMessage) }
                }
            }
        }
    }

    fun cancelMerge() {
        mergeJob?.cancel()
        _fileState.update { it.copy(mergeState = MergeState.Cancelled) }
    }

    fun onErrorDismissed() {
        _fileState.update { it.copy(errorMessage = null) }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun buildOutputFileName(): String {
        val ts = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US).format(Date())
        return "Merged_$ts.pdf"
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
            if (cursor.moveToFirst()) {
                fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))
            }
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
