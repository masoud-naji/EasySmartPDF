package com.masoudnaji.easysmartpdf.ui.screens.pdftoimage

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.masoudnaji.easysmartpdf.data.repository.ImageRepositoryImpl
import com.masoudnaji.easysmartpdf.data.repository.PdfRepositoryImpl
import com.masoudnaji.easysmartpdf.domain.model.ConversionConfig
import com.masoudnaji.easysmartpdf.domain.model.ConversionEvent
import com.masoudnaji.easysmartpdf.domain.model.ImageFormat
import com.masoudnaji.easysmartpdf.domain.model.ImageQuality
import com.masoudnaji.easysmartpdf.domain.usecase.ConvertPdfToImagesUseCase
import com.masoudnaji.easysmartpdf.domain.usecase.GetPdfInfoUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreatePicturesViewModel(application: Application) : AndroidViewModel(application) {

    private val pdfRepository = PdfRepositoryImpl(application)
    private val imageRepository = ImageRepositoryImpl(application)
    private val getPdfInfoUseCase = GetPdfInfoUseCase(pdfRepository)
    private val convertUseCase = ConvertPdfToImagesUseCase(pdfRepository, imageRepository)

    private val _uiState = MutableStateFlow(CreatePicturesUiState())
    val uiState: StateFlow<CreatePicturesUiState> = _uiState.asStateFlow()

    private var selectedPdfUri: Uri? = null
    private var conversionJob: Job? = null

    fun onPdfSelected(uri: Uri) {
        selectedPdfUri = uri
        viewModelScope.launch {
            try {
                val info = getPdfInfoUseCase(uri)
                _uiState.update {
                    it.copy(
                        pdfInfo = info,
                        fromPage = 1,
                        toPage = info.pageCount,
                        pageMode = PageMode.ALL,
                        conversionState = ConversionState.Idle
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "We couldn't open this PDF. Please try another file.")
                }
            }
        }
    }

    fun onPageModeChanged(mode: PageMode) {
        val totalPages = _uiState.value.pdfInfo?.pageCount ?: return
        _uiState.update {
            it.copy(
                pageMode = mode,
                fromPage = if (mode == PageMode.ALL) 1 else it.fromPage,
                toPage = if (mode == PageMode.ALL) totalPages else it.toPage
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

    fun onQualityChanged(quality: ImageQuality) {
        _uiState.update { it.copy(imageQuality = quality) }
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun cancelConversion() {
        conversionJob?.cancel()
        _uiState.update { it.copy(conversionState = ConversionState.Cancelled) }
    }

    fun startConversion() {
        val uri = selectedPdfUri ?: return
        val state = _uiState.value
        val info = state.pdfInfo ?: return
        val cs = state.conversionState
        if (cs !is ConversionState.Idle && cs !is ConversionState.Cancelled) return

        val folderName = buildFolderName(info.fileName, state.imageQuality)
        val config = ConversionConfig(
            pdfUri = uri,
            baseFileName = info.fileName.removeSuffix(".pdf"),
            folderName = folderName,
            fromPage = state.fromPage,
            toPage = state.toPage,
            imageFormat = ImageFormat.JPEG,
            imageQuality = state.imageQuality
        )

        conversionJob = viewModelScope.launch {
            convertUseCase(config).collect { event ->
                when (event) {
                    is ConversionEvent.Started -> {
                        _uiState.update { it.copy(conversionState = ConversionState.Started) }
                    }
                    is ConversionEvent.Progress -> {
                        val saved = (_uiState.value.conversionState as? ConversionState.InProgress)?.savedCount ?: 0
                        _uiState.update {
                            it.copy(
                                conversionState = ConversionState.InProgress(
                                    current = event.current,
                                    total = event.total,
                                    savedCount = saved
                                )
                            )
                        }
                    }
                    is ConversionEvent.PageSaved -> {
                        val current = (_uiState.value.conversionState as? ConversionState.InProgress)?.current ?: event.pageNumber
                        _uiState.update {
                            it.copy(
                                conversionState = ConversionState.InProgress(
                                    current = current,
                                    total = event.total,
                                    savedCount = event.pageNumber
                                )
                            )
                        }
                    }
                    is ConversionEvent.Completed -> {
                        _uiState.update {
                            it.copy(
                                conversionState = ConversionState.Completed(
                                    savedCount = event.savedCount,
                                    folderName = folderName
                                )
                            )
                        }
                    }
                    is ConversionEvent.Failed -> {
                        _uiState.update {
                            it.copy(
                                conversionState = ConversionState.Idle,
                                errorMessage = event.userMessage
                            )
                        }
                    }
                }
            }
        }
    }

    private fun buildFolderName(fileName: String, quality: ImageQuality): String {
        val sanitized = fileName.removeSuffix(".pdf")
            .replace(Regex("[/\\\\:*?\"<>|]"), "_")
            .take(40)
        val qualityLabel = quality.name.lowercase().replaceFirstChar { it.uppercase() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        return "${sanitized}_${qualityLabel}_${timestamp}"
    }
}
