package com.masoudnaji.easysmartpdf.ui.screens.pdftoimage

import com.masoudnaji.easysmartpdf.domain.model.ImageQuality
import com.masoudnaji.easysmartpdf.domain.model.PdfInfo

data class CreatePicturesUiState(
    val pdfInfo: PdfInfo? = null,
    val pageMode: PageMode = PageMode.ALL,
    val fromPage: Int = 1,
    val toPage: Int = 1,
    val imageQuality: ImageQuality = ImageQuality.BALANCED,
    val conversionState: ConversionState = ConversionState.Idle,
    val errorMessage: String? = null
)

enum class PageMode { ALL, RANGE }

sealed interface ConversionState {
    object Idle : ConversionState
    object Started : ConversionState
    data class InProgress(val current: Int, val total: Int, val savedCount: Int = 0) : ConversionState
    object Cancelled : ConversionState
    data class Completed(val savedCount: Int, val folderName: String) : ConversionState
    data class Failed(val message: String) : ConversionState
}
