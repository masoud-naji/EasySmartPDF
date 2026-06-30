package com.masoudnaji.easysmartpdf.ui.screens.split

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.masoudnaji.easysmartpdf.data.repository.PdfRepositoryImpl
import com.masoudnaji.easysmartpdf.data.repository.SplitRepositoryImpl
import com.masoudnaji.easysmartpdf.domain.model.SplitConfig
import com.masoudnaji.easysmartpdf.domain.model.SplitEvent
import com.masoudnaji.easysmartpdf.domain.usecase.GetPdfInfoUseCase
import com.masoudnaji.easysmartpdf.domain.usecase.SplitPdfUseCase
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

class SplitPdfViewModel(application: Application) : AndroidViewModel(application) {

    private val splitRepository = SplitRepositoryImpl(application)
    private val pdfRepository = PdfRepositoryImpl(application)
    private val getPdfInfoUseCase = GetPdfInfoUseCase(pdfRepository)
    private val splitPdfUseCase = SplitPdfUseCase(splitRepository)

    private val _uiState = MutableStateFlow(SplitPdfUiState())
    val uiState: StateFlow<SplitPdfUiState> = _uiState.asStateFlow()

    private var selectedPdfUri: Uri? = null
    private var splitJob: Job? = null

    fun onPdfSelected(uri: Uri) {
        selectedPdfUri = uri
        viewModelScope.launch {
            try {
                val info = getPdfInfoUseCase(uri)
                val fileSize = withContext(Dispatchers.IO) { queryFileSize(uri) }
                _uiState.update {
                    it.copy(
                        pdfInfo = info,
                        fileSize = fileSize,
                        fromPage = 1,
                        toPage = info.pageCount,
                        pageMode = SplitPageMode.ALL,
                        splitState = SplitState.Idle
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "We couldn't open this PDF. Please try another file.")
                }
            }
        }
    }

    private fun queryFileSize(uri: Uri): Long {
        val context = getApplication<Application>()
        return context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))
                else 0L
            } ?: 0L
    }

    fun onPageModeChanged(mode: SplitPageMode) {
        val totalPages = _uiState.value.pdfInfo?.pageCount ?: return
        _uiState.update {
            it.copy(
                pageMode = mode,
                fromPage = if (mode == SplitPageMode.ALL) 1 else it.fromPage,
                toPage = if (mode == SplitPageMode.ALL) totalPages else it.toPage
            )
        }
    }

    fun onFromPageChanged(page: Int) {
        _uiState.update { state ->
            state.copy(fromPage = page.coerceIn(1, state.toPage))
        }
    }

    fun onToPageChanged(page: Int) {
        val maxPage = _uiState.value.pdfInfo?.pageCount ?: 1
        _uiState.update { state ->
            state.copy(toPage = page.coerceIn(state.fromPage, maxPage))
        }
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun cancelSplit() {
        splitJob?.cancel()
        _uiState.update { it.copy(splitState = SplitState.Cancelled) }
    }

    fun onOutputModeChanged(mode: SplitOutputMode) {
        _uiState.update { it.copy(outputMode = mode) }
    }

    fun startSplit() {
        val uri = selectedPdfUri ?: return
        val state = _uiState.value
        val info = state.pdfInfo ?: return
        val ss = state.splitState
        if (ss !is SplitState.Idle && ss !is SplitState.Cancelled) return

        val folderName = buildFolderName(info.fileName)
        val pagesToSplit = when (state.pageMode) {
            SplitPageMode.ALL -> (1..info.pageCount).toList()
            SplitPageMode.RANGE -> (state.fromPage..state.toPage).toList()
        }

        val config = SplitConfig(
            pdfUri = uri,
            folderName = folderName,
            pagesToSplit = pagesToSplit,
            separatePdfs = state.outputMode == SplitOutputMode.SEPARATE_PDFS
        )

        splitJob = viewModelScope.launch {
            splitPdfUseCase(config).collect { event ->
                when (event) {
                    is SplitEvent.Started -> {
                        _uiState.update { it.copy(splitState = SplitState.Started) }
                    }
                    is SplitEvent.Progress -> {
                        _uiState.update {
                            it.copy(splitState = SplitState.InProgress(event.current, event.total))
                        }
                    }
                    is SplitEvent.Completed -> {
                        _uiState.update {
                            it.copy(
                                splitState = SplitState.Completed(
                                    fileCount = event.fileCount,
                                    folderName = event.folderName
                                )
                            )
                        }
                    }
                    is SplitEvent.Failed -> {
                        _uiState.update {
                            it.copy(
                                splitState = SplitState.Idle,
                                errorMessage = event.userMessage
                            )
                        }
                    }
                }
            }
        }
    }

    private fun buildFolderName(fileName: String): String {
        val sanitized = fileName.removeSuffix(".pdf")
            .replace(Regex("[/\\\\:*?\"<>|]"), "_")
            .take(40)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        return "${sanitized}_${timestamp}"
    }
}
