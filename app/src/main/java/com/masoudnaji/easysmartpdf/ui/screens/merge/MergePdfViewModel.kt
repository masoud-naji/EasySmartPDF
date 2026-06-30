package com.masoudnaji.easysmartpdf.ui.screens.merge

import android.app.Application
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.masoudnaji.easysmartpdf.data.repository.MergeRepositoryImpl
import com.masoudnaji.easysmartpdf.domain.model.MergeConfig
import com.masoudnaji.easysmartpdf.domain.model.MergeEvent
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
    private val mergeUseCase = MergePdfUseCase(mergeRepository)

    private val _uiState = MutableStateFlow(MergePdfUiState())
    val uiState: StateFlow<MergePdfUiState> = _uiState.asStateFlow()

    private var mergeJob: Job? = null

    fun addPdf(uri: Uri) {
        if (_uiState.value.pdfList.any { it.uri == uri }) {
            _uiState.update { it.copy(errorMessage = "This PDF has already been added.") }
            return
        }
        // Add immediately so the filename appears without waiting for disk IO.
        // ContentResolver display-name lookup is a local DB query — fast enough for main thread.
        val displayName = queryDisplayName(uri)
        _uiState.update { state ->
            state.copy(pdfList = state.pdfList + PdfEntry(uri, displayName, isLoadingMetadata = true))
        }
        // Load size + page count in the background; update this entry when ready.
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
        _uiState.update { state ->
            val updated = state.pdfList.toMutableList().also { it.removeAt(index) }
            state.copy(pdfList = updated)
        }
    }

    fun movePdf(from: Int, to: Int) {
        val list = _uiState.value.pdfList.toMutableList()
        if (from < 0 || to < 0 || from >= list.size || to >= list.size) return
        val item = list.removeAt(from)
        list.add(to, item)
        _uiState.update { it.copy(pdfList = list) }
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun cancelMerge() {
        mergeJob?.cancel()
        _uiState.update { it.copy(mergeState = MergeState.Cancelled) }
    }

    fun startMerge() {
        val state = _uiState.value
        val ms = state.mergeState
        if (ms !is MergeState.Idle && ms !is MergeState.Cancelled) return
        if (state.pdfList.size < 2) return

        val outputFileName = buildOutputFileName()
        val config = MergeConfig(
            pdfUris = state.pdfList.map { it.uri },
            outputFileName = outputFileName
        )

        mergeJob = viewModelScope.launch {
            mergeUseCase(config).collect { event ->
                when (event) {
                    is MergeEvent.Started -> {
                        _uiState.update { it.copy(mergeState = MergeState.Started) }
                    }
                    is MergeEvent.Progress -> {
                        _uiState.update {
                            it.copy(mergeState = MergeState.InProgress(event.current, event.total))
                        }
                    }
                    is MergeEvent.Completed -> {
                        _uiState.update {
                            it.copy(mergeState = MergeState.Completed(fileName = event.fileName))
                        }
                    }
                    is MergeEvent.Failed -> {
                        _uiState.update {
                            it.copy(
                                mergeState = MergeState.Idle,
                                errorMessage = event.userMessage
                            )
                        }
                    }
                }
            }
        }
    }

    private fun buildOutputFileName(): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US).format(Date())
        return "Merged_$timestamp.pdf"
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
        } catch (_: Exception) { }
        return Pair(pageCount, fileSize)
    }
}
