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
import com.masoudnaji.easysmartpdf.domain.model.ThumbnailState
import com.masoudnaji.easysmartpdf.domain.usecase.LoadPageThumbnailsUseCase
import com.masoudnaji.easysmartpdf.domain.usecase.MergePdfUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MergePdfViewModel(application: Application) : AndroidViewModel(application) {

    private val mergeRepository = MergeRepositoryImpl(application)
    private val pageEditorRepository = PageEditorRepositoryImpl(application)
    private val mergeUseCase = MergePdfUseCase(mergeRepository)
    private val loadThumbnailsUseCase = LoadPageThumbnailsUseCase(pageEditorRepository)

    private val _uiState = MutableStateFlow(MergePdfUiState())
    val uiState: StateFlow<MergePdfUiState> = _uiState.asStateFlow()

    private var mergeJob: Job? = null
    private var thumbnailJob: Job? = null

    // ── Phase 1: File selection ──────────────────────────────────────────────

    fun addPdf(uri: Uri) {
        if (_uiState.value.pdfList.any { it.uri == uri }) {
            _uiState.update { it.copy(errorMessage = "This PDF has already been added.") }
            return
        }
        recycleAndClearPageState()
        val displayName = queryDisplayName(uri)
        _uiState.update { state ->
            state.copy(pdfList = state.pdfList + PdfEntry(uri, displayName, isLoadingMetadata = true))
        }
        viewModelScope.launch {
            val (pageCount, fileSize) = withContext(Dispatchers.IO) { readSizeAndPageCount(uri) }
            _uiState.update { state ->
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
        recycleAndClearPageState()
        _uiState.update { state ->
            val updated = state.pdfList.toMutableList().also { it.removeAt(index) }
            state.copy(pdfList = updated)
        }
    }

    fun movePdf(from: Int, to: Int) {
        val list = _uiState.value.pdfList.toMutableList()
        if (from < 0 || to < 0 || from >= list.size || to >= list.size) return
        recycleAndClearPageState()
        val item = list.removeAt(from)
        list.add(to, item)
        _uiState.update { it.copy(pdfList = list) }
    }

    // ── Phase 2: Page editor ─────────────────────────────────────────────────

    fun loadPageThumbnails() {
        thumbnailJob?.cancel()
        val pdfList = _uiState.value.pdfList

        // Build the flat page list with Pending thumbnails first.
        val flatPages = mutableListOf<PageItem>()
        pdfList.forEach { entry ->
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

        _uiState.update { it.copy(pages = flatPages, thumbnailsLoaded = 0, thumbnailsTotal = flatPages.size) }

        thumbnailJob = viewModelScope.launch {
            loadThumbnailsUseCase(flatPages).collect { loaded ->
                _uiState.update { state ->
                    val idx = state.pages.indexOfFirst { it.id == loaded.id }
                    if (idx == -1) return@update state
                    val updated = state.pages.toMutableList()
                    // Recycle previous bitmap if present to avoid leaks.
                    val prev = updated[idx].thumbnail
                    if (prev != null && prev != loaded.thumbnail && !prev.isRecycled) {
                        prev.recycle()
                    }
                    updated[idx] = loaded
                    state.copy(
                        pages = updated,
                        thumbnailsLoaded = state.thumbnailsLoaded + 1
                    )
                }
            }
        }
    }

    fun rotatePage(pageId: String, clockwise: Boolean) {
        _uiState.update { state ->
            val idx = state.pages.indexOfFirst { it.id == pageId }
            if (idx == -1) return@update state
            val updated = state.pages.toMutableList()
            val delta = if (clockwise) 90 else -90
            updated[idx] = updated[idx].copy(rotation = (updated[idx].rotation + delta + 360) % 360)
            state.copy(pages = updated)
        }
    }

    fun deletePage(pageId: String) {
        _uiState.update { state ->
            val idx = state.pages.indexOfFirst { it.id == pageId }
            if (idx == -1) return@update state
            val updated = state.pages.toMutableList()
            val removed = updated.removeAt(idx)
            removed.thumbnail?.let { if (!it.isRecycled) it.recycle() }
            state.copy(pages = updated)
        }
    }

    fun movePage(from: Int, to: Int) {
        val list = _uiState.value.pages.toMutableList()
        if (from < 0 || to < 0 || from >= list.size || to >= list.size) return
        val item = list.removeAt(from)
        list.add(to, item)
        _uiState.update { it.copy(pages = list) }
    }

    fun setZoomedPage(pageId: String?) {
        _uiState.update { it.copy(zoomedPageId = pageId) }
    }

    // ── Phase 3: Merge ───────────────────────────────────────────────────────

    fun startMerge() {
        val state = _uiState.value
        val ms = state.mergeState
        if (ms !is MergeState.Idle && ms !is MergeState.Cancelled) return
        if (state.pages.isEmpty()) return

        val config = MergeConfig(
            pages = state.pages,
            outputFileName = buildOutputFileName()
        )

        mergeJob = viewModelScope.launch {
            mergeUseCase(config).collect { event ->
                when (event) {
                    is MergeEvent.Started ->
                        _uiState.update { it.copy(mergeState = MergeState.Started) }
                    is MergeEvent.Progress ->
                        _uiState.update { it.copy(mergeState = MergeState.InProgress(event.current, event.total)) }
                    is MergeEvent.Completed ->
                        _uiState.update { it.copy(mergeState = MergeState.Completed(event.fileName)) }
                    is MergeEvent.Failed ->
                        _uiState.update { it.copy(mergeState = MergeState.Idle, errorMessage = event.userMessage) }
                }
            }
        }
    }

    fun cancelMerge() {
        mergeJob?.cancel()
        _uiState.update { it.copy(mergeState = MergeState.Cancelled) }
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Called whenever pdfList changes so the page editor always reloads
     * from the current file list on next entry.
     */
    private fun recycleAndClearPageState() {
        thumbnailJob?.cancel()
        thumbnailJob = null
        _uiState.value.pages.forEach { page ->
            page.thumbnail?.let { if (!it.isRecycled) it.recycle() }
        }
        _uiState.update {
            it.copy(pages = emptyList(), thumbnailsLoaded = 0, thumbnailsTotal = 0, zoomedPageId = null)
        }
    }

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
        _uiState.value.pages.forEach { page ->
            page.thumbnail?.let { if (!it.isRecycled) it.recycle() }
        }
    }
}
