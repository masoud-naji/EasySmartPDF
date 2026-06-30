package com.masoudnaji.easysmartpdf.ui.screens.imagetopdf

import com.masoudnaji.easysmartpdf.domain.model.FitMode
import com.masoudnaji.easysmartpdf.domain.model.ImageEntry
import com.masoudnaji.easysmartpdf.domain.model.PdfOutputQuality
import com.masoudnaji.easysmartpdf.domain.model.PdfMargin
import com.masoudnaji.easysmartpdf.domain.model.PdfOrientation
import com.masoudnaji.easysmartpdf.domain.model.PdfPageSize

data class ImageToPdfUiState(
    val imageList: List<ImageEntry> = emptyList(),
    val pageSize: PdfPageSize = PdfPageSize.A4,
    val orientation: PdfOrientation = PdfOrientation.PORTRAIT,
    val orientationIsManual: Boolean = false,
    val margin: PdfMargin = PdfMargin.SMALL,
    val quality: PdfOutputQuality = PdfOutputQuality.HIGH,
    val fitMode: FitMode = FitMode.FIT,
    val createState: ImageToPdfState = ImageToPdfState.Idle,
    val errorMessage: String? = null
)

sealed interface ImageToPdfState {
    object Idle : ImageToPdfState
    object Started : ImageToPdfState
    data class InProgress(val current: Int, val total: Int) : ImageToPdfState
    object Cancelled : ImageToPdfState
    data class Completed(val fileName: String) : ImageToPdfState
}
